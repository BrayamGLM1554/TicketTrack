package com.tickettrack.app.ui.transportista.myexpenses

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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tickettrack.app.domain.model.expense.ExpenseCategory
import com.tickettrack.app.ui.transportista.myexpenses.components.MyExpenseCard

/**
 * Pantalla de lista de gastos del transportista (USER).
 *
 * Funcionalidades:
 * - Lista de gastos filtrados (createdBy = userEmail)
 * - Filtros por categoría y viaje
 * - Búsqueda por texto
 * - Pull to refresh
 * - Estadísticas rápidas
 * - Botón para registrar nuevo gasto
 *
 * HU relevantes: HU16 (Registrar Gasto), HU18 (Consultar Gastos)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyExpensesScreen(
    onNavigateToRegisterExpense: () -> Unit,
    viewModel: MyExpensesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(state.isRefreshing)

    val displayExpenses = state.getDisplayExpenses()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Gastos",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón de filtros
                    IconButton(onClick = { viewModel.toggleFilters() }) {
                        Badge(
                            containerColor = if (state.hasActiveFilters()) {
                                Color(0xFFF44336)
                            } else {
                                Color.Transparent
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtros"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5AC5C5),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToRegisterExpense,
                containerColor = Color(0xFF5AC5C5)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Registrar Gasto",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshExpenses() },
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Estadísticas rápidas
                QuickStatsCard(
                    totalAmount = state.totalExpenses,
                    expenseCount = state.expenseCount,
                    filteredAmount = if (state.hasActiveFilters()) state.getFilteredTotal() else null
                )

                // Sección de filtros (expandible)
                if (state.showFilters) {
                    FiltersSection(
                        selectedCategory = state.selectedCategoryFilter,
                        selectedTrip = state.selectedTripFilter,
                        availableTrips = state.availableTrips,
                        searchQuery = state.searchQuery,
                        onCategorySelected = { viewModel.filterByCategory(it) },
                        onTripSelected = { viewModel.filterByTrip(it) },
                        onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }

                // Badge de filtros activos
                if (state.hasActiveFilters()) {
                    ActiveFiltersBadge(
                        onClear = { viewModel.clearFilters() }
                    )
                }

                // Lista de gastos o estados
                when {
                    state.isLoading && state.expenses.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF5AC5C5)
                            )
                        }
                    }

                    state.isEmpty -> {
                        EmptyState(
                            message = "No has registrado gastos aún.\n¡Registra tu primer gasto!",
                            onAddExpense = onNavigateToRegisterExpense
                        )
                    }

                    displayExpenses.isEmpty() -> {
                        EmptyState(
                            message = "No se encontraron gastos con los filtros aplicados.\nIntenta cambiar los criterios de búsqueda.",
                            onAddExpense = null
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayExpenses) { expense ->
                                MyExpenseCard(expense = expense)
                            }
                        }
                    }
                }

                // Mensaje de error
                state.errorMessage?.let { error ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text(error)
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES INTERNOS
// ==========================================

@Composable
private fun QuickStatsCard(
    totalAmount: Double,
    expenseCount: Int,
    filteredAmount: Double?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Resumen de Gastos",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total Gastado",
                    value = "$${String.format("%.2f", totalAmount)}",
                    color = Color(0xFF5AC5C5)
                )
                StatItem(
                    label = "Número de Gastos",
                    value = expenseCount.toString(),
                    color = Color(0xFF2196F3)
                )
            }

            // Mostrar total filtrado si hay filtros activos
            filteredAmount?.let { amount ->
                if (amount != totalAmount) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filtrado: $${String.format("%.2f", amount)}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersSection(
    selectedCategory: ExpenseCategory?,
    selectedTrip: String?,
    availableTrips: List<TripFilterOption>,
    searchQuery: String,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    onTripSelected: (String?) -> Unit,
    onSearchChanged: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por descripción...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5AC5C5),
                    unfocusedBorderColor = Color.Gray
                )
            )

            // Filtros de categoría
            Text(
                text = "Filtrar por categoría:",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            // Primera fila de categorías
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("Todas") }
                )
                FilterChip(
                    selected = selectedCategory == ExpenseCategory.FUEL,
                    onClick = { onCategorySelected(ExpenseCategory.FUEL) },
                    label = { Text("Combustible") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                FilterChip(
                    selected = selectedCategory == ExpenseCategory.FOOD,
                    onClick = { onCategorySelected(ExpenseCategory.FOOD) },
                    label = { Text("Comida") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            // Segunda fila de categorías
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == ExpenseCategory.TOLL,
                    onClick = { onCategorySelected(ExpenseCategory.TOLL) },
                    label = { Text("Casetas") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Toll,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                FilterChip(
                    selected = selectedCategory == ExpenseCategory.MAINTENANCE,
                    onClick = { onCategorySelected(ExpenseCategory.MAINTENANCE) },
                    label = { Text("Mantenimiento") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            // Tercera fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == ExpenseCategory.OTHER,
                    onClick = { onCategorySelected(ExpenseCategory.OTHER) },
                    label = { Text("Otros") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            // Filtro por viaje (si hay viajes disponibles)
            if (availableTrips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Filtrar por viaje:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )

                // Dropdown de viajes
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedTrip?.let { tripId ->
                            availableTrips.find { it.tripId == tripId }?.tripName ?: "Seleccionar viaje"
                        } ?: "Todos los viajes",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5AC5C5)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        // Opción "Todos"
                        DropdownMenuItem(
                            text = { Text("Todos los viajes") },
                            onClick = {
                                onTripSelected(null)
                                expanded = false
                            }
                        )

                        // Viajes disponibles
                        availableTrips.forEach { trip ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = trip.tripName,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "(${trip.expenseCount})",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    onTripSelected(trip.tripId)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Botón limpiar filtros
            if (selectedCategory != null || selectedTrip != null || searchQuery.isNotEmpty()) {
                TextButton(
                    onClick = onClearFilters,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar filtros")
                }
            }
        }
    }
}

@Composable
private fun ActiveFiltersBadge(
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF5AC5C5).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color(0xFF5AC5C5),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filtros activos",
                    color = Color(0xFF5AC5C5),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
            TextButton(onClick = onClear) {
                Text("Limpiar", color = Color(0xFF5AC5C5))
            }
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    onAddExpense: (() -> Unit)?
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
                imageVector = Icons.Default.Receipt,
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

            onAddExpense?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = it,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5AC5C5)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Gasto")
                }
            }
        }
    }
}