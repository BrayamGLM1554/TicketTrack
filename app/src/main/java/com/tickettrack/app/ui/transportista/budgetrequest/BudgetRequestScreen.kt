package com.tickettrack.app.ui.transportista.budgetrequest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.Urgency

/**
 * Pantalla para solicitar aumento de presupuesto.
 *
 * Permite al transportista:
 * - Seleccionar el viaje
 * - Ingresar monto solicitado
 * - Justificar la razón
 * - Seleccionar urgencia
 * - Enviar solicitud
 *
 * HU relevantes: HU13 (Aumentar Presupuesto), HU20 (Solicitudes)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetRequestScreen(
    tripId: String? = null,
    onNavigateBack: () -> Unit,
    onRequestSubmitted: () -> Unit
) {
    // ✅ CORRECCIÓN: Obtener el context primero dentro del @Composable
    val context = LocalContext.current

    // ✅ Crear el ViewModel con el context ya obtenido
    val viewModel: BudgetRequestViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BudgetRequestViewModel(
                    application = context.applicationContext as android.app.Application,
                    preselectedTripId = tripId
                ) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navegar de vuelta cuando la solicitud se envíe exitosamente
    LaunchedEffect(state.submissionSuccess) {
        if (state.submissionSuccess) {
            snackbarHostState.showSnackbar(
                message = "Solicitud enviada correctamente",
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(1500)
            onRequestSubmitted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Solicitar Aumento",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5AC5C5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (state.isLoadingTrips) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF5AC5C5))
            }
        } else if (state.availableTrips.isEmpty()) {
            EmptyState(
                message = "No tienes viajes activos para solicitar aumento de presupuesto.",
                onNavigateBack = onNavigateBack
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información
                InfoCard()

                // Selector de viaje
                TripSelector(
                    selectedTrip = state.selectedTrip,
                    availableTrips = state.availableTrips,
                    onTripSelected = { viewModel.onTripSelected(it) },
                    error = state.tripError
                )

                // Mostrar información del viaje seleccionado
                state.selectedTrip?.let { trip ->
                    TripInfoCard(trip = trip)
                }

                // Campo: Presupuesto Actual (readonly)
                if (state.selectedTrip != null) {
                    OutlinedTextField(
                        value = "$${String.format("%.2f", state.currentBudget)} MXN",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Presupuesto Actual") },
                        readOnly = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Gray,
                            disabledLeadingIconColor = Color.Gray,
                            disabledTextColor = Color.Black
                        )
                    )
                }

                // Campo: Monto Solicitado
                OutlinedTextField(
                    value = state.requestedBudget,
                    onValueChange = { viewModel.onRequestedBudgetChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Monto Solicitado *") },
                    placeholder = { Text("Ej: 20000.00") },
                    leadingIcon = {
                        Icon(Icons.Default.TrendingUp, contentDescription = null)
                    },
                    trailingIcon = {
                        Text(
                            text = "MXN",
                            modifier = Modifier.padding(end = 12.dp),
                            color = Color.Gray
                        )
                    },
                    isError = state.requestedBudgetError != null,
                    supportingText = {
                        state.requestedBudgetError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5AC5C5),
                        focusedLabelColor = Color(0xFF5AC5C5)
                    )
                )

                // Mostrar cálculo del aumento
                if (state.isIncreaseValid()) {
                    IncreaseCalculationCard(
                        increaseAmount = state.calculateIncrease(),
                        percentage = state.getIncreasePercentage()
                    )
                }

                // Campo: Razón/Justificación
                OutlinedTextField(
                    value = state.reason,
                    onValueChange = { viewModel.onReasonChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    label = { Text("Razón de la Solicitud *") },
                    placeholder = { Text("Explica detalladamente por qué necesitas este aumento...") },
                    isError = state.reasonError != null,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = state.reasonError ?: "Mínimo 20 caracteres",
                                color = if (state.reasonError != null) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    Color.Gray
                                }
                            )
                            Text(
                                text = "${state.reason.length}/500",
                                color = Color.Gray
                            )
                        }
                    },
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5AC5C5),
                        focusedLabelColor = Color(0xFF5AC5C5)
                    )
                )

                // Selector de urgencia
                UrgencySelector(
                    selectedUrgency = state.urgency,
                    onUrgencySelected = { viewModel.onUrgencyChanged(it) }
                )

                // Botón enviar
                Button(
                    onClick = { viewModel.submitRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isSubmitting && state.isFormValid(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5AC5C5),
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviando...")
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviar Solicitud",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Nota final
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tu solicitud será revisada por un administrador. Recibirás una notificación cuando sea aprobada o rechazada.",
                            fontSize = 13.sp,
                            color = Color(0xFF1976D2)
                        )
                    }
                }

                // Espacio final
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Mostrar error si existe
        state.errorMessage?.let { error ->
            LaunchedEffect(error) {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearError()
            }
        }
    }
}

// ==========================================
// COMPONENTES INTERNOS
// ==========================================

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3CD)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFF856404),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Importante",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF856404)
                )
                Text(
                    text = "Solicita aumentos solo cuando sea necesario. Justifica bien tu solicitud para mayor probabilidad de aprobación.",
                    fontSize = 13.sp,
                    color = Color(0xFF856404)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripSelector(
    selectedTrip: Trip?,
    availableTrips: List<Trip>,
    onTripSelected: (Trip) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Selecciona el Viaje *",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedTrip?.cargoName ?: "Seleccionar viaje",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                leadingIcon = {
                    Icon(Icons.Default.DirectionsBus, contentDescription = null)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                isError = error != null,
                supportingText = {
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5AC5C5),
                    focusedLabelColor = Color(0xFF5AC5C5)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableTrips.forEach { trip ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = trip.cargoName,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${trip.origin.city} → ${trip.destination.city}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Presupuesto: $${String.format("%.0f", trip.budget.current)}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        },
                        onClick = {
                            onTripSelected(trip)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TripInfoCard(trip: Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Información del Viaje",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            InfoRow(label = "ID", value = trip.id)
            InfoRow(label = "Origen", value = "${trip.origin.city}, ${trip.origin.state}")
            InfoRow(label = "Destino", value = "${trip.destination.city}, ${trip.destination.state}")
            InfoRow(label = "Presupuesto Actual", value = "$${String.format("%.2f", trip.budget.current)} MXN")
            InfoRow(label = "Gastado", value = "$${String.format("%.2f", trip.totalExpenses)} MXN")
            InfoRow(label = "Disponible", value = "$${String.format("%.2f", trip.remainingBudget)} MXN")

            // Barra de progreso
            Spacer(modifier = Modifier.height(8.dp))
            val usagePercentage = trip.getBudgetUsagePercentage() / 100.0

            LinearProgressIndicator(
                progress = { usagePercentage.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    usagePercentage >= 0.9 -> Color(0xFFF44336)
                    usagePercentage >= 0.7 -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                },
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            Text(
                text = "${trip.getBudgetUsagePercentage().toInt()}% utilizado",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Aumentos previos
            if (trip.budgetIncreaseCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Este viaje ya tiene ${trip.budgetIncreaseCount} aumento(s) aprobado(s)",
                    fontSize = 12.sp,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun IncreaseCalculationCard(
    increaseAmount: Double,
    percentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Cálculo del Aumento",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Aumento",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "+$${String.format("%.2f", increaseAmount)} MXN",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${percentage.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UrgencySelector(
    selectedUrgency: Urgency,
    onUrgencySelected: (Urgency) -> Unit
) {
    Column {
        Text(
            text = "Nivel de Urgencia *",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UrgencyChip(
                urgency = Urgency.LOW,
                isSelected = selectedUrgency == Urgency.LOW,
                onClick = { onUrgencySelected(Urgency.LOW) },
                modifier = Modifier.weight(1f)
            )
            UrgencyChip(
                urgency = Urgency.MEDIUM,
                isSelected = selectedUrgency == Urgency.MEDIUM,
                onClick = { onUrgencySelected(Urgency.MEDIUM) },
                modifier = Modifier.weight(1f)
            )
            UrgencyChip(
                urgency = Urgency.HIGH,
                isSelected = selectedUrgency == Urgency.HIGH,
                onClick = { onUrgencySelected(Urgency.HIGH) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UrgencyChip(
    urgency: Urgency,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (urgency) {
        Urgency.LOW -> Pair(Color(0xFF4CAF50), "Baja")
        Urgency.MEDIUM -> Pair(Color(0xFFFF9800), "Media")
        Urgency.HIGH -> Pair(Color(0xFFF44336), "Alta")
        Urgency.CRITICAL -> Pair(Color(0xFF9C27B0), "Crítica")
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color
        ),
        border = FilterChipDefaults.filterChipBorder(
            selected = isSelected,
            enabled = true,
            borderColor = if (isSelected) color else Color.Gray,
            selectedBorderColor = color,
            borderWidth = 2.dp
        )
    )
}

@Composable
private fun EmptyState(
    message: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
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
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5AC5C5)
                )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Volver")
            }
        }
    }
}