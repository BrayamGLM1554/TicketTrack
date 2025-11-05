package com.tickettrack.app.ui.transportista

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.TripStatus
import com.tickettrack.app.domain.model.expense.Expense
import com.tickettrack.app.domain.model.expense.ExpenseCategory

/**
 * Dashboard principal del Transportista (USER).
 *
 * Muestra:
 * - Cards con resumen de viajes y presupuesto
 * - Viajes recientes asignados
 * - Gastos recientes registrados
 * - Alertas de presupuesto
 * - Accesos rápidos
 *
 * HU relevantes: Parte del ecosistema de HU09-HU21
 */
@Composable
fun TransportistaDashboardScreen(
    viewModel: TransportistaDashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(state.isRefreshing)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refreshDashboard() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con nombre del usuario
            item {
                DashboardHeader(userName = state.userName)
            }

            // Alerta de presupuesto bajo
            if (state.hasLowBudgetAlert) {
                item {
                    BudgetAlert(
                        message = "⚠️ Atención: Has utilizado el ${state.budgetUsagePercentage.toInt()}% del presupuesto asignado",
                        isWarning = true
                    )
                }
            }

            // Cards de resumen
            item {
                SummaryCards(
                    tripsInProgress = state.tripsInProgress,
                    tripsPending = state.tripsPending,
                    totalExpenses = state.totalExpenses,
                    remainingBudget = state.remainingBudget
                )
            }

            // Barra de presupuesto
            item {
                BudgetProgressCard(
                    totalBudget = state.totalBudgetAssigned,
                    usedBudget = state.totalExpenses,
                    percentage = state.budgetUsagePercentage
                )
            }

            // Viajes recientes
            if (state.recentTrips.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Mis Viajes Recientes",
                        icon = Icons.Default.DirectionsBus
                    )
                }

                items(state.recentTrips) { trip ->
                    TripMiniCard(trip = trip)
                }
            }

            // Gastos recientes
            if (state.recentExpenses.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Gastos Recientes",
                        icon = Icons.Default.AttachMoney
                    )
                }

                items(state.recentExpenses) { expense ->
                    ExpenseMiniCard(expense = expense)
                }
            }

            // Mensaje si está cargando
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF5AC5C5)
                        )
                    }
                }
            }

            // Mensaje de error
            state.errorMessage?.let { error ->
                item {
                    ErrorCard(
                        message = error,
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES INTERNOS
// ==========================================

@Composable
private fun DashboardHeader(userName: String) {
    Column {
        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF187083)
        )
    }
}

@Composable
private fun BudgetAlert(
    message: String,
    isWarning: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) Color(0xFFFFF3CD) else Color(0xFFD1ECF1)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.Info,
                contentDescription = null,
                tint = if (isWarning) Color(0xFF856404) else Color(0xFF0C5460),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = if (isWarning) Color(0xFF856404) else Color(0xFF0C5460),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SummaryCards(
    tripsInProgress: Int,
    tripsPending: Int,
    totalExpenses: Double,
    remainingBudget: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "En Viaje",
                value = tripsInProgress.toString(),
                icon = Icons.Default.DirectionsBus,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Pendientes",
                value = tripsPending.toString(),
                icon = Icons.Default.Schedule,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Gastado",
                value = "$${String.format("%.0f", totalExpenses)}",
                icon = Icons.Default.AttachMoney,
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Disponible",
                value = "$${String.format("%.0f", remainingBudget)}",
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun BudgetProgressCard(
    totalBudget: Double,
    usedBudget: Double,
    percentage: Float
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Uso de Presupuesto",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${percentage.toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = when {
                        percentage >= 90 -> Color(0xFFF44336)
                        percentage >= 70 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    },
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = (percentage / 100).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    percentage >= 90 -> Color(0xFFF44336)
                    percentage >= 70 -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                },
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gastado: $${String.format("%.2f", usedBudget)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Total: $${String.format("%.2f", totalBudget)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF187083),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF187083)
        )
    }
}

@Composable
private fun TripMiniCard(trip: Trip) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navegar al detalle */ },
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
            // Estado
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(trip.status.getColor()).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = trip.status.getIcon(),
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.cargoName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${trip.origin.city} → ${trip.destination.city}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Badge de estado
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(trip.status.getColor()).copy(alpha = 0.1f)
            ) {
                Text(
                    text = when (trip.status) {
                        TripStatus.PENDING -> "Pendiente"
                        TripStatus.IN_PROGRESS -> "En Viaje"
                        TripStatus.COMPLETED -> "Completado"
                        TripStatus.CANCELLED -> "Cancelado"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(trip.status.getColor())
                )
            }
        }
    }
}

@Composable
private fun ExpenseMiniCard(expense: Expense) {
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(expense.category.getColor()).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = expense.category.getIcon(),
                    contentDescription = null,
                    tint = Color(expense.category.getColor()),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = expense.category.displayName,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Monto
            Text(
                text = "$${String.format("%.2f", expense.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF187083)
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8D7DA)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color(0xFF721C24),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = Color(0xFF721C24),
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color(0xFF721C24)
                )
            }
        }
    }
}

// Extensiones para obtener iconos de categorías
private fun ExpenseCategory.getIcon(): ImageVector {
    return when (this) {
        ExpenseCategory.FUEL -> Icons.Default.LocalGasStation
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TOLL -> Icons.Default.Toll
        ExpenseCategory.MAINTENANCE -> Icons.Default.Build
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
    }
}