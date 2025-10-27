package com.tickettrack.app.ui.expenses.charts

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.domain.model.expense.ExpenseCategory
import com.tickettrack.app.domain.model.expense.TimePeriod

/**
 * Pantalla de gráficas de gastos
 * HU19 - Visualizar gastos en gráfica cambiante por semana, mes y año
 *
 * TODO: Integrar librería de gráficas (MPAndroidChart o Vico)
 * Por ahora muestra las estadísticas en formato de lista
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseChartsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseChartsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Cargar estadísticas al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadStats(userId)
    }

    // Mostrar error si existe
    state.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo de selección de período
    if (state.showPeriodDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hidePeriodDialog() },
            title = { Text("Seleccionar Período") },
            text = {
                Column {
                    TimePeriod.values().forEach { period ->
                        TextButton(
                            onClick = { viewModel.changePeriod(period, userId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(period.displayName)
                                if (period == state.selectedPeriod) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF5AC5C5)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Análisis de Gastos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón para cambiar período
                    TextButton(
                        onClick = { viewModel.showPeriodDialog() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(state.selectedPeriod.displayName)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5AC5C5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF5AC5C5))
            }
        } else if (state.stats == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = "No hay datos para mostrar",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            val stats = state.stats!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Resumen general
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF5AC5C5)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Resumen General",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Gastado",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = stats.getFormattedTotal(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Promedio",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = stats.getFormattedAverage(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Divider(color = Color.White.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Número de Gastos",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = "${stats.expenseCount}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Gastos por categoría
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Gastos por Categoría",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (stats.expensesByCategory.isEmpty()) {
                                Text(
                                    text = "No hay datos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            } else {
                                stats.expensesByCategory.forEach { (category, amount) ->
                                    CategoryExpenseRow(
                                        category = category,
                                        amount = amount,
                                        totalAmount = stats.totalAmount
                                    )
                                }
                            }
                        }
                    }
                }

                // TODO: Agregar gráfica de líneas con librería
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF9C4)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFF57C00)
                            )
                            Column {
                                Text(
                                    text = "Gráfica en Desarrollo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF57C00)
                                )
                                Text(
                                    text = "La gráfica de líneas se integrará cuando se agregue la librería MPAndroidChart o Vico.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6D4C41)
                                )
                            }
                        }
                    }
                }

                // Gastos por fecha
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Gastos por Fecha",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (stats.expensesByDate.isEmpty()) {
                                Text(
                                    text = "No hay datos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Listar gastos por fecha
                items(stats.expensesByDate.takeLast(10).reversed()) { dateExpense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
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
                                    text = dateExpense.date,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${dateExpense.count} ${if (dateExpense.count == 1) "gasto" else "gastos"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "$${"%.2f".format(dateExpense.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5AC5C5)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente para mostrar gasto por categoría con barra de progreso
 */
@Composable
private fun CategoryExpenseRow(
    category: ExpenseCategory,
    amount: Double,
    totalAmount: Double
) {
    val percentage = if (totalAmount > 0) (amount / totalAmount) * 100 else 0.0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(getCategoryColor(category))
                )
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "$${"%.2f".format(amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = getCategoryColor(category)
            )
        }

        LinearProgressIndicator(
            progress = (percentage / 100).toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = getCategoryColor(category),
            trackColor = getCategoryColor(category).copy(alpha = 0.2f)
        )

        Text(
            text = "${"%.1f".format(percentage)}%",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

/**
 * Obtiene el color según la categoría
 */
private fun getCategoryColor(category: ExpenseCategory): Color {
    return when (category) {
        ExpenseCategory.FUEL -> Color(0xFFF44336)
        ExpenseCategory.FOOD -> Color(0xFFFF9800)
        ExpenseCategory.TOLL -> Color(0xFF2196F3)
        ExpenseCategory.MAINTENANCE -> Color(0xFF9C27B0)
        ExpenseCategory.OTHER -> Color(0xFF607D8B)
    }
}