package com.tickettrack.app.ui.main.trips.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.domain.model.trip.StatusChange
import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.TripStatus
import com.tickettrack.app.ui.main.trips.detail.components.BudgetSection
import com.tickettrack.app.ui.main.trips.detail.components.DriverSection
import com.tickettrack.app.ui.main.trips.detail.components.IncreaseBudgetDialog

/**
 * Pantalla de detalle de un viaje.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    // Inicializar ViewModel con TokenManager
    val viewModel: TripDetailViewModel = remember {
        TripDetailViewModel(
            tripId = tripId,
            tokenManager = tokenManager
        )
    }

    val state by viewModel.state.collectAsState()

    // Mostrar error si existe
    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
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

    // Diálogo de aumentar presupuesto
    if (state.showIncreaseBudgetDialog && state.trip != null) {
        IncreaseBudgetDialog(
            currentBudget = state.trip!!.budget.current,
            newAmount = state.newBudgetAmount,
            newAmountError = state.newBudgetAmountError,
            reason = state.increaseReason,
            reasonError = state.increaseReasonError,
            selectedUrgency = state.selectedUrgency,
            isIncreasing = state.isIncreasingBudget,
            onNewAmountChanged = viewModel::onNewBudgetAmountChanged,
            onReasonChanged = viewModel::onIncreaseReasonChanged,
            onUrgencySelected = viewModel::onUrgencySelected,
            onConfirm = { viewModel.increaseBudget() },
            onDismiss = { viewModel.hideIncreaseBudgetDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detalle del Viaje",
                            fontWeight = FontWeight.Bold
                        )
                        state.trip?.let { trip ->
                            Text(
                                text = trip.cargoName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color(0xFF5AC5C5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoadingTrip) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF5AC5C5))
                        Text(
                            text = "Cargando viaje...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else if (state.trip != null) {
                // Contenido principal
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header con estado
                    TripHeader(
                        trip = state.trip!!,
                        onStartTrip = { viewModel.startTrip() },
                        onCompleteTrip = { viewModel.completeTrip() },
                        onCancelTrip = { viewModel.cancelTrip() },
                        canChangeStatus = viewModel.canChangeStatus(),
                        isChangingStatus = state.isChangingStatus
                    )

                    // Sección de presupuesto
                    BudgetSection(
                        budget = state.trip!!.budget,
                        totalExpenses = state.trip!!.totalExpenses,
                        remainingBudget = state.trip!!.remainingBudget,
                        isHistoryExpanded = state.isBudgetHistoryExpanded,
                        canIncreaseBudget = viewModel.canIncreaseBudget(),
                        onToggleHistory = { viewModel.toggleBudgetHistory() },
                        onIncreaseBudget = { viewModel.showIncreaseBudgetDialog() }
                    )

                    // Sección de transportista
                    DriverSection(
                        driverId = state.trip!!.assignedDriverId,
                        driverName = "Transportista", // Temporal hasta tener endpoint
                        isLoading = state.isLoadingDriver
                    )

                    // Información de la carga
                    CargoInfoCard(trip = state.trip!!)

                    // Ubicaciones
                    LocationsCard(trip = state.trip!!)

                    // Historial de estados
                    if (state.trip!!.statusHistory.isNotEmpty()) {
                        StatusHistoryCard(
                            history = state.trip!!.statusHistory,
                            isExpanded = state.isStatusHistoryExpanded,
                            onToggle = { viewModel.toggleStatusHistory() }
                        )
                    }

                    // Espaciador final
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // Sin datos
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text = "No se pudo cargar el viaje",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5AC5C5)
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripHeader(
    trip: Trip,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit,
    onCancelTrip: () -> Unit,
    canChangeStatus: Boolean,
    isChangingStatus: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Estado actual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(trip.status.getColor())
                ) {
                    Text(
                        text = trip.status.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Botones de acción (solo si tiene permisos)
            if (canChangeStatus && !isChangingStatus) {
                Divider()

                when (trip.status) {
                    TripStatus.PENDING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onStartTrip,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("Iniciar Viaje")
                            }

                            OutlinedButton(
                                onClick = onCancelTrip,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF44336)
                                )
                            ) {
                                Text("Cancelar")
                            }
                        }
                    }
                    TripStatus.IN_PROGRESS -> {
                        Button(
                            onClick = onCompleteTrip,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Completar Viaje")
                        }
                    }
                    else -> {
                        // No hay acciones disponibles
                    }
                }
            }

            if (isChangingStatus) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF5AC5C5)
                )
            }
        }
    }
}

@Composable
private fun CargoInfoCard(
    trip: Trip
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📦 Información de la Carga",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Divider()

            InfoRow("Tipo:", trip.cargo.type)
            InfoRow("Peso:", "${trip.cargo.weight} kg")
            InfoRow("Descripción:", trip.cargo.description)

            trip.cargo.specialRequirements?.let {
                InfoRow("Requisitos especiales:", it)
            }
        }
    }
}

@Composable
private fun LocationsCard(
    trip: Trip
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📍 Ubicaciones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Origen
            Column {
                Text(
                    text = "Origen",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5AC5C5)
                )
                Text(
                    text = trip.origin.getFullAddress(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider()

            // Destino
            Column {
                Text(
                    text = "Destino",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5AC5C5)
                )
                Text(
                    text = trip.destination.getFullAddress(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun StatusHistoryCard(
    history: List<StatusChange>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Header clickeable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (history.isNotEmpty()) {
                            Modifier.clickableWithoutRipple(onClick = onToggle)
                        } else {
                            Modifier
                        }
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Historial de Estados (${history.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (history.isNotEmpty()) {
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF5AC5C5)
                    )
                }
            }

            // Contenido expandible
            if (isExpanded && history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                history.forEach { change ->
                    StatusChangeItem(change)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusChangeItem(
    change: StatusChange
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = change.status.getDisplayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(change.status.getColor())
                ) {
                    Text(
                        text = change.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Text(
                text = "Cambiado por: ${change.changedByName}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Text(
                text = change.getFormattedDate(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Extension para clickeable sin ripple
@Composable
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        )
    )
}