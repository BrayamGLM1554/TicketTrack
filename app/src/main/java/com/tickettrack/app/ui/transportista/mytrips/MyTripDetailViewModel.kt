package com.tickettrack.app.ui.transportista.mytrips

import android.app.Application
import android.util.Log
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
import com.tickettrack.app.domain.model.trip.Location
import com.tickettrack.app.domain.model.trip.Coordinates
/**
 * ViewModel para el detalle de un viaje del transportista.
 *
 * Responsabilidades:
 * - Cargar detalles del viaje
 * - Cargar gastos asociados
 * - Cambiar estado del viaje (iniciar/finalizar)
 * - Gestionar solicitudes de aumento de presupuesto
 * - Mostrar historial de cambios
 *
 * TODO: Conectar con APIs:
 * - GET /api/trips/{id}
 * - GET /api/trips/{id}/expenses
 * - PUT /api/trips/{id}/status
 * - POST /api/budget-requests
 */
class MyTripDetailViewModel(
    application: Application,
    private val tripId: String
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MyTripDetailViewModel"
    }

    private val tokenManager = TokenManager(application)

    private val _state = MutableStateFlow(MyTripDetailState(tripId = tripId))
    val state: StateFlow<MyTripDetailState> = _state.asStateFlow()

    init {
        loadTripDetail()
    }

    /**
     * Carga los detalles del viaje y sus gastos.
     *
     * TODO: Reemplazar con:
     * val tripResult = tripRepository.getTripById(tripId)
     * val expensesResult = expenseRepository.getExpensesByTrip(tripId)
     */
    fun loadTripDetail() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingTrip = true,
                    isLoadingExpenses = true,
                    errorMessage = null,
                    currentUserId = tokenManager.getUserEmail() ?: "",
                    currentUserName = tokenManager.getUserName() ?: ""
                )
            }

            try {
                Log.d(TAG, "Loading trip detail: $tripId")

                // Simular delay de red
                delay(1000)

                // TODO: Llamadas reales a API
                // val tripResult = repository.getTripById(tripId)
                // val expensesResult = repository.getExpensesByTrip(tripId)

                // Por ahora, datos mock
                val mockTrip = getMockTrip(tripId, _state.value.currentUserId)
                val mockExpenses = getMockExpenses(tripId, _state.value.currentUserId)

                val totalExpenses = mockExpenses.sumOf { it.amount }

                _state.update {
                    it.copy(
                        trip = mockTrip,
                        expenses = mockExpenses,
                        totalExpensesCalculated = totalExpenses,
                        isLoadingTrip = false,
                        isLoadingExpenses = false
                    )
                }

                Log.d(TAG, "Trip loaded successfully: ${mockTrip?.cargoName}")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading trip detail", e)
                _state.update {
                    it.copy(
                        isLoadingTrip = false,
                        isLoadingExpenses = false,
                        errorMessage = "Error al cargar viaje: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Refresca los datos del viaje (pull to refresh).
     */
    fun refreshTripDetail() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            delay(1000)
            loadTripDetail()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Cambia el estado del viaje a IN_PROGRESS.
     *
     * TODO: Reemplazar con:
     * val result = tripRepository.updateTripStatus(
     *     tripId = tripId,
     *     newStatus = TripStatus.IN_PROGRESS,
     *     changedBy = currentUserId,
     *     changedByName = currentUserName
     * )
     */
    fun startTrip() {
        viewModelScope.launch {
            _state.update { it.copy(isChangingStatus = true) }

            try {
                Log.d(TAG, "Starting trip: $tripId")

                // Simular delay de API
                delay(1500)

                // TODO: Llamada real a API
                // val result = repository.updateTripStatus(...)

                // Mock: Actualizar estado localmente
                val currentTrip = _state.value.trip
                if (currentTrip != null) {
                    val now = Instant.now().toString()
                    val statusChange = StatusChange(
                        status = TripStatus.IN_PROGRESS,
                        changedAt = now,
                        changedBy = _state.value.currentUserId,
                        changedByName = _state.value.currentUserName
                    )

                    val updatedTrip = currentTrip.copy(
                        status = TripStatus.IN_PROGRESS,
                        statusHistory = currentTrip.statusHistory + statusChange,
                        updatedAt = now
                    )

                    _state.update {
                        it.copy(
                            trip = updatedTrip,
                            isChangingStatus = false,
                            statusChangeSuccess = true,
                            showStartTripDialog = false
                        )
                    }

                    Log.d(TAG, "Trip started successfully")

                    // Limpiar el flag de éxito después de un momento
                    delay(2000)
                    _state.update { it.copy(statusChangeSuccess = false) }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error starting trip", e)
                _state.update {
                    it.copy(
                        isChangingStatus = false,
                        errorMessage = "Error al iniciar viaje: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Cambia el estado del viaje a COMPLETED.
     *
     * TODO: Reemplazar con llamada a API PUT /api/trips/{id}/status
     */
    fun completeTrip() {
        viewModelScope.launch {
            _state.update { it.copy(isChangingStatus = true) }

            try {
                Log.d(TAG, "Completing trip: $tripId")

                // Simular delay de API
                delay(1500)

                // TODO: Llamada real a API
                // val result = repository.updateTripStatus(...)

                // Mock: Actualizar estado localmente
                val currentTrip = _state.value.trip
                if (currentTrip != null) {
                    val now = Instant.now().toString()
                    val statusChange = StatusChange(
                        status = TripStatus.COMPLETED,
                        changedAt = now,
                        changedBy = _state.value.currentUserId,
                        changedByName = _state.value.currentUserName
                    )

                    val updatedTrip = currentTrip.copy(
                        status = TripStatus.COMPLETED,
                        statusHistory = currentTrip.statusHistory + statusChange,
                        updatedAt = now,
                        completedAt = now
                    )

                    _state.update {
                        it.copy(
                            trip = updatedTrip,
                            isChangingStatus = false,
                            statusChangeSuccess = true,
                            showCompleteTripDialog = false
                        )
                    }

                    Log.d(TAG, "Trip completed successfully")

                    // Limpiar el flag de éxito después de un momento
                    delay(2000)
                    _state.update { it.copy(statusChangeSuccess = false) }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error completing trip", e)
                _state.update {
                    it.copy(
                        isChangingStatus = false,
                        errorMessage = "Error al completar viaje: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Muestra el diálogo para iniciar viaje.
     */
    fun showStartTripDialog() {
        _state.update { it.copy(showStartTripDialog = true) }
    }

    /**
     * Oculta el diálogo para iniciar viaje.
     */
    fun dismissStartTripDialog() {
        _state.update { it.copy(showStartTripDialog = false) }
    }

    /**
     * Muestra el diálogo para completar viaje.
     */
    fun showCompleteTripDialog() {
        _state.update { it.copy(showCompleteTripDialog = true) }
    }

    /**
     * Oculta el diálogo para completar viaje.
     */
    fun dismissCompleteTripDialog() {
        _state.update { it.copy(showCompleteTripDialog = false) }
    }

    /**
     * Muestra el diálogo para solicitar aumento de presupuesto.
     */
    fun showBudgetRequestDialog() {
        _state.update { it.copy(showBudgetRequestDialog = true) }
    }

    /**
     * Oculta el diálogo para solicitar aumento de presupuesto.
     */
    fun dismissBudgetRequestDialog() {
        _state.update { it.copy(showBudgetRequestDialog = false) }
    }

    /**
     * Cambia la tab seleccionada.
     */
    fun onTabSelected(index: Int) {
        _state.update { it.copy(selectedTab = index) }
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
     * Obtiene un viaje mock por ID.
     */
    private fun getMockTrip(tripId: String, userEmail: String): Trip? {
        val now = Instant.now()

        // Retornar diferentes viajes según el ID
        return when (tripId) {
            "trip_001" -> Trip(
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
                    description = "Refrigeradores y lavadoras (10 unidades)",
                    specialRequirements = "Manejo con cuidado, productos frágiles. Entregar en horario de 8am a 5pm."
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
            )

            "trip_002" -> Trip(
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
                    description = "Mezcladoras de concreto (2 unidades)",
                    specialRequirements = "Requiere escolta y permisos especiales. Vehículo de carga pesada obligatorio."
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
            )

            "trip_003" -> Trip(
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
                    description = "Medicamentos refrigerados (500 cajas)",
                    specialRequirements = "Temperatura controlada 2-8°C. Manejo certificado. Entrega urgente."
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

            else -> null
        }
    }

    /**
     * Obtiene gastos mock para un viaje.
     */
    private fun getMockExpenses(tripId: String, userEmail: String): List<Expense> {
        val now = Instant.now()

        return when (tripId) {
            "trip_001" -> listOf(
                Expense(
                    id = "exp_001",
                    tripId = tripId,
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
                    tripId = tripId,
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
                    tripId = tripId,
                    amount = 2850.0,
                    currency = "MXN",
                    category = ExpenseCategory.MAINTENANCE,
                    description = "Reparación de neumático - Taller Express",
                    date = now.minus(2, ChronoUnit.DAYS).toString(),
                    ticketImagePath = "tickets/trip_001/exp_003_ticket.jpg",
                    ticketPublicUrl = "https://storage.googleapis.com/ticket003.jpg",
                    ticketModerationStatus = ModerationStatus.APPROVED,
                    createdBy = userEmail,
                    createdByName = tokenManager.getUserName() ?: "Transportista",
                    createdAt = now.minus(2, ChronoUnit.DAYS).toString(),
                    updatedAt = now.minus(2, ChronoUnit.DAYS).toString()
                )
            )

            "trip_003" -> listOf(
                Expense(
                    id = "exp_011",
                    tripId = tripId,
                    amount = 3200.0,
                    currency = "MXN",
                    category = ExpenseCategory.FUEL,
                    description = "Gasolina Premium - Guadalajara a Querétaro",
                    date = now.minus(6, ChronoUnit.DAYS).toString(),
                    ticketImagePath = "tickets/trip_003/exp_011_ticket.jpg",
                    ticketPublicUrl = "https://storage.googleapis.com/ticket011.jpg",
                    ticketModerationStatus = ModerationStatus.APPROVED,
                    createdBy = userEmail,
                    createdByName = tokenManager.getUserName() ?: "Transportista",
                    createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                    updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
                ),
                Expense(
                    id = "exp_012",
                    tripId = tripId,
                    amount = 680.0,
                    currency = "MXN",
                    category = ExpenseCategory.FOOD,
                    description = "Comida y desayuno - Ruta Guadalajara",
                    date = now.minus(6, ChronoUnit.DAYS).toString(),
                    ticketImagePath = "tickets/trip_003/exp_012_ticket.jpg",
                    ticketPublicUrl = "https://storage.googleapis.com/ticket012.jpg",
                    ticketModerationStatus = ModerationStatus.APPROVED,
                    createdBy = userEmail,
                    createdByName = tokenManager.getUserName() ?: "Transportista",
                    createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                    updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
                ),
                Expense(
                    id = "exp_013",
                    tripId = tripId,
                    amount = 450.0,
                    currency = "MXN",
                    category = ExpenseCategory.TOLL,
                    description = "Casetas autopista Guadalajara-León",
                    date = now.minus(6, ChronoUnit.DAYS).toString(),
                    ticketImagePath = "tickets/trip_003/exp_013_ticket.jpg",
                    ticketPublicUrl = "https://storage.googleapis.com/ticket013.jpg",
                    ticketModerationStatus = ModerationStatus.APPROVED,
                    createdBy = userEmail,
                    createdByName = tokenManager.getUserName() ?: "Transportista",
                    createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                    updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
                ),
                Expense(
                    id = "exp_014",
                    tripId = tripId,
                    amount = 1850.0,
                    currency = "MXN",
                    category = ExpenseCategory.FUEL,
                    description = "Gasolina - León, Guanajuato",
                    date = now.minus(6, ChronoUnit.DAYS).toString(),
                    ticketImagePath = "tickets/trip_003/exp_014_ticket.jpg",
                    ticketPublicUrl = "https://storage.googleapis.com/ticket014.jpg",
                    ticketModerationStatus = ModerationStatus.APPROVED,
                    createdBy = userEmail,
                    createdByName = tokenManager.getUserName() ?: "Transportista",
                    createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                    updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
                ),
                Expense(
                    id = "exp_015",
                    tripId = tripId,
                    amount = 2320.0,
                    currency = "MXN",
                    category = ExpenseCategory.OTHER,
                    description = "Sistema de refrigeración - Mantenimiento urgente",
                    date = now.minus(6, ChronoUnit.DAYS).toString(),
                    ticketImagePath = "tickets/trip_003/exp_015_ticket.jpg",
                    ticketPublicUrl = "https://storage.googleapis.com/ticket015.jpg",
                    ticketModerationStatus = ModerationStatus.APPROVED,
                    createdBy = userEmail,
                    createdByName = tokenManager.getUserName() ?: "Transportista",
                    createdAt = now.minus(6, ChronoUnit.DAYS).toString(),
                    updatedAt = now.minus(6, ChronoUnit.DAYS).toString()
                )
            )

            else -> emptyList()
        }
    }
}