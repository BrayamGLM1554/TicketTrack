package com.tickettrack.app.ui.transportista.mytrips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tickettrack.app.domain.model.trip.TripStatus
import com.tickettrack.app.ui.transportista.mytrips.components.MyTripCard

/**
 * Pantalla de lista de viajes asignados al transportista (USER).
 *
 * Funcionalidades:
 * - Lista de viajes filtrados (assignedDriverId = userEmail)
 * - Filtros por estado
 * - Búsqueda por texto
 * - Pull to refresh
 * - Estadísticas rápidas
 * - Navegación al detalle de viaje
 *
 * HU relevantes: HU11 (Consultar Detalles), HU14 (Validación de Roles)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripsScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: MyTripsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(state.isRefreshing)

    val displayTrips = state.getDisplayTrips()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Viajes",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón de filtros
                    IconButton(onClick = { viewModel.toggleFilters() }) {
                        Badge(
                            containerColor = if (state.hasActiveFilters()) {
                                Color(0xFFF44336)
                            } else {
                                Color.Transparent
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtros"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5AC5C5),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshTrips() },
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Estadísticas rápidas
                QuickStats(
                    total = state.totalTrips,
                    inProgress = state.tripsInProgress,
                    pending = state.tripsPending,
                    completed = state.tripsCompleted
                )

                // Sección de filtros (expandible)
                if (state.showFilters) {
                    FiltersSection(
                        selectedStatus = state.selectedStatusFilter,
                        searchQuery = state.searchQuery,
                        onStatusSelected = { viewModel.filterByStatus(it) },
                        onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }

                // Badge de filtros activos
                if (state.hasActiveFilters()) {
                    ActiveFiltersBadge(
                        onClear = { viewModel.clearFilters() }
                    )
                }

                // Lista de viajes o estados
                when {
                    state.isLoading && state.trips.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF5AC5C5)
                            )
                        }
                    }

                    state.isEmpty -> {
                        EmptyState(
                            message = "No tienes viajes asignados aún.\nEspera a que el admin te asigne uno."
                        )
                    }

                    displayTrips.isEmpty() -> {
                        EmptyState(
                            message = "No se encontraron viajes con los filtros aplicados.\nIntenta cambiar los criterios de búsqueda."
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayTrips) { trip ->
                                MyTripCard(
                                    trip = trip,
                                    onClick = { onNavigateToDetail(trip.id) }
                                )
                            }
                        }
                    }
                }

                // Mensaje de error
                state.errorMessage?.let { error ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text(error)
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES INTERNOS
// ==========================================

@Composable
private fun QuickStats(
    total: Int,
    inProgress: Int,
    pending: Int,
    completed: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Total",
                value = total.toString(),
                color = Color(0xFF5AC5C5)
            )
            StatItem(
                label = "En Viaje",
                value = inProgress.toString(),
                color = Color(0xFF2196F3)
            )
            StatItem(
                label = "Pendientes",
                value = pending.toString(),
                color = Color(0xFFFF9800)
            )
            StatItem(
                label = "Completados",
                value = completed.toString(),
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun FiltersSection(
    selectedStatus: TripStatus?,
    searchQuery: String,
    onStatusSelected: (TripStatus?) -> Unit,
    onSearchChanged: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre, origen o destino...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5AC5C5),
                    unfocusedBorderColor = Color.Gray
                )
            )

            // Filtros de estado
            Text(
                text = "Filtrar por estado:",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { onStatusSelected(null) },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = selectedStatus == TripStatus.PENDING,
                    onClick = { onStatusSelected(TripStatus.PENDING) },
                    label = { Text("Pendientes") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF9800).copy(alpha = 0.3f)
                    )
                )
                FilterChip(
                    selected = selectedStatus == TripStatus.IN_PROGRESS,
                    onClick = { onStatusSelected(TripStatus.IN_PROGRESS) },
                    label = { Text("En Viaje") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2196F3).copy(alpha = 0.3f)
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == TripStatus.COMPLETED,
                    onClick = { onStatusSelected(TripStatus.COMPLETED) },
                    label = { Text("Completados") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.3f)
                    )
                )
            }

            // Botón limpiar filtros
            if (selectedStatus != null || searchQuery.isNotEmpty()) {
                TextButton(
                    onClick = onClearFilters,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar filtros")
                }
            }
        }
    }
}

@Composable
private fun ActiveFiltersBadge(
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF5AC5C5).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color(0xFF5AC5C5),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filtros activos",
                    color = Color(0xFF5AC5C5),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
            TextButton(onClick = onClear) {
                Text("Limpiar", color = Color(0xFF5AC5C5))
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}