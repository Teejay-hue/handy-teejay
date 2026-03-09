package com.handyteejay.ui.screens.passenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.handyteejay.data.model.GeoPoint
import com.handyteejay.data.model.LocationInfo
import com.handyteejay.data.model.Ride
import com.handyteejay.data.model.User
import com.handyteejay.data.repository.RideRepository
import com.handyteejay.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PassengerViewModel @Inject constructor(
    private val rideRepository: RideRepository,
    private val userRepository: UserRepository,
    private val locationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(PassengerUiState())
    val uiState: StateFlow<PassengerUiState> = _uiState.asStateFlow()

    init {
        observeNearbyDrivers()
    }

    private fun observeNearbyDrivers() {
        viewModelScope.launch {
            _uiState.value.currentLocation?.let { location ->
                userRepository.observeNearbyDrivers(
                    GeoPoint(location.latitude, location.longitude),
                    5.0
                ).collect { drivers ->
                    _uiState.update { it.copy(nearbyDrivers = drivers) }
                }
            }
        }
    }

    fun setPickup(location: LocationInfo) {
        _uiState.update { it.copy(pickupLocation = location) }
        calculateFare()
    }

    fun setDestination(location: LocationInfo) {
        _uiState.update { it.copy(destination = location) }
        calculateFare()
    }

    private fun calculateFare() {
        val state = _uiState.value
        if (state.pickupLocation != null && state.destination != null) {
            val distance = calculateDistance(state.pickupLocation, state.destination)
            viewModelScope.launch {
                val fare = rideRepository.calculateFare(distance, (distance * 2).toInt())
                _uiState.update { it.copy(fareEstimate = fare) }
            }
        }
    }

    private fun calculateDistance(pickup: LocationInfo, destination: LocationInfo): Double {
        val R = 6371
        val lat1 = Math.toRadians(pickup.latitude)
        val lat2 = Math.toRadians(destination.latitude)
        val dLat = Math.toRadians(destination.latitude - pickup.latitude)
        val dLon = Math.toRadians(destination.longitude - pickup.longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }

    fun requestRide() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val state = _uiState.value
            if (state.pickupLocation != null && state.destination != null) {
                rideRepository.requestRide(state.pickupLocation, state.destination)
                    .onSuccess { ride ->
                        _uiState.update { 
                            it.copy(
                                currentRide = ride,
                                isLoading = false
                            )
                        }
                        observeRideUpdates(ride.id)
                    }
                    .onFailure { e ->
                        _uiState.update { 
                            it.copy(
                                error = e.message,
                                isLoading = false
                            )
                        }
                    }
            }
        }
    }

    private fun observeRideUpdates(rideId: String) {
        viewModelScope.launch {
            rideRepository.observeRide(rideId).collect { ride ->
                _uiState.update { it.copy(currentRide = ride) }
            }
        }
    }

    fun centerOnCurrentLocation() {
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class PassengerUiState(
    val currentLocation: LatLng? = null,
    val pickupLocation: LocationInfo? = null,
    val destination: LocationInfo? = null,
    val nearbyDrivers: List<User> = emptyList(),
    val currentRide: Ride? = null,
    val fareEstimate: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
