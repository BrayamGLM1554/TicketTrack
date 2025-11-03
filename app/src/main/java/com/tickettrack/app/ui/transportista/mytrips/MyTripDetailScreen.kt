package com.tickettrack.app.ui.transportista.mytrips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.TripStatus
import com.tickettrack.app.domain.model.expense.Expense
import com.tickettrack.app.domain.model.expense.ExpenseCategory
import com.tickettrack.app.domain.model.expense.ModerationStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Pantalla de detalle de un viaje para el transportista.
 *
 * Muestra:
 * - Información completa del viaje
 * - 3 tabs: Detalles, Gastos, Historial
 * - Botones de acción (iniciar, completar, solicitar presupuesto)
 * - Alertas de presupuesto
 *
 * HU relevantes: HU11 (Consultar Detalles), HU13 (Aumentar Presupuesto)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripDetailScreen(
    tripId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRegisterExpense: (String) -> Unit,
    onNavigateToBudgetRequest: (String) -> Unit
) {
    // ✅ CORRECCIÓN: Obtener context primero
    val context = androidx.compose.ui.platform.LocalContext.current

    // ✅ Crear ViewModel con el context ya obtenido
    val viewModel: MyTripDetailViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MyTripDetailViewModel(
                    application = context.applicationContext as android.app.Application,
                    tripId = tripId
                ) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(state.isRefreshing)

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensaje de éxito
    LaunchedEffect(state.statusChangeSuccess) {
        if (state.statusChangeSuccess) {
            snackbarHostState.showSnackbar(
                message = "Estado del viaje actualizado correctamente",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle del Viaje",
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Botones de acción según el estado del viaje
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón: Registrar Gasto (siempre visible si el viaje no está completado)
                if (state.trip?.status != TripStatus.COMPLETED && state.trip?.status != TripStatus.CANCELLED) {
                    FloatingActionButton(
                        onClick = { onNavigateToRegisterExpense(tripId) },
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Registrar Gasto",
                            tint = Color.White
                        )
                    }
                }

                // Botón: Iniciar Viaje
                if (state.canStartTrip()) {
                    FloatingActionButton(
                        onClick = { viewModel.showStartTripDialog() },
                        containerColor = Color(0xFF2196F3)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Iniciar Viaje",
                            tint = Color.White
                        )
                    }
                }

                // Botón: Completar Viaje
                if (state.canCompleteTrip()) {
                    FloatingActionButton(
                        onClick = { viewModel.showCompleteTripDialog() },
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completar Viaje",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshTripDetail() },
            modifier = Modifier.padding(padding)
        ) {
            if (state.isLoadingTrip) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF5AC5C5))
                }
            } else if (state.trip == null) {
                EmptyState(message = "No se pudo cargar el viaje")
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header con información básica
                    TripHeader(trip = state.trip!!)

                    // Alerta de presupuesto
                    if (state.isBudgetAtRisk()) {
                        BudgetAlert(
                            message = "⚠️ Presupuesto en riesgo: ${state.getBudgetUsagePercentage().toInt()}% utilizado",
                            onRequestIncrease = { viewModel.showBudgetRequestDialog() }
                        )
                    }

                    // Tabs
                    TabRow(
                        selectedTabIndex = state.selectedTab,
                        containerColor = Color.White,
                        contentColor = Color(0xFF5AC5C5)
                    ) {
                        Tab(
                            selected = state.selectedTab == 0,
                            onClick = { viewModel.onTabSelected(0) },
                            text = { Text("Detalles") }
                        )
                        Tab(
                            selected = state.selectedTab == 1,
                            onClick = { viewModel.onTabSelected(1) },
                            text = { Text("Gastos (${state.expenses.size})") }
                        )
                        Tab(
                            selected = state.selectedTab == 2,
                            onClick = { viewModel.onTabSelected(2) },
                            text = { Text("Historial") }
                        )
                    }

                    // Contenido según tab seleccionada
                    when (state.selectedTab) {
                        0 -> DetailsTab(trip = state.trip!!)
                        1 -> ExpensesTab(
                            expenses = state.expenses,
                            isLoading = state.isLoadingExpenses,
                            onAddExpense = { onNavigateToRegisterExpense(tripId) }
                        )
                        2 -> HistoryTab(trip = state.trip!!)
                    }
                }
            }
        }
    }

    // Diálogos
    if (state.showStartTripDialog) {
        StartTripDialog(
            onConfirm = {
                viewModel.startTrip()
            },
            onDismiss = { viewModel.dismissStartTripDialog() },
            isLoading = state.isChangingStatus
        )
    }

    if (state.showCompleteTripDialog) {
        CompleteTripDialog(
            onConfirm = {
                viewModel.completeTrip()
            },
            onDismiss = { viewModel.dismissCompleteTripDialog() },
            isLoading = state.isChangingStatus
        )
    }

    if (state.showBudgetRequestDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBudgetRequestDialog() },
            title = { Text("Solicitar Aumento de Presupuesto") },
            text = { Text("Serás redirigido a la pantalla para solicitar un aumento de presupuesto.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissBudgetRequestDialog()
                    onNavigateToBudgetRequest(tripId)
                }) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissBudgetRequestDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Mensaje de error
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

// ==========================================
// COMPONENTES INTERNOS
// ==========================================

@Composable
private fun TripHeader(trip: Trip) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF5AC5C5).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Nombre del viaje y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.cargoName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = trip.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ID: ${trip.id}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ruta
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${trip.origin.city}, ${trip.origin.state}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${trip.destination.city}, ${trip.destination.state}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BudgetAlert(
    message: String,
    onRequestIncrease: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3CD)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF856404),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = Color(0xFF856404),
                    fontSize = 14.sp
                )
            }
            TextButton(onClick = onRequestIncrease) {
                Text("Solicitar", color = Color(0xFF856404))
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TripStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TripStatus.PENDING -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.2f),
            Color(0xFFFF9800),
            "Pendiente"
        )
        TripStatus.IN_PROGRESS -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.2f),
            Color(0xFF2196F3),
            "En Viaje"
        )
        TripStatus.COMPLETED -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.2f),
            Color(0xFF4CAF50),
            "Completado"
        )
        TripStatus.CANCELLED -> Triple(
            Color(0xFFF44336).copy(alpha = 0.2f),
            Color(0xFFF44336),
            "Cancelado"
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

// ==========================================
// TABS
// ==========================================

@Composable
private fun DetailsTab(trip: Trip) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Información de la carga
        item {
            DetailCard(
                title = "Información de la Carga",
                icon = Icons.Default.LocalShipping
            ) {
                DetailRow(label = "Tipo", value = trip.cargo.type)
                DetailRow(label = "Peso", value = "${trip.cargo.weight} kg")
                DetailRow(label = "Descripción", value = trip.cargo.description)
                trip.cargo.specialRequirements?.let {
                    DetailRow(
                        label = "Requisitos Especiales",
                        value = it,
                        isHighlight = true
                    )
                }
            }
        }

        // Origen
        item {
            DetailCard(
                title = "Origen",
                icon = Icons.Default.Place
            ) {
                DetailRow(label = "Dirección", value = trip.origin.address)
                DetailRow(label = "Ciudad", value = trip.origin.city)
                DetailRow(label = "Estado", value = trip.origin.state)
                DetailRow(label = "C.P.", value = trip.origin.zipCode)
            }
        }

        // Destino
        item {
            DetailCard(
                title = "Destino",
                icon = Icons.Default.Place
            ) {
                DetailRow(label = "Dirección", value = trip.destination.address)
                DetailRow(label = "Ciudad", value = trip.destination.city)
                DetailRow(label = "Estado", value = trip.destination.state)
                DetailRow(label = "C.P.", value = trip.destination.zipCode)
            }
        }

        // Presupuesto
        item {
            DetailCard(
                title = "Presupuesto",
                icon = Icons.Default.AccountBalanceWallet
            ) {
                DetailRow(
                    label = "Presupuesto Inicial",
                    value = "$${String.format("%.2f", trip.budget.initial)} ${trip.budget.currency}"
                )
                DetailRow(
                    label = "Presupuesto Actual",
                    value = "$${String.format("%.2f", trip.budget.current)} ${trip.budget.currency}",
                    isHighlight = trip.budget.current != trip.budget.initial
                )
                DetailRow(
                    label = "Gastos Totales",
                    value = "$${String.format("%.2f", trip.totalExpenses)} ${trip.budget.currency}",
                    valueColor = Color(0xFFF44336)
                )
                DetailRow(
                    label = "Disponible",
                    value = "$${String.format("%.2f", trip.remainingBudget)} ${trip.budget.currency}",
                    valueColor = Color(0xFF4CAF50)
                )

                // ✅ CORRECCIÓN:
// Barra de progreso
                Spacer(modifier = Modifier.height(8.dp))
                val usagePercentage = trip.getBudgetUsagePercentage() / 100f

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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${trip.getBudgetUsagePercentage().toInt()}% utilizado",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                // Aumentos de presupuesto
                if (trip.budget.history.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aumentos aprobados: ${trip.budgetIncreaseCount}",
                        fontSize = 12.sp,
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Fechas
        item {
            DetailCard(
                title = "Fechas",
                icon = Icons.Default.DateRange
            ) {
                DetailRow(
                    label = "Creado",
                    value = formatDate(trip.createdAt)
                )
                DetailRow(
                    label = "Última actualización",
                    value = formatDate(trip.updatedAt)
                )
                trip.completedAt?.let {
                    DetailRow(
                        label = "Completado",
                        value = formatDate(it),
                        valueColor = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpensesTab(
    expenses: List<Expense>,
    isLoading: Boolean,
    onAddExpense: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF5AC5C5))
        }
    } else if (expenses.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.Gray.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay gastos registrados",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddExpense,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5AC5C5)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Primer Gasto")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Resumen de gastos
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Resumen de Gastos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total de Gastos",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "$${String.format("%.2f", expenses.sumOf { it.amount })}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5AC5C5)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Número de Gastos",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${expenses.size}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5AC5C5)
                                )
                            }
                        }
                    }
                }
            }

            // Lista de gastos
            items(expenses) { expense ->
                ExpenseCard(expense = expense)
            }
        }
    }
}

@Composable
private fun HistoryTab(trip: Trip) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Historial de estados
        item {
            DetailCard(
                title = "Historial de Estados",
                icon = Icons.Default.History
            ) {
                trip.statusHistory.forEach { statusChange ->
                    StatusHistoryItem(statusChange = statusChange)
                }
            }
        }

        // Historial de aumentos de presupuesto
        if (trip.budget.history.isNotEmpty()) {
            item {
                DetailCard(
                    title = "Aumentos de Presupuesto",
                    icon = Icons.Default.TrendingUp
                ) {
                    trip.budget.history.forEach { increase ->
                        BudgetIncreaseItem(increase = increase)
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES DE DETALLE
// ==========================================

@Composable
private fun DetailCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF5AC5C5).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF5AC5C5),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = valueColor,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ExpenseCard(expense: Expense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de categoría
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(expense.category.getColor()).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = expense.category.getIcon(),
                    contentDescription = null,
                    tint = Color(expense.category.getColor()),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 2
                )
                Text(
                    text = expense.category.displayName,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = formatDate(expense.date),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Monto
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF5AC5C5)
                )
                // Badge de estado de moderación
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (expense.ticketModerationStatus) {
                        ModerationStatus.APPROVED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ModerationStatus.PENDING -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        ModerationStatus.REJECTED -> Color(0xFFF44336).copy(alpha = 0.1f)
                        ModerationStatus.FLAGGED -> Color(0xFFF44336).copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = when (expense.ticketModerationStatus) {
                            ModerationStatus.APPROVED -> "✓"
                            ModerationStatus.PENDING -> "⏳"
                            ModerationStatus.REJECTED -> "✗"
                            ModerationStatus.FLAGGED -> "⚠"
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusHistoryItem(statusChange: com.tickettrack.app.domain.model.trip.StatusChange) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Indicador de color
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(statusChange.status.getColor()))
                .align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (statusChange.status) {
                    TripStatus.PENDING -> "Viaje Creado"
                    TripStatus.IN_PROGRESS -> "Viaje Iniciado"
                    TripStatus.COMPLETED -> "Viaje Completado"
                    TripStatus.CANCELLED -> "Viaje Cancelado"
                },
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = "Por: ${statusChange.changedByName}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = formatDate(statusChange.changedAt),
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun BudgetIncreaseItem(increase: com.tickettrack.app.domain.model.trip.BudgetIncrease) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD1ECF1)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Aumento de Presupuesto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0C5460)
                )
                Text(
                    text = "+$${String.format("%.2f", increase.increase)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0C5460)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "De $${String.format("%.2f", increase.previousAmount)} a $${String.format("%.2f", increase.newAmount)}",
                fontSize = 12.sp,
                color = Color(0xFF0C5460)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Motivo: ${increase.reason}",
                fontSize = 12.sp,
                color = Color(0xFF0C5460)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Aprobado por: ${increase.approvedByName}",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                text = formatDate(increase.approvedAt),
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

// ==========================================
// DIÁLOGOS
// ==========================================

@Composable
private fun StartTripDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Iniciar Viaje",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas iniciar este viaje?\n\n" +
                        "Al iniciar, podrás registrar gastos y el viaje cambiará a estado 'En Viaje'."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar Viaje")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun CompleteTripDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Completar Viaje",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas completar este viaje?\n\n" +
                        "Esta acción marcará el viaje como finalizado y no podrás agregar más gastos."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Completar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

// ==========================================
// UTILIDADES
// ==========================================

private fun formatDate(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val formatter = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        isoDate
    }
}

// Extensiones para íconos y colores
private fun ExpenseCategory.getIcon(): androidx.compose.ui.graphics.vector.ImageVector {
    return when (this) {
        ExpenseCategory.FUEL -> Icons.Default.LocalGasStation
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TOLL -> Icons.Default.Toll
        ExpenseCategory.MAINTENANCE -> Icons.Default.Build
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
    }
}

private fun ExpenseCategory.getColor(): Long {
    return when (this) {
        ExpenseCategory.FUEL -> 0xFF2196F3
        ExpenseCategory.FOOD -> 0xFFFF9800
        ExpenseCategory.TOLL -> 0xFF9C27B0
        ExpenseCategory.MAINTENANCE -> 0xFFF44336
        ExpenseCategory.OTHER -> 0xFF607D8B
    }
}