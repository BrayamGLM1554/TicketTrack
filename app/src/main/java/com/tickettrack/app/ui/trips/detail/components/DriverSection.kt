package com.tickettrack.app.ui.trips.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Sección de información del transportista asignado.
 */
@Composable
fun DriverSection(
    driverId: String?,
    driverName: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🚛",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Transportista Asignado",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF5AC5C5))
                }
            } else if (driverId != null && driverName != null) {
                // Información básica del transportista
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Placeholder para foto
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = Color(0xFF5AC5C5)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = driverName.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "ID: $driverId",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Divider()

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ℹ️",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Información detallada del transportista disponible próximamente",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }
            } else {
                // Sin datos
                Text(
                    text = "No se asignó transportista",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}