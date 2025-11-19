package com.tickettrack.app.ui.expenses

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.domain.model.BudgetRequest
import com.tickettrack.app.domain.model.ExpenseCategory
import com.tickettrack.app.domain.model.ExpensesByCategory
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    token: String,
    onNavigateToBudgetRequestDetails: (String) -> Unit,
    viewModel: ExpensesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData(token, forceRefresh = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gastos y Presupuestos")
                        // Mostrar si está usando caché
                        if (uiState.lastRefreshTime > 0 && !uiState.isLoading) {
                            val secondsAgo = (System.currentTimeMillis() - uiState.lastRefreshTime) / 1000
                            if (secondsAgo < 60) {
                                Text(
                                    text = "Actualizado hace ${secondsAgo}s",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshData(token) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = if (uiState.isLoading) Color.Gray else Color(0xFF1A1A1A)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            if (uiState.isLoading && uiState.expenses.isEmpty()) {
                // Mostrar loading solo si no hay datos previos
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando gastos...", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total de gastos
                    item {
                        TotalExpensesCard(totalExpenses = uiState.totalExpenses)
                    }

                    // Gráfica de pastel
                    if (uiState.expensesByCategory.isNotEmpty()) {
                        item {
                            ExpensesPieChart(expensesByCategory = uiState.expensesByCategory)
                        }
                    }

                    // Solicitudes de presupuesto pendientes
                    item {
                        Text(
                            text = "Solicitudes de Aumento",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    when {
                        uiState.isLoadingBudgetRequests -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Primary)
                                }
                            }
                        }

                        uiState.budgetRequestsError != null -> {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFEBEE)
                                    )
                                ) {
                                    Text(
                                        text = uiState.budgetRequestsError ?: "Error",
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        uiState.pendingBudgetRequests.isEmpty() -> {
                            item {
                                EmptyBudgetRequestsCard()
                            }
                        }

                        else -> {
                            items(uiState.pendingBudgetRequests) { request ->
                                BudgetRequestCard(
                                    request = request,
                                    onClick = { onNavigateToBudgetRequestDetails(request.id) }
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
fun TotalExpensesCard(totalExpenses: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Total de Gastos",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${String.format("%,.2f", totalExpenses)}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ExpensesPieChart(expensesByCategory: List<ExpensesByCategory>) {
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
                text = "Gastos por Categoría",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Gráfica de pastel
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PieChart(expensesByCategory = expensesByCategory)
                }

                // Leyenda
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    expensesByCategory.forEach { category ->
                        LegendItem(
                            color = getCategoryColor(category.category),
                            label = getCategoryLabel(category.category),
                            percentage = category.percentage,
                            amount = category.totalAmount
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(expensesByCategory: List<ExpensesByCategory>) {
    var startAngle by remember { mutableStateOf(0f) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(expensesByCategory) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = Modifier.size(180.dp)) {
        val radius = size.minDimension / 2
        val centerX = size.width / 2
        val centerY = size.height / 2

        startAngle = -90f

        expensesByCategory.forEach { category ->
            val sweepAngle = (category.percentage / 100f) * 360f * animatedProgress.value
            val color = getCategoryColor(category.category)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2)
            )

            startAngle += sweepAngle
        }

        // Círculo blanco en el centro
        drawCircle(
            color = Color.White,
            radius = radius * 0.5f,
            center = Offset(centerX, centerY)
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String, percentage: Float, amount: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, CircleShape)
        )
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "${String.format("%.1f", percentage)}% - $${String.format("%.0f", amount)}",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun BudgetRequestCard(
    request: BudgetRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.tripName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = request.driverName,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = Color(0xFFFFA726).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Pendiente",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFA726),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Presupuesto Actual",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "$${String.format("%.2f", request.currentBudget)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Solicitado",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "$${String.format("%.2f", request.requestedBudget)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aumento: $${String.format("%.2f", request.increaseAmount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBudgetRequestsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay solicitudes pendientes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Todas las solicitudes han sido procesadas",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

fun getCategoryColor(category: ExpenseCategory): Color {
    return when (category) {
        ExpenseCategory.FUEL -> Color(0xFFFF6B6B)
        ExpenseCategory.FOOD -> Color(0xFF4ECDC4)
        ExpenseCategory.TOLL -> Color(0xFFFFA726)
        ExpenseCategory.MAINTENANCE -> Color(0xFF9C27B0)
        ExpenseCategory.PARKING -> Color(0xFF66BB6A)
        ExpenseCategory.OTHER -> Color(0xFF78909C)
    }
}

fun getCategoryLabel(category: ExpenseCategory): String {
    return when (category) {
        ExpenseCategory.FUEL -> "Combustible"
        ExpenseCategory.FOOD -> "Comida"
        ExpenseCategory.TOLL -> "Casetas"
        ExpenseCategory.MAINTENANCE -> "Mantenimiento"
        ExpenseCategory.PARKING -> "Estacionamiento"
        ExpenseCategory.OTHER -> "Otros"
    }
}