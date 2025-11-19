package com.tickettrack.app.ui.dashboard

import androidx.compose.foundation.background
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
import com.tickettrack.app.domain.model.DashboardResponse
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    token: String,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPeriodMenu by remember { mutableStateOf(false) }

    LaunchedEffect(token) {
        viewModel.loadDashboard(token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header con selector de período
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Primary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dashboard",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Resumen general de la empresa",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // Selector de período
                Box {
                    FilterChip(
                        selected = false,
                        onClick = { showPeriodMenu = true },
                        label = {
                            Text(
                                text = getPeriodLabel(uiState.selectedPeriod),
                                color = Primary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Primary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White
                        )
                    )

                    DropdownMenu(
                        expanded = showPeriodMenu,
                        onDismissRequest = { showPeriodMenu = false }
                    ) {
                        listOf("today", "week", "month", "year").forEach { period ->
                            DropdownMenuItem(
                                text = { Text(getPeriodLabel(period)) },
                                onClick = {
                                    viewModel.changePeriod(token, period)
                                    showPeriodMenu = false
                                }
                            )
                        }
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
                            text = "Error del servidor",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "El servidor está teniendo problemas. Por favor, inténtalo más tarde.",
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDashboard(token) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            uiState.dashboard != null -> {
                DashboardContent(dashboard = uiState.dashboard!!)
            }
        }
    }
}

@Composable
fun DashboardContent(dashboard: DashboardResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Resumen Financiero
        FinancialOverviewCard(overview = dashboard.overview)

        // Stats Row: Viajes y Transportistas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                title = "Viajes Activos",
                value = dashboard.trips.active.toString(),
                icon = Icons.Default.DirectionsCar,
                color = Color(0xFF42A5F5),
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Transportistas",
                value = dashboard.drivers.active.toString(),
                icon = Icons.Default.People,
                color = Color(0xFF66BB6A),
                modifier = Modifier.weight(1f)
            )
        }

        // Gastos por Categoría
        ExpensesByCategoryCard(expenses = dashboard.expenses)

        // Estado de Viajes
        TripsStatusCard(trips = dashboard.trips)

        // Alertas
        if (dashboard.alerts.isNotEmpty()) {
            AlertsCard(alerts = dashboard.alerts)
        }

        // Actividad Reciente
        if (dashboard.recentActivity.isNotEmpty()) {
            RecentActivityCard(activities = dashboard.recentActivity)
        }
    }
}

@Composable
fun FinancialOverviewCard(overview: com.tickettrack.app.domain.model.OverviewData) {
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
                text = "Resumen Financiero",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Presupuesto Total
            FinancialRow(
                label = "Presupuesto Total",
                amount = overview.totalBudget,
                icon = Icons.Default.AccountBalance,
                color = Color(0xFF42A5F5)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Gastado
            FinancialRow(
                label = "Total Gastado",
                amount = overview.totalSpent,
                icon = Icons.Default.MonetizationOn,
                color = Color(0xFFFF9800)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Disponible
            FinancialRow(
                label = "Disponible",
                amount = overview.availableBalance,
                icon = Icons.Default.Savings,
                color = Color(0xFF66BB6A)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progreso
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Uso del presupuesto",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${String.format("%.1f", overview.budgetUsedPercentage)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = getProgressColor(overview.budgetUsedPercentage)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = (overview.budgetUsedPercentage / 100).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = getProgressColor(overview.budgetUsedPercentage),
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }
    }
}

@Composable
fun FinancialRow(
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${String.format("%,.2f", amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ExpensesByCategoryCard(expenses: com.tickettrack.app.domain.model.ExpensesData) {
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
                    text = "Gastos por Categoría",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "$${String.format("%,.2f", expenses.totalAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            expenses.byCategory.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                CategoryRow(
                    category = category,
                    amount = amount,
                    percentage = (amount / expenses.totalAmount * 100).toFloat()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun CategoryRow(
    category: String,
    amount: Double,
    percentage: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.replaceFirstChar { it.uppercase() },
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "$${String.format("%,.2f", amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = percentage / 100,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = getCategoryColor(category),
            trackColor = Color(0xFFE0E0E0)
        )
    }
}

@Composable
fun TripsStatusCard(trips: com.tickettrack.app.domain.model.TripsData) {
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
                text = "Estado de Viajes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TripStatusItem(
                    label = "Pendientes",
                    count = trips.pending,
                    color = Color(0xFFFFA726)
                )
                TripStatusItem(
                    label = "En Progreso",
                    count = trips.inProgress,
                    color = Color(0xFF42A5F5)
                )
                TripStatusItem(
                    label = "Completados",
                    count = trips.completed,
                    color = Color(0xFF66BB6A)
                )
            }
        }
    }
}

@Composable
fun TripStatusItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun AlertsCard(alerts: List<com.tickettrack.app.domain.model.AlertItem>) {
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
                    text = "Alertas Activas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Badge(
                    containerColor = Color.Red
                ) {
                    Text(
                        text = alerts.size.toString(),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            alerts.take(3).forEach { alert ->
                AlertItem(alert = alert)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AlertItem(alert: com.tickettrack.app.domain.model.AlertItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = getSeverityColor(alert.severity).copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getSeverityIcon(alert.severity),
                contentDescription = null,
                tint = getSeverityColor(alert.severity),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = alert.message,
                fontSize = 13.sp,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RecentActivityCard(activities: List<com.tickettrack.app.domain.model.ActivityItem>) {
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
                text = "Actividad Reciente",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            activities.take(5).forEach { activity ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = getActivityIcon(activity.type),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activity.description,
                            fontSize = 13.sp,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = formatTimestamp(activity.timestamp),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// HELPER FUNCTIONS
// ============================================

fun getPeriodLabel(period: String): String {
    return when (period) {
        "today" -> "Hoy"
        "week" -> "Esta Semana"
        "month" -> "Este Mes"
        "year" -> "Este Año"
        else -> period
    }
}

fun getProgressColor(percentage: Double): Color {
    return when {
        percentage < 50 -> Color(0xFF66BB6A) // Verde
        percentage < 80 -> Color(0xFFFFA726) // Naranja
        else -> Color(0xFFEF5350) // Rojo
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "fuel" -> Color(0xFFEF5350)
        "food" -> Color(0xFF42A5F5)
        "tolls" -> Color(0xFFAB47BC)
        "maintenance" -> Color(0xFFFF9800)
        "lodging" -> Color(0xFF66BB6A)
        else -> Color(0xFF757575)
    }
}

fun getSeverityColor(severity: String): Color {
    return when (severity.lowercase()) {
        "low" -> Color(0xFF66BB6A)
        "medium" -> Color(0xFFFFA726)
        "high" -> Color(0xFFFF5722)
        "critical" -> Color(0xFFD32F2F)
        else -> Color(0xFF757575)
    }
}

fun getSeverityIcon(severity: String): ImageVector {
    return when (severity.lowercase()) {
        "low" -> Icons.Default.Info
        "medium" -> Icons.Default.Warning
        "high", "critical" -> Icons.Default.Error
        else -> Icons.Default.Info
    }
}

fun getActivityIcon(type: String): ImageVector {
    return when (type.lowercase()) {
        "trip" -> Icons.Default.DirectionsCar
        "expense" -> Icons.Default.Receipt
        "driver" -> Icons.Default.Person
        else -> Icons.Default.CheckCircle
    }
}

fun formatTimestamp(timestamp: String): String {
    // Implementar formateo de fecha según necesites
    return timestamp
}
