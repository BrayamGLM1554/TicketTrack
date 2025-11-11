package com.tickettrack.app.ui.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.domain.model.Trip
import com.tickettrack.app.domain.model.TripStatus
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsListScreen(
    token: String,
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToTripDetails: (String) -> Unit,
    viewModel: TripsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTrips(token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Viajes",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "${uiState.filteredTrips.size} viajes",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }

                    FloatingActionButton(
                        onClick = onNavigateToCreateTrip,
                        containerColor = Primary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear viaje"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filtros
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == null,
                            onClick = { viewModel.filterByStatus(null) },
                            label = { Text("Todos") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == TripStatus.PENDING,
                            onClick = { viewModel.filterByStatus(TripStatus.PENDING) },
                            label = { Text("Pendientes") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFA726),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == TripStatus.IN_PROGRESS,
                            onClick = { viewModel.filterByStatus(TripStatus.IN_PROGRESS) },
                            label = { Text("En Progreso") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF42A5F5),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == TripStatus.COMPLETED,
                            onClick = { viewModel.filterByStatus(TripStatus.COMPLETED) },
                            label = { Text("Completados") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF66BB6A),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Contenido
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadTrips(token) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            uiState.filteredTrips.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay viajes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uiState.selectedFilter != null) {
                                "No hay viajes con este estado"
                            } else {
                                "Crea tu primer viaje"
                            },
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredTrips) { trip ->
                        TripCard(
                            trip = trip,
                            onTripClick = { onNavigateToTripDetails(trip.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripCard(
    trip: Trip,
    onTripClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTripClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header con nombre y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.tripName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(status = trip.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Origen
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Origen",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = trip.origin,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Destino
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Destino",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = trip.destination,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider()

            Spacer(modifier = Modifier.height(12.dp))

            // Info adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Transportista",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = trip.transportistaName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Presupuesto",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "$${String.format("%.2f", trip.budgetAssigned)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: TripStatus?) {
    val (text, color) = when (status) {
        TripStatus.PENDING -> "Pendiente" to Color(0xFFFFA726)
        TripStatus.IN_PROGRESS -> "En Progreso" to Color(0xFF42A5F5)
        TripStatus.COMPLETED -> "Completado" to Color(0xFF66BB6A)
        else -> "Desconocido" to Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}