package com.tickettrack.app.ui.expenses.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tickettrack.app.domain.model.expense.ExpenseCategory
import com.tickettrack.app.ui.expenses.list.components.ExpenseCard

/**
 * Pantalla principal de lista de gastos
 * HU18 - Consultar y filtrar gastos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    userId: String,
    userRole: String,
    onNavigateToRegister: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToBudgetRequests: () -> Unit,
    tripId: String? = null,
    viewModel: ExpenseListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val canDelete = userRole == "admin" || userRole == "consignatario"

    // Cargar gastos al iniciar
    LaunchedEffect(Unit) {
        if (tripId != null) {
            viewModel.loadExpensesByTrip(tripId)
        } else {
            viewModel.loadExpenses(userId, userRole)
        }
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

    // Diálogo de confirmación de eliminación
    state.expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = { Text("Eliminar Gasto") },
            text = { Text("¿Estás seguro de que deseas eliminar este gasto de ${expense.getFormattedAmount()}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(expense.id, userId)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de filtros
    if (state.showFilterDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideFilterDialog() },
            title = { Text("Filtrar por Categoría") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            viewModel.onCategoryFilterChanged(null)
                            viewModel.hideFilterDialog()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Todas las categorías", modifier = Modifier.fillMaxWidth())
                    }

                    Divider()

                    ExpenseCategory.values().forEach { category ->
                        TextButton(
                            onClick = {
                                viewModel.onCategoryFilterChanged(category)
                                viewModel.hideFilterDialog()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category.displayName, modifier = Modifier.fillMaxWidth())
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
                title = {
                    Column {
                        Text(if (tripId != null) "Gastos del Viaje" else "Mis Gastos")
                        if (state.hasActiveFilters()) {
                            Text(
                                text = "Filtros activos",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    // Botón de filtros
                    IconButton(onClick = { viewModel.showFilterDialog() }) {
                        BadgedBox(
                            badge = {
                                if (state.hasActiveFilters()) {
                                    Badge { Text("!") }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filtrar",
                                tint = Color.White
                            )
                        }
                    }

                    // Botón de gráficas
                    IconButton(onClick = onNavigateToCharts) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "Gráficas",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5AC5C5),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            // Botón para agregar gasto (solo para transportistas)
            if (userRole == "driver" || userRole == "transportista") {
                FloatingActionButton(
                    onClick = onNavigateToRegister,
                    containerColor = Color(0xFF5AC5C5)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Gasto")
                }
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(state.isRefreshing),
            onRefresh = { viewModel.refreshExpenses(userId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF5AC5C5))
                }
            } else if (state.filteredExpenses.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = if (state.hasActiveFilters()) {
                                "No se encontraron gastos con los filtros aplicados"
                            } else {
                                "No hay gastos registrados"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )

                        if (state.hasActiveFilters()) {
                            Button(
                                onClick = { viewModel.clearFilters() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5AC5C5)
                                )
                            ) {
                                Text("Limpiar Filtros")
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Resumen de gastos
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF5AC5C5).copy(alpha = 0.1f)
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
                                    text = "Total Gastado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = state.getFormattedTotal(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5AC5C5)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Gastos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${state.filteredExpenses.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5AC5C5)
                                )
                            }
                        }
                    }

                    // Barra de búsqueda
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = { Text("Buscar gasto...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de gastos
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.filteredExpenses) { expense ->
                            ExpenseCard(
                                expense = expense,
                                onClick = { /* TODO: Navegar a detalle */ },
                                onDelete = { viewModel.showDeleteConfirmation(expense) },
                                canDelete = canDelete
                            )
                        }
                    }
                }
            }
        }
    }
}