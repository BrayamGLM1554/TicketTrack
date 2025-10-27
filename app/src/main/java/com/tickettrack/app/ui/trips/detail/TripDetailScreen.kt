package com.tickettrack.app.ui.trips.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.ui.trips.detail.components.BudgetSection
import com.tickettrack.app.ui.trips.detail.components.DriverSection
import com.tickettrack.app.ui.trips.detail.components.IncreaseBudgetDialog

/**
 * Pantalla de detalle de viaje.
 *
 * Muestra:
 * - Información del viaje (carga, origen, destino)
 * - Presupuesto con historial expandible
 * - Transportista asignado
 * - Botones de acción (aumentar presupuesto, cambiar estado)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    onNavigateBack: () -> Unit,
    currentUserId: String,
    currentUserName: String,
    currentUserRole: String,
    viewModel: TripDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Cargar viaje al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadTrip(tripId, currentUserId, currentUserName, currentUserRole)
    }

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
            onConfirm = viewModel::increaseBudget,
            onDismiss = viewModel::hideIncreaseBudgetDialog
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de Viaje",
                        fontWeight = FontWeight.Bold
                    )
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
            when {
                state.isLoadingTrip -> {
                    // Loading
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF5AC5C5)
                    )
                }
                state.trip != null -> {
                    // Contenido principal
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val trip = state.trip!!

                        // Información del Viaje
                        TripInfoCard(trip = trip)

                        // Presupuesto
                        BudgetSection(
                            budget = trip.budget,
                            totalExpenses = trip.totalExpenses,
                            remainingBudget = trip.remainingBudget,
                            isHistoryExpanded = state.isBudgetHistoryExpanded,
                            canIncreaseBudget = state.canIncreaseBudget() && trip.canIncreaseBudget(),
                            onToggleHistory = viewModel::toggleBudgetHistory,
                            onIncreaseBudget = viewModel::showIncreaseBudgetDialog
                        )

                        // Transportista
                        DriverSection(
                            driver = state.driver,
                            isLoading = state.isLoadingDriver
                        )

                        // Botones de acción
                        if (state.canChangeStatus()) {
                            ActionsSection(
                                trip = trip,
                                isChangingStatus = state.isChangingStatus,
                                onStartTrip = viewModel::startTrip,
                                onCompleteTrip = viewModel::completeTrip,
                                onCancelTrip = viewModel::cancelTrip
                            )
                        }

                        // Espaciador final
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                else -> {
                    // Sin datos
                    Text(
                        text = "No se pudo cargar el viaje",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }

            // Snackbar de éxito al cambiar estado
            if (state.statusChangeSuccess) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estado actualizado exitosamente",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripInfoCard(
    trip: Trip,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con nombre y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.cargoName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(status = trip.status.getDisplayName(), icon = trip.getStatusIcon())
            }

            Divider()

            // Carga
            SectionTitle("Información de la Carga")
            InfoRow("Tipo", trip.cargo.type)
            InfoRow("Peso", trip.cargo.getWeightInTonsFormatted())
            InfoRow("Descripción", trip.cargo.description)
            if (!trip.cargo.specialRequirements.isNullOrBlank()) {
                InfoRow("Requisitos", trip.cargo.specialRequirements!!)
            }

            Divider()

            // Origen y Destino
            SectionTitle("Ruta")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📍 Origen",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5AC5C5),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = trip.origin.city,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = trip.origin.state,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "→",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Gray
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "🏁 Destino",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5AC5C5),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = trip.destination.city,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = trip.destination.state,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionsSection(
    trip: Trip,
    isChangingStatus: Boolean,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit,
    onCancelTrip: () -> Unit,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Acciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            when {
                trip.canStart() -> {
                    Button(
                        onClick = onStartTrip,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChangingStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("Iniciar Viaje")
                    }
                }
                trip.canFinalize() -> {
                    Button(
                        onClick = onCompleteTrip,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChangingStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Finalizar Viaje")
                    }
                }
            }

            if (trip.canCancel()) {
                OutlinedButton(
                    onClick = onCancelTrip,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isChangingStatus,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Cancelar Viaje")
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, icon: String) {
    Surface(
        color = Color(0xFFE3F2FD),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF5AC5C5)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
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
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}