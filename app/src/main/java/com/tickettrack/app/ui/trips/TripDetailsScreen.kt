package com.tickettrack.app.ui.trips

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.domain.model.Expense
import com.tickettrack.app.domain.model.ExpenseCategory
import com.tickettrack.app.domain.model.Trip
import com.tickettrack.app.domain.model.TripStatus
import com.tickettrack.app.ui.expenses.getCategoryColor
import com.tickettrack.app.ui.expenses.getCategoryLabel
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import com.tickettrack.app.utils.JwtDecoder
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    token: String,
    tripId: String,
    onBackPressed: () -> Unit,
    onNavigateToExpenseDetails: (String) -> Unit,
    viewModel: TripDetailsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showStartDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    // Extraer rol del token
    val userRole = remember(token) {
        val role = JwtDecoder.extractRole(token) ?: "USER"
        Log.d("TripDetailsScreen", "Rol extraído del token: $role")
        role
    }

    LaunchedEffect(tripId) {
        viewModel.loadTripDetails(token, tripId)
    }

    LaunchedEffect(uiState.statusUpdateSuccess) {
        if (uiState.statusUpdateSuccess) {
            viewModel.clearStatusUpdateState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Viaje") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
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
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
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
                                onClick = { viewModel.loadTripDetails(token, tripId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                uiState.trip != null -> {
                    TripDetailsContent(
                        trip = uiState.trip!!,
                        expenses = uiState.expenses,
                        isLoadingExpenses = uiState.isLoadingExpenses,
                        expensesError = uiState.expensesError,
                        onExpenseClick = onNavigateToExpenseDetails,
                        onStartTrip = { showStartDialog = true },
                        onCompleteTrip = { showCompleteDialog = true },
                        isUpdatingStatus = uiState.isUpdatingStatus,
                        userRole = userRole
                    )
                }
            }

            // Diálogo de confirmación para INICIAR viaje (solo Admin)
            if (showStartDialog) {
                AlertDialog(
                    onDismissRequest = { showStartDialog = false },
                    title = { Text("Iniciar Viaje") },
                    text = {
                        Text("¿Estás seguro de que deseas iniciar este viaje? El estado cambiará a 'En Progreso'.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.startTrip(token, tripId)
                                showStartDialog = false
                            },
                            enabled = !uiState.isUpdatingStatus
                        ) {
                            if (uiState.isUpdatingStatus) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Primary
                                )
                            } else {
                                Text("Iniciar")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showStartDialog = false },
                            enabled = !uiState.isUpdatingStatus
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo de confirmación para COMPLETAR viaje (solo ADMIN)
            if (showCompleteDialog) {
                AlertDialog(
                    onDismissRequest = { showCompleteDialog = false },
                    title = { Text("Completar Viaje") },
                    text = {
                        Text("¿Estás seguro de que deseas completar este viaje? El estado cambiará a 'Completado' y no podrás modificarlo después.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.completeTrip(token, tripId)
                                showCompleteDialog = false
                            },
                            enabled = !uiState.isUpdatingStatus
                        ) {
                            if (uiState.isUpdatingStatus) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF66BB6A)
                                )
                            } else {
                                Text("Completar", color = Color(0xFF66BB6A))
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCompleteDialog = false },
                            enabled = !uiState.isUpdatingStatus
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Snackbar para mostrar error de actualización
            uiState.statusUpdateError?.let { error ->
                LaunchedEffect(error) {
                    delay(3000)
                    viewModel.clearStatusUpdateState()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripDetailsContent(
    trip: Trip,
    expenses: List<Expense>,
    isLoadingExpenses: Boolean,
    expensesError: String?,
    onExpenseClick: (String) -> Unit,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit,
    isUpdatingStatus: Boolean,
    userRole: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card de Estado y Nombre con botón de acción
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trip.tripName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(status = trip.status)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de acción SOLO para USER
                if (userRole == "ADMIN") {
                    when (trip.status) {
                        TripStatus.PENDING -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onStartTrip,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF42A5F5)
                                ),
                                enabled = !isUpdatingStatus,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isUpdatingStatus) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (isUpdatingStatus) "Iniciando..." else "Iniciar Viaje",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        TripStatus.IN_PROGRESS -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onCompleteTrip,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF66BB6A)
                                ),
                                enabled = !isUpdatingStatus,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isUpdatingStatus) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (isUpdatingStatus) "Completando..." else "Completar Viaje",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        TripStatus.COMPLETED -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF66BB6A).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF66BB6A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Viaje Completado",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF66BB6A)
                                    )
                                }
                            }
                        }
                        null -> { /* No mostrar nada */ }
                    }
                } else {
                    // Para USER solo mostrar el estado si está completado
                    if (trip.status == TripStatus.COMPLETED) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF66BB6A).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF66BB6A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Viaje Completado",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF66BB6A)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card de Ubicaciones
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ruta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Origen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Origen",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = trip.origin,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A),
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Línea divisoria
                Row(
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .background(
                                color = TextSecondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(0.dp))

                // Destino
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Destino",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = trip.destination,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Card de Carga
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Carga",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = trip.cargo,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 20.sp
                )
            }
        }

        // Card de Transportista y Presupuesto
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Información del Viaje",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Transportista
                DetailRow(
                    icon = Icons.Default.Person,
                    label = "Transportista",
                    value = trip.transportistaName,
                    iconTint = Primary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Presupuesto
                DetailRow(
                    icon = Icons.Default.AttachMoney,
                    label = "Presupuesto Asignado",
                    value = "$${String.format("%.2f", trip.budgetAssigned)}",
                    iconTint = Color(0xFF4CAF50),
                    valueColor = Primary,
                    valueFontWeight = FontWeight.Bold
                )
            }
        }

        // Card de Información Adicional
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Información Adicional",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fecha de creación
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha de Creación",
                    value = formatDate(trip.createdAt),
                    iconTint = Color(0xFF42A5F5)
                )
            }
        }

        // Card de Gastos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gastos del Viaje",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    if (expenses.isNotEmpty()) {
                        Text(
                            text = "$${String.format("%.2f", expenses.sumOf { it.amount })}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isLoadingExpenses -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    expensesError != null -> {
                        Text(
                            text = expensesError,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }

                    expenses.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = TextSecondary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay gastos registrados",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            expenses.forEach { expense ->
                                ExpenseItemCard(
                                    expense = expense,
                                    onClick = { onExpenseClick(expense.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = getCategoryColor(expense.category).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(expense.category),
                        contentDescription = null,
                        tint = getCategoryColor(expense.category),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = getCategoryLabel(expense.category),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = expense.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", expense.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = formatDate(expense.createdAt),
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

fun getCategoryIcon(category: ExpenseCategory): ImageVector {
    return when (category) {
        ExpenseCategory.FUEL -> Icons.Default.LocalGasStation
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TOLL -> Icons.Default.Toll
        ExpenseCategory.MAINTENANCE -> Icons.Default.Build
        ExpenseCategory.PARKING -> Icons.Default.LocalParking
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String?,
    iconTint: Color = Primary,
    valueColor: Color = Color(0xFF1A1A1A),
    valueFontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value ?: "No disponible",
                fontSize = 14.sp,
                fontWeight = valueFontWeight,
                color = valueColor,
                lineHeight = 20.sp
            )
        }
    }
}

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "No disponible"

    return try {
        val zonedDateTime = ZonedDateTime.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("es", "MX"))
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        "Fecha inválida"
    }
}