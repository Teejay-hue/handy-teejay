package com.handyteejay.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint as FirestoreGeoPoint
import com.handyteejay.data.model.GeoPoint
import com.handyteejay.data.model.User
import com.handyteejay.data.model.VehicleInfo
import com.handyteejay.data.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun getUser(userId: String): Result<User> = try {
        val doc = usersCollection.document(userId).get().await()
        val user = doc.toObject(User::class.java)
            ?: throw Exception("User not found")
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun observeUser(userId: String): Flow<User> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val user = snapshot?.toObject(User::class.java)
                if (user != null) {
                    trySend(user)
                }
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun updateLocation(location: GeoPoint): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("Not authenticated")
        val geoPoint = FirestoreGeoPoint(location.latitude, location.longitude)
        
        usersCollection.document(userId)
            .update("currentLocation", geoPoint)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("Not authenticated")
        
        usersCollection.document(userId)
            .update("isOnline", isOnline)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateVehicleInfo(vehicleInfo: VehicleInfo): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("Not authenticated")
        
        usersCollection.document(userId)
            .update("vehicleInfo", vehicleInfo)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun rateUser(userId: String, rating: Double, comment: String?): Result<Unit> = try {
        val ratingsCollection = firestore.collection("ratings")
        val ratingData = hashMapOf(
            "userId" to userId,
            "ratedBy" to auth.currentUser?.uid,
            "rating" to rating,
            "comment" to comment,
            "timestamp" to System.currentTimeMillis()
        )
        
        ratingsCollection.add(ratingData).await()
        
        // Update user's average rating
        val userDoc = usersCollection.document(userId).get().await()
        val currentUser = userDoc.toObject(User::class.java)
        val newTotalRides = (currentUser?.totalRides ?: 0) + 1
        val newRating = ((currentUser?.rating ?: 5.0) * (newTotalRides - 1) + rating) / newTotalRides
        
        usersCollection.document(userId).update(
            mapOf(
                "rating" to newRating,
                "totalRides" to newTotalRides
            )
        ).await()
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun observeNearbyDrivers(location: GeoPoint, radiusKm: Double): Flow<List<User>> = callbackFlow {
        // Note: In production, use GeoFire for proper geospatial queries
        val query = usersCollection
            .whereEqualTo("userType", "DRIVER")
            .whereEqualTo("isOnline", true)
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val drivers = snapshot?.documents?.mapNotNull { 
                it.toObject(User::class.java) 
            } ?: emptyList()
            
            trySend(drivers)
        }
        
        awaitClose { listener.remove() }
    }
}
