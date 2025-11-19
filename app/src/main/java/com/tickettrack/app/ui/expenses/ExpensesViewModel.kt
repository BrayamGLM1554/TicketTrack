package com.tickettrack.app.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.*
import com.tickettrack.app.domain.repository.IBudgetRequestsRepository
import com.tickettrack.app.domain.repository.IExpensesRepository
import com.tickettrack.app.domain.repository.ITripsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExpensesUiState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val expensesByCategory: List<ExpensesByCategory> = emptyList(),
    val totalExpenses: Double = 0.0,
    val error: String? = null,

    // Budget Requests
    val isLoadingBudgetRequests: Boolean = false,
    val pendingBudgetRequests: List<BudgetRequest> = emptyList(),
    val budgetRequestsError: String? = null,

    // Cache tracking
    val lastRefreshTime: Long = 0
)

class ExpensesViewModel(
    private val expensesRepository: IExpensesRepository,
    private val budgetRequestsRepository: IBudgetRequestsRepository,
    private val tripsRepository: ITripsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    // Caché en memoria
    private var cachedTrips: List<Trip>? = null
    private var cachedAllExpenses: List<Expense>? = null
    private val cacheValidityMs = 30_000L // 30 segundos

    fun loadData(token: String, forceRefresh: Boolean = false) {
        loadExpenses(token, forceRefresh)
        loadPendingBudgetRequests(token)
    }

    private fun loadExpenses(token: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val currentTime = System.currentTimeMillis()
                val cacheIsValid = currentTime - _uiState.value.lastRefreshTime < cacheValidityMs

                // Usar caché si es válido y no es refresh forzado
                if (!forceRefresh && cacheIsValid && cachedTrips != null && cachedAllExpenses != null) {
                    println("⚡ Usando caché - Tiempo de carga: 0ms")
                    processAndDisplayExpenses(cachedTrips!!, cachedAllExpenses!!)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                val startTime = System.currentTimeMillis()
                println("🚀 Iniciando carga desde servidor...")

                // ESTRATEGIA: Una sola petición para gastos + viajes en paralelo
                val tripsDeferred = async { tripsRepository.getTrips(token) }
                val expensesDeferred = async {
                    expensesRepository.getExpenses(
                        token = token,
                        tripId = null, // Traer TODOS los gastos
                        limit = 10000
                    )
                }

                // Esperar AMBAS peticiones en paralelo
                val tripsResult = tripsDeferred.await()
                val expensesResult = expensesDeferred.await()

                val loadTime = System.currentTimeMillis() - startTime
                println("⏱️ Tiempo de carga del servidor: ${loadTime}ms")

                // Validar trips
                if (tripsResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = tripsResult.exceptionOrNull()?.message ?: "Error al cargar viajes"
                    )
                    return@launch
                }

                val trips = tripsResult.getOrNull() ?: emptyList()

                if (trips.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        expenses = emptyList(),
                        expensesByCategory = emptyList(),
                        totalExpenses = 0.0,
                        lastRefreshTime = currentTime
                    )
                    return@launch
                }

                // Validar expenses
                if (expensesResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = expensesResult.exceptionOrNull()?.message ?: "Error al cargar gastos"
                    )
                    return@launch
                }

                val allExpenses = expensesResult.getOrNull()?.expenses ?: emptyList()

                // Guardar en caché
                cachedTrips = trips
                cachedAllExpenses = allExpenses

                // Procesar y mostrar
                processAndDisplayExpenses(trips, allExpenses, currentTime)

                val totalTime = System.currentTimeMillis() - startTime
                println("✅ Carga completada en ${totalTime}ms")

            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error inesperado"
                )
            }
        }
    }

    private fun processAndDisplayExpenses(
        trips: List<Trip>,
        allExpenses: List<Expense>,
        refreshTime: Long = System.currentTimeMillis()
    ) {
        // Filtrar gastos por viajes del admin (filtrado en memoria - instantáneo)
        val tripIds = trips.map { it.id }.toSet()
        val filteredExpenses = allExpenses.filter { it.tripId in tripIds }

        println("📊 Trips: ${trips.size} | Gastos totales: ${allExpenses.size} | Gastos filtrados: ${filteredExpenses.size}")

        val byCategory = calculateExpensesByCategory(filteredExpenses)
        val total = filteredExpenses.sumOf { it.amount }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            expenses = filteredExpenses,
            expensesByCategory = byCategory,
            totalExpenses = total,
            lastRefreshTime = refreshTime
        )
    }

    private fun loadPendingBudgetRequests(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingBudgetRequests = true,
                budgetRequestsError = null
            )

            val result = budgetRequestsRepository.getPendingBudgetRequests(token)

            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingBudgetRequests = false,
                        pendingBudgetRequests = response.data
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingBudgetRequests = false,
                        budgetRequestsError = exception.message ?: "Error al cargar solicitudes"
                    )
                }
            )
        }
    }

    private fun calculateExpensesByCategory(expenses: List<Expense>): List<ExpensesByCategory> {
        if (expenses.isEmpty()) return emptyList()

        val total = expenses.sumOf { it.amount }

        return expenses
            .groupBy { it.category }
            .map { (category, expensesList) ->
                val categoryTotal = expensesList.sumOf { it.amount }
                ExpensesByCategory(
                    category = category,
                    totalAmount = categoryTotal,
                    count = expensesList.size,
                    percentage = ((categoryTotal / total) * 100).toFloat()
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    fun refreshData(token: String) {
        loadData(token, forceRefresh = true)
    }

    fun clearCache() {
        cachedTrips = null
        cachedAllExpenses = null
    }
}