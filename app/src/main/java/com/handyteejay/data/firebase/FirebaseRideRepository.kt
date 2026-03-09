package com.handyteejay.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint as FirestoreGeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.handyteejay.data.model.GeoPoint
import com.handyteejay.data.model.LocationInfo
import com.handyteejay.data.model.Ride
import com.handyteejay.data.model.RideStatus
import com.handyteejay.data.repository.RideRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRideRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging
) : RideRepository {

    private val ridesCollection = firestore.collection("rides")

    override suspend fun requestRide(pickup: LocationInfo, destination: LocationInfo): Result<Ride> = try {
        val currentUser = auth.currentUser ?: throw Exception("Not authenticated")
        
        val ride = Ride(
            id = ridesCollection.document().id,
            passengerId = currentUser.uid,
            pickup = pickup,
            destination = destination,
            status = RideStatus.REQUESTED,
            fare = calculateFare(0.0, 0)
        )
        
        ridesCollection.document(ride.id).set(ride).await()
        messaging.subscribeToTopic("ride_${ride.id}").await()
        
        Result.success(ride)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun acceptRide(rideId: String): Result<Unit> = try {
        val driverId = auth.currentUser?.uid ?: throw Exception("Not authenticated")
        
        val updates = hashMapOf(
            "driverId" to driverId,
            "status" to RideStatus.ACCEPTED.name,
            "acceptedAt" to System.currentTimeMillis()
        )
        
        ridesCollection.document(rideId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateRideStatus(rideId: String, status: RideStatus): Result<Unit> = try {
        val updates = hashMapOf(
            "status" to status.name,
            "${status.name.lowercase()}At" to System.currentTimeMillis()
        )
        
        ridesCollection.document(rideId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun cancelRide(rideId: String, reason: String): Result<Unit> = try {
        val updates = hashMapOf(
            "status" to RideStatus.CANCELLED.name,
            "cancelledAt" to System.currentTimeMillis(),
            "cancellationReason" to reason
        )
        
        ridesCollection.document(rideId).update(updates).await()
        messaging.unsubscribeFromTopic("ride_$rideId").await()
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateDriverLocation(rideId: String, location: GeoPoint): Result<Unit> = try {
        val geoPoint = FirestoreGeoPoint(location.latitude, location.longitude)
        ridesCollection.document(rideId)
            .update("driverLocation", geoPoint)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun observeRide(rideId: String): Flow<Ride> = callbackFlow {
        val listener = ridesCollection.document(rideId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val ride = snapshot?.toObject(Ride::class.java)
                if (ride != null) {
                    trySend(ride)
                }
            }
        
        awaitClose { listener.remove() }
    }

    override fun observeNearbyRides(location: GeoPoint, radiusKm: Double): Flow<List<Ride>> = callbackFlow {
        val query = ridesCollection
            .whereEqualTo("status", RideStatus.REQUESTED.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val rides = snapshot?.documents?.mapNotNull { 
                it.toObject(Ride::class.java) 
            } ?: emptyList()
            
            trySend(rides)
        }
        
        awaitClose { listener.remove() }
    }

    override fun observeUserRides(): Flow<List<Ride>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            close(Exception("Not authenticated"))
            return@callbackFlow
        }
        
        val query = ridesCollection
            .whereEqualTo("passengerId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val rides = snapshot?.documents?.mapNotNull { 
                it.toObject(Ride::class.java) 
            } ?: emptyList()
            
            trySend(rides)
        }
        
        awaitClose { listener.remove() }
    }

    override suspend fun calculateFare(distanceKm: Double, durationMin: Int): Double {
        val baseFare = 2.50
        val perKmRate = 1.50
        val perMinRate = 0.25
        return baseFare + (distanceKm * perKmRate) + (durationMin * perMinRate)
    }
}
