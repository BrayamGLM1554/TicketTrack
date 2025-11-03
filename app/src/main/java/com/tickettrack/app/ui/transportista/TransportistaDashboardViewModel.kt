package com.tickettrack.app.ui.transportista

import com.tickettrack.app.domain.model.trip.Location
import com.tickettrack.app.domain.model.trip.Coordinates
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.domain.model.trip.*
import com.tickettrack.app.domain.model.expense.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * ViewModel del Dashboard del Transportista.
 *
 * Responsabilidades:
 * - Cargar resumen de viajes asignados
 * - Calcular estadísticas financieras
 * - Mostrar viajes y gastos recientes
 * - Gestionar alertas
 *
 * TODO: Conectar con APIs reales cuando estén disponibles
 */
class TransportistaDashboardViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _state = MutableStateFlow(TransportistaDashboardState())
    val state: StateFlow<TransportistaDashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * Carga todos los datos del dashboard.
     *
     * TODO: Reemplazar con llamadas a:
     * - GET /api/trips?userId={userId}&assignedOnly=true
     * - GET /api/expenses?userId={userId}&period=current
     * - GET /api/budget-requests?userId={userId}&status=pending
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Obtener datos del usuario
                val userEmail = tokenManager.getUserEmail() ?: ""
                val userName = tokenManager.getUserName() ?: "Transportista"

                // Simular delay de red
                delay(1000)

                // TODO: Aquí irían las llamadas reales a las APIs
                // val tripsResult = tripRepository.getTripsByDriver(userEmail)
                // val expensesResult = expenseRepository.getExpensesByUser(userEmail)

                // Por ahora, usamos datos mock
                val mockTrips = getMockTrips(userEmail)
                val mockExpenses = getMockExpenses(userEmail)

                // Calcular estadísticas
                val tripsInProgress = mockTrips.count { it.status == TripStatus.IN_PROGRESS }
                val tripsPending = mockTrips.count { it.status == TripStatus.PENDING }
                val tripsCompleted = mockTrips.count { it.status == TripStatus.COMPLETED }

                val totalExpenses = mockExpenses.sumOf { it.amount }
                val totalBudget = mockTrips.sumOf { it.budget.current }
                val remainingBudget = totalBudget - totalExpenses
                val budgetUsage = if (totalBudget > 0) {
                    ((totalExpenses / totalBudget) * 100).toFloat()
                } else 0f

                // Alerta de presupuesto bajo
                val hasLowBudget = budgetUsage >= 90f

                _state.update {
                    it.copy(
                        userName = userName,
                        userEmail = userEmail,
                        totalTripsAssigned = mockTrips.size,
                        tripsInProgress = tripsInProgress,
                        tripsPending = tripsPending,
                        tripsCompleted = tripsCompleted,
                        totalExpenses = totalExpenses,
                        totalBudgetAssigned = totalBudget,
                        remainingBudget = remainingBudget,
                        budgetUsagePercentage = budgetUsage,
                        recentTrips = mockTrips.take(3),
                        recentExpenses = mockExpenses.take(5),
                        hasLowBudgetAlert = hasLowBudget,
                        hasPendingRequests = 0, // TODO: Obtener de API
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar datos: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Refresca los datos del dashboard (pull to refresh).
     */
    fun refreshDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            delay(1000)
            loadDashboardData()
            _state.update { it.copy(isRefreshing = false) }
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
     * Genera viajes de ejemplo asignados al transportista.
     */
    private fun getMockTrips(userEmail: String): List<Trip> {
        val now = Instant.now()

        return listOf(
            Trip(
                id = "trip_001",
                cargoName = "Entrega Whirlpool - Bimbo",
                origin = Location(
                    address = "Whirlpool México, Carretera A Laredo Km. 9.5",
                    city = "Cienega De Flores",
                    state = "Nuevo León",
                    zipCode = "65550",
                    coordinates = Coordinates(25.6366, -100.1753)
                ),
                destination = Location(
                    address = "Bimbo Planta Centro, Av. Insurgentes 345",
                    city = "Ciudad de México",
                    state = "CDMX",
                    zipCode = "03100",
                    coordinates = Coordinates(19.3910, -99.1580)
                ),
                cargo = Cargo(
                    type = "Electrodomésticos",
                    weight = 5000.0,
                    description = "Refrigeradores y lavadoras",
                    specialRequirements = "Manejo con cuidado, productos frágiles"
                ),
                budget = Budget(
                    initial = 15000.0,
                    current = 15000.0,
                    currency = "MXN",
                    history = emptyList()
                ),
                assignedDriverId = userEmail,
                createdByAdminId = "admin@tickettrack.com",
                status = TripStatus.IN_PROGRESS,
                statusHistory = listOf(
                    StatusChange(
                        status = TripStatus.PENDING,
                        changedAt = now.minus(2, ChronoUnit.DAYS).toString(),
                        changedBy = "admin@tickettrack.com",
                        changedByName = "Admin Principal"
                    ),
                    StatusChange(
                        status = TripStatus.IN_PROGRESS,
                        changedAt = now.minus(1, ChronoUnit.DAYS).toString(),
                        changedBy = userEmail,
                        changedByName = tokenManager.getUserName() ?: "Transportista"
                    )
                ),
                totalExpenses = 4500.0,
                remainingBudget = 10500.0,
                expenseCount = 3,
                budgetIncreaseCount = 0,
                createdAt = now.minus(3, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(1, ChronoUnit.DAYS).toString(),
                completedAt = null
            ),

            Trip(
                id = "trip_002",
                cargoName = "Transporte de Maquinaria - CEMEX",
                origin = Location(
                    address = "CEMEX Planta Monterrey, Av. Constitución 444",
                    city = "Monterrey",
                    state = "Nuevo León",
                    zipCode = "64000",
                    coordinates = Coordinates(25.6866, -100.3161)
                ),
                destination = Location(
                    address = "Construcción Torre Reforma, Paseo de la Reforma 483",
                    city = "Ciudad de México",
                    state = "CDMX",
                    zipCode = "06500",
                    coordinates = Coordinates(19.4284, -99.1677)
                ),
                cargo = Cargo(
                    type = "Maquinaria pesada",
                    weight = 8000.0,
                    description = "Mezcladoras de concreto",
                    specialRequirements = "Requiere escolta"
                ),
                budget = Budget(
                    initial = 25000.0,
                    current = 25000.0,
                    currency = "MXN",
                    history = emptyList()
                ),
                assignedDriverId = userEmail,
                createdByAdminId = "admin@tickettrack.com",
                status = TripStatus.PENDING,
                statusHistory = listOf(
                    StatusChange(
                        status = TripStatus.PENDING,
                        changedAt = now.toString(),
                        changedBy = "admin@tickettrack.com",
                        changedByName = "Admin Principal"
                    )
                ),
                totalExpenses = 0.0,
                remainingBudget = 25000.0,
                expenseCount = 0,
                budgetIncreaseCount = 0,
                createdAt = now.toString(),
                updatedAt = now.toString(),
                completedAt = null
            ),

            Trip(
                id = "trip_003",
                cargoName = "Entrega Farmacéuticos - Farmacias Guadalajara",
                origin = Location(
                    address = "Laboratorios Pisa, Av. Guadalupe 5200",
                    city = "Guadalajara",
                    state = "Jalisco",
                    zipCode = "44280",
                    coordinates = Coordinates(20.6597, -103.3496)
                ),
                destination = Location(
                    address = "Centro de Distribución FG, Periférico Norte 1234",
                    city = "Querétaro",
                    state = "Querétaro",
                    zipCode = "76090",
                    coordinates = Coordinates(20.5888, -100.3899)
                ),
                cargo = Cargo(
                    type = "Productos farmacéuticos",
                    weight = 2000.0,
                    description = "Medicamentos refrigerados",
                    specialRequirements = "Temperatura controlada 2-8°C"
                ),
                budget = Budget(
                    initial = 12000.0,
                    current = 12000.0,
                    currency = "MXN",
                    history = emptyList()
                ),
                assignedDriverId = userEmail,
                createdByAdminId = "admin@tickettrack.com",
                status = TripStatus.COMPLETED,
                statusHistory = listOf(
                    StatusChange(
                        status = TripStatus.PENDING,
                        changedAt = now.minus(7, ChronoUnit.DAYS).toString(),
                        changedBy = "admin@tickettrack.com",
                        changedByName = "Admin Principal"
                    ),
                    StatusChange(
                        status = TripStatus.IN_PROGRESS,
                        changedAt = now.minus(6, ChronoUnit.DAYS).toString(),
                        changedBy = userEmail,
                        changedByName = tokenManager.getUserName() ?: "Transportista"
                    ),
                    StatusChange(
                        status = TripStatus.COMPLETED,
                        changedAt = now.minus(5, ChronoUnit.DAYS).toString(),
                        changedBy = userEmail,
                        changedByName = tokenManager.getUserName() ?: "Transportista"
                    )
                ),
                totalExpenses = 8500.0,
                remainingBudget = 3500.0,
                expenseCount = 5,
                budgetIncreaseCount = 0,
                createdAt = now.minus(7, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(5, ChronoUnit.DAYS).toString(),
                completedAt = now.minus(5, ChronoUnit.DAYS).toString()
            )
        )
    }

    /**
     * Genera gastos de ejemplo del transportista.
     */
    private fun getMockExpenses(userEmail: String): List<Expense> {
        val now = Instant.now()

        return listOf(
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
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(1, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(1, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_003",
                tripId = "trip_001",
                amount = 280.0,
                currency = "MXN",
                category = ExpenseCategory.TOLL,
                description = "Caseta México-Querétaro",
                date = now.minus(2, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_001/exp_003_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket003.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(2, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(2, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_004",
                tripId = "trip_001",
                amount = 2570.0,
                currency = "MXN",
                category = ExpenseCategory.MAINTENANCE,
                description = "Cambio de llantas - Taller Michelin",
                date = now.minus(2, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_001/exp_004_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket004.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(2, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(2, ChronoUnit.DAYS).toString()
            ),

            Expense(
                id = "exp_005",
                tripId = "trip_003",
                amount = 980.0,
                currency = "MXN",
                category = ExpenseCategory.FUEL,
                description = "Gasolina Pemex - Guadalajara",
                date = now.minus(6, ChronoUnit.DAYS).toString(),
                ticketImagePath = "tickets/trip_003/exp_005_ticket.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/ticket005.jpg",
                ticketModerationStatus = ModerationStatus.APPROVED,
                createdBy = userEmail,
                createdByName = tokenManager.getUserName() ?: "Transportista",
                createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
            )
        )
    }
}