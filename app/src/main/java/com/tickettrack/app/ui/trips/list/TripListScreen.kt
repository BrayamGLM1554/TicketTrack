package com.tickettrack.app.ui.trips.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.domain.model.trip.TripStatus
import com.tickettrack.app.ui.trips.list.components.TripCard

/**
 * Pantalla de lista de viajes.
 *
 * Siguiendo arquitectura MVVM:
 * - Observa el estado del ViewModel
 * - Renderiza UI basada en el estado
 * - Delega eventos al ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToTripDetail: (String) -> Unit,
    currentUserId: String,
    currentUserRole: String,
    viewModel: TripListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Cargar viajes al iniciar la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadTrips(currentUserId, currentUserRole)
    }

    // Mostrar error si existe
    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Error") },
            text = { Text(state.errorMessage ?: "") },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("Entendido")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Viajes",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón de filtros
                    IconButton(onClick = { viewModel.toggleFilters() }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = if (state.selectedStatusFilter != null)
                                Color(0xFF5AC5C5)
                            else
                                Color.Gray
                        )
                    }

                    // Botón de búsqueda (opcional, por ahora comentado)
                    /*
                    IconButton(onClick = { /* TODO: Implementar búsqueda */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    }
                    */
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            // Solo admin/consignatario puede crear viajes
            if (currentUserRole == "ADMIN" || currentUserRole == "consignatario") {
                FloatingActionButton(
                    onClick = onNavigateToCreateTrip,
                    containerColor = Color(0xFF5AC5C5),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir Viaje"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros (expandible)
            if (state.showFilters) {
                FilterSection(
                    selectedStatus = state.selectedStatusFilter,
                    onStatusSelected = { status -> viewModel.filterByStatus(status) },
                    onClearFilters = { viewModel.clearFilters() },
                    tripCounts = mapOf(
                        TripStatus.PENDING to state.getTripCountByStatus(TripStatus.PENDING),
                        TripStatus.IN_PROGRESS to state.getTripCountByStatus(TripStatus.IN_PROGRESS),
                        TripStatus.COMPLETED to state.getTripCountByStatus(TripStatus.COMPLETED)
                    )
                )
            }

            // Contenido principal
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // Estado de carga inicial
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF5AC5C5)
                        )
                    }

                    // Estado vacío
                    state.isEmpty -> {
                        EmptyState(
                            message = if (currentUserRole == "ADMIN" || currentUserRole == "consignatario") {
                                "No hay viajes registrados.\n¡Crea tu primer viaje!"
                            } else {
                                "No tienes viajes asignados aún."
                            },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Lista de viajes con pull to refresh
                    else -> {
                        val filteredTrips = state.getFilteredTrips()

                        if (filteredTrips.isEmpty()) {
                            // Sin resultados después de filtrar
                            EmptyState(
                                message = "No se encontraron viajes con los filtros seleccionados.",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            PullToRefreshBox(
                                isRefreshing = state.isRefreshing,
                                onRefresh = { viewModel.refreshTrips() },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = filteredTrips,
                                        key = { trip -> trip.id }
                                    ) { trip ->
                                        TripCard(
                                            trip = trip,
                                            onClick = { onNavigateToTripDetail(trip.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sección de filtros.
 */
@Composable
private fun FilterSection(
    selectedStatus: TripStatus?,
    onStatusSelected: (TripStatus?) -> Unit,
    onClearFilters: () -> Unit,
    tripCounts: Map<TripStatus, Int>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtrar por estado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (selectedStatus != null) {
                TextButton(onClick = onClearFilters) {
                    Text(
                        text = "Limpiar",
                        color = Color(0xFF5AC5C5)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chips de filtro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onStatusSelected(null) },
                label = { Text("Todos (${tripCounts.values.sum()})") }
            )

            FilterChip(
                selected = selectedStatus == TripStatus.PENDING,
                onClick = { onStatusSelected(TripStatus.PENDING) },
                label = { Text("Pendiente (${tripCounts[TripStatus.PENDING] ?: 0})") }
            )

            FilterChip(
                selected = selectedStatus == TripStatus.IN_PROGRESS,
                onClick = { onStatusSelected(TripStatus.IN_PROGRESS) },
                label = { Text("En Progreso (${tripCounts[TripStatus.IN_PROGRESS] ?: 0})") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FilterChip(
            selected = selectedStatus == TripStatus.COMPLETED,
            onClick = { onStatusSelected(TripStatus.COMPLETED) },
            label = { Text("Completado (${tripCounts[TripStatus.COMPLETED] ?: 0})") }
        )
    }
}

/**
 * Estado vacío.
 */
@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📦",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}