package com.handyteejay.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Ride(
    val id: String = "",
    val passengerId: String = "",
    val driverId: String? = null,
    val pickup: LocationInfo = LocationInfo(),
    val destination: LocationInfo = LocationInfo(),
    val status: RideStatus = RideStatus.REQUESTED,
    val fare: Double = 0.0,
    val distance: Double = 0.0,
    val duration: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val cancelledAt: Long? = null,
    val cancellationReason: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val driverLocation: GeoPoint? = null,
    val routePolyline: String? = null
)

@Serializable
data class LocationInfo(
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val placeName: String = ""
)

enum class RideStatus {
    REQUESTED, ACCEPTED, DRIVER_ARRIVED, STARTED, COMPLETED, CANCELLED
}

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUND
ED
}
