package com.handyteejay.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val userType: UserType = UserType.PASSENGER,
    val profileImageUrl: String? = null,
    val isOnline: Boolean = false,
    val currentLocation: GeoPoint? = null,
    val vehicleInfo: VehicleInfo? = null,
    val rating: Double = 5.0,
    val totalRides: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserType {
    PASSENGER, DRIVER
}

@Serializable
data class VehicleInfo(
    val model: String = "",
    val color: String = "",
    val licensePlate: String = "",
    val year: Int = 0
)

@Serializable
data class GeoPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0
.0
)
