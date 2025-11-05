package com.tickettrack.app.ui.transportista.myexpenses

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.domain.model.expense.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import com.tickettrack.app.domain.model.trip.Location
/**
 * ViewModel para la lista de gastos del transportista.
 *
 * Responsabilidades:
 * - Cargar gastos del usuario (createdBy = userEmail)
 * - Filtrar por categoría, viaje y búsqueda
 * - Calcular estadísticas
 * - Gestionar refresh
 *
 * TODO: Conectar con API GET /api/expenses?userId={userId}
 */
class MyExpensesViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MyExpensesViewModel"
    }

    private val tokenManager = TokenManager(application)

    private val _state = MutableStateFlow(MyExpensesState())
    val state: StateFlow<MyExpensesState> = _state.asStateFlow()

    init {
        loadMyExpenses()
    }

    /**
     * Carga los gastos del transportista.
     *
     * TODO: Reemplazar con:
     * val result = expenseRepository.getExpensesByUser(userEmail)
     */
    fun loadMyExpenses() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    currentUserId = tokenManager.getUserEmail() ?: "",
                    currentUserName = tokenManager.getUserName() ?: ""
                )
            }

            try {
                Log.d(TAG, "Loading expenses for user: ${_state.value.currentUserId}")

                // Simular delay de red
                delay(1000)

                // TODO: Llamada real a API
                // val result = repository.getExpensesByUser(_state.value.currentUserId)

                // Por ahora, datos mock
                val mockExpenses = getMockExpenses(_state.value.currentUserId)

                // Calcular estadísticas
                val total = mockExpenses.sumOf { it.amount }
                val byCategory = mockExpenses
                    .groupBy { it.category }
                    .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }

                // Obtener viajes disponibles para filtro
                val trips = mockExpenses
                    .groupBy { it.tripId }
                    .map { (tripId, expenses) ->
                        TripFilterOption(
                            tripId = tripId,
                            tripName = getTripNameById(tripId),
                            expenseCount = expenses.size
                        )
                    }
                    .sortedByDescending { it.expenseCount }

                _state.update {
                    it.copy(
                        expenses = mockExpenses,
                        totalExpenses = total,
                        expenseCount = mockExpenses.size,
                        expensesByCategory = byCategory,
                        availableTrips = trips,
                        isLoading = false,
                        isEmpty = mockExpenses.isEmpty()
                    )
                }

                Log.d(TAG, "Expenses loaded successfully: ${mockExpenses.size}")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading expenses", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar gastos: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Refresca la lista de gastos (pull to refresh).
     */
    fun refreshExpenses() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            delay(1000)
            loadMyExpenses()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Filtra gastos por categoría.
     */
    fun filterByCategory(category: ExpenseCategory?) {
        _state.update { it.copy(selectedCategoryFilter = category) }
    }

    /**
     * Filtra gastos por viaje.
     */
    fun filterByTrip(tripId: String?) {
        _state.update { it.copy(selectedTripFilter = tripId) }
    }

    /**
     * Actualiza el query de búsqueda.
     */
    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    /**
     * Alterna la visibilidad de los filtros.
     */
    fun toggleFilters() {
        _state.update { it.copy(showFilters = !it.showFilters) }
    }

    /**
     * Limpia todos los filtros.
     */
    fun clearFilters() {
        _state.update {
            it.copy(
                selectedCategoryFilter = null,
                selectedTripFilter = null,
                searchQuery = ""
            )
        }
    }

    /**
     * Limpia el mensaje de error.
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    // ==========================================
    // DATOS MOCK (Remover cuando API esté lista)
    // ==========================================

    /**
     * Genera gastos de ejemplo del transportista.
     */
    private fun getMockExpenses(userEmail: String): List<Expense> {
        val now = Instant.now()

        return listOf(
            // Gastos de trip_001 (En progreso)
            Expense(
                id = "exp_001",
                tripId = "trip_001",
                amount = 1200.0,
                currency = "MXN",
                category = ExpenseCategory.FUEL,
                description = "Gasolina Pemex - Querétaro",
                date = now.minus(1, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_001/exp_001_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket001.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(1, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = Location(
                    address = "Gasolinera Pemex, Carr. 57",
                    coordinates = com.tickettrack.app.domain.model.trip.Coordinates(20.5888, -100.3899)
                ),
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(1, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(1, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_002",
                tripId = "trip_001",
                amount = 450.0,
                currency = "MXN",
                category = ExpenseCategory.FOOD,
                description = "Comida - Restaurante La Parilla",
                date = now.minus(1, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_001/exp_002_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket002.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(1, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = null,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(1, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(1, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_003",
                tripId = "trip_001",
                amount = 2850.0,
                currency = "MXN",
                category = ExpenseCategory.MAINTENANCE,
                description = "Reparación de neumático - Taller Express",
                date = now.minus(2, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_001/exp_003_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket003.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(2, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = Location(
                    address = "Taller Express, Av. Constitución 250",
                    coordinates = com.tickettrack.app.domain.model.trip.Coordinates(20.5950, -100.3850)
                ),
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(2, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(2, ChronoUnit.DAYS).toString()
            ),

            // Gastos de trip_003 (Completado)
            Expense(
                id = "exp_011",
                tripId = "trip_003",
                amount = 3200.0,
                currency = "MXN",
                category = ExpenseCategory.FUEL,
                description = "Gasolina Premium - Guadalajara a Querétaro",
                date = now.minus(6, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_003/exp_011_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket011.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(6, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = Location(
                    address = "Pemex, Guadalajara Centro",
                    coordinates = com.tickettrack.app.domain.model.trip.Coordinates(20.6597, -103.3496)
                ),
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_012",
                tripId = "trip_003",
                amount = 680.0,
                currency = "MXN",
                category = ExpenseCategory.FOOD,
                description = "Comida y desayuno - Ruta Guadalajara",
                date = now.minus(6, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_003/exp_012_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket012.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(6, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = null,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_013",
                tripId = "trip_003",
                amount = 450.0,
                currency = "MXN",
                category = ExpenseCategory.TOLL,
                description = "Casetas autopista Guadalajara-León",
                date = now.minus(6, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_003/exp_013_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket013.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(6, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = null,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_014",
                tripId = "trip_003",
                amount = 1850.0,
                currency = "MXN",
                category = ExpenseCategory.FUEL,
                description = "Gasolina - León, Guanajuato",
                date = now.minus(6, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_003/exp_014_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket014.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(6, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = Location(
                    address = "Gasolinera León Centro",
                    coordinates = com.tickettrack.app.domain.model.trip.Coordinates(21.1236, -101.6827)
                ),
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_015",
                tripId = "trip_003",
                amount = 2320.0,
                currency = "MXN",
                category = ExpenseCategory.OTHER,
                description = "Sistema de refrigeración - Mantenimiento urgente",
                date = now.minus(6, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_003/exp_015_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket015.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(6, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = Location(
                    address = "Taller Refrigeración Industrial",
                    coordinates = com.tickettrack.app.domain.model.trip.Coordinates(20.5888, -100.3899)
                ),
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
            ),

            // Gastos de trip_005 (En progreso - con varios gastos)
            Expense(
                id = "exp_021",
                tripId = "trip_005",
                amount = 2100.0,
                currency = "MXN",
                category = ExpenseCategory.FUEL,
                description = "Gasolina Premium - Saltillo",
                date = now.minus(3, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_005/exp_021_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket021.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(3, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = null,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(3, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(3, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_022",
                tripId = "trip_005",
                amount = 890.0,
                currency = "MXN",
                category = ExpenseCategory.FOOD,
                description = "Comida y hospedaje",
                date = now.minus(3, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_005/exp_022_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket022.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(3, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = null,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(3, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(3, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_023",
                tripId = "trip_005",
                amount = 3500.0,
                currency = "MXN",
                category = ExpenseCategory.MAINTENANCE,
                description = "Cambio de aceite y filtros - Servicio mayor",
                date = now.minus(2, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_005/exp_023_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket023.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(2, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = Location(
                    address = "Taller Automotriz del Norte",
                    coordinates = com.tickettrack.app.domain.model.trip.Coordinates(25.4260, -100.9737)
                ),
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(2, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(2, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_024",
                tripId = "trip_005",
                amount = 580.0,
                currency = "MXN",
                category = ExpenseCategory.TOLL,
                description = "Casetas Saltillo-Monterrey",
                date = now.minus(2, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_005/exp_024_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket024.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(2, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = null,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(2, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(2, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_025",
                tripId = "trip_005",
                amount = 1800.0,
                currency = "MXN",
                category = ExpenseCategory.FUEL,
                description = "Gasolina - San Luis Potosí",
                date = now.minus(1, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_005/exp_025_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket025.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                moderationNote = "",
                moderatedAt = now.minus(1, ChronoUnit.DAYS).toString(),
                moderatedBy = "ai-moderator",
                location = null,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(1, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(1, ChronoUnit.DAYS).toString()
            ),

            // Gasto reciente con moderación pendiente
            Expense(
                id = "exp_026",
                tripId = "trip_001",
                amount = 750.0,
                currency = "MXN",
                category = ExpenseCategory.FOOD,
                description = "Comida - En ruta a CDMX",
                date = now.minus(2, ChronoUnit.HOURS).toString(),
                ticketImagePath = "tickets/trip_001/exp_026_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket026.jpg",
                ticketModerationStatus = ModerationStatus.PENDING,
                moderationNote = "",
                moderatedAt = "",
                moderatedBy = null,
                location = null,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(2, ChronoUnit.HOURS).toString(),
                updatedAt = now.minus(2, ChronoUnit.HOURS).toString()
            )
        )
    }

    /**
     * Obtiene el nombre de un viaje por su ID (mock).
     */
    private fun getTripNameById(tripId: String): String {
        return when (tripId) {
            "trip_001" -> "Entrega Whirlpool - Bimbo"
            "trip_002" -> "Transporte CEMEX"
            "trip_003" -> "Farmacéuticos Guadalajara"
            "trip_004" -> "Distribución Coca-Cola"
            "trip_005" -> "Autopartes General Motors"
            else -> "Viaje $tripId"
        }
    }
}