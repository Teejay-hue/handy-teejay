package com.handyteejay.data.repository

import com.handyteejay.data.model.GeoPoint
import com.handyteejay.data.model.LocationInfo
import com.handyteejay.data.model.Ride
import com.handyteejay.data.model.RideStatus
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    suspend fun requestRide(pickup: LocationInfo, destination: LocationInfo): Result<Ride>
    suspend fun acceptRide(rideId: String): Result<Unit>
    suspend fun updateRideStatus(rideId: String, status: RideStatus): Result<Unit>
    suspend fun cancelRide(rideId: String, reason: String): Result<Unit>
    suspend fun updateDriverLocation(rideId: String, location: GeoPoint): Result<Unit>
    fun observeRide(rideId: String): Flow<Ride>
    fun observeNearbyRides(location: GeoPoint, radiusKm: Double): Flow<List<Ride>>
    fun observeUserRides(): Flow<List<Ride>>
    suspend fun calculateFare(distanceKm: Double, durationMin: Int): Double

}
