package com.handyteejay.data.repository

import com.handyteejay.data.model.GeoPoint
import com.handyteejay.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUser(userId: String): Result<User>
    fun observeUser(userId: String): Flow<User>
    suspend fun updateLocation(location: GeoPoint): Result<Unit>
    suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit>
    suspend fun updateVehicleInfo(vehicleInfo: com.handyteejay.data.model.VehicleInfo): Result<Unit>
    suspend fun rateUser(userId: String, rating: Double, comment: String?): Result<Unit>
    fun observeNearbyDrivers(location: GeoPoint, radiusKm: Double): Flow<List<User>>

}
