package com.handyteejay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.handyteejay.data.model.LocationInfo

@Composable
fun SearchBottomSheet(
    pickup: LocationInfo?,
    destination: LocationInfo?,
    onPickupClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onRequestRide: () -> Unit,
    isLoading: Boolean,
    fareEstimate: Double?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LocationField(
                icon = Icons.Default.LocationOn,
                label = "Pickup Location",
                value = pickup?.address ?: "Current Location",
                onClick = onPickupClick,
                isPickup = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            LocationField(
                icon = Icons.Default.Place,
                label = "Where to?",
                value = destination?.address ?: "",
                onClick = onDestinationClick,
                isPickup = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (fareEstimate != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estimated Fare",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "$${String.format("%.2f", fareEstimate)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onRequestRide,
                enabled = pickup != null && destination != null && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Request Handy Teejay")
                }
            }
        }
    }
}

@Composable
private fun LocationField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    isPickup: Boolean
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPickup) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value.ifEmpty { "Tap to select" },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
