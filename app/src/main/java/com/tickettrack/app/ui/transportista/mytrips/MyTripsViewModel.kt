package com.tickettrack.app.ui.transportista.mytrips

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.domain.model.trip.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * ViewModel para la lista de viajes del transportista.
 *
 * Responsabilidades:
 * - Cargar viajes asignados (assignedDriverId = userEmail)
 * - Filtrar por estado y búsqueda
 * - Calcular estadísticas
 * - Gestionar refresh
 *
 * TODO: Conectar con API GET /api/trips?userId={userId}&assignedOnly=true
 */
class MyTripsViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MyTripsViewModel"
    }

    private val tokenManager = TokenManager(application)

    private val _state = MutableStateFlow(MyTripsState())
    val state: StateFlow<MyTripsState> = _state.asStateFlow()

    init {
        loadMyTrips()
    }

    /**
     * Carga los viajes asignados al transportista.
     *
     * TODO: Reemplazar con:
     * val result = tripRepository.getTripsByDriver(userEmail)
     */
    fun loadMyTrips() {
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
                Log.d(TAG, "Loading trips for user: ${_state.value.currentUserId}")

                // Simular delay de red
                delay(1000)

                // TODO: Llamada real a API
                // val result = repository.getTripsByDriver(_state.value.currentUserId)

                // Por ahora, datos mock
                val mockTrips = getMockTrips(_state.value.currentUserId)

                // Calcular estadísticas
                val inProgress = mockTrips.count { it.status == TripStatus.IN_PROGRESS }
                val pending = mockTrips.count { it.status == TripStatus.PENDING }
                val completed = mockTrips.count { it.status == TripStatus.COMPLETED }

                _state.update {
                    it.copy(
                        trips = mockTrips,
                        totalTrips = mockTrips.size,
                        tripsInProgress = inProgress,
                        tripsPending = pending,
                        tripsCompleted = completed,
                        isLoading = false,
                        isEmpty = mockTrips.isEmpty()
                    )
                }

                Log.d(TAG, "Trips loaded successfully: ${mockTrips.size}")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading trips", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar viajes: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Refresca la lista de viajes (pull to refresh).
     */
    fun refreshTrips() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            delay(1000)
            loadMyTrips()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Filtra viajes por estado.
     */
    fun filterByStatus(status: TripStatus?) {
        _state.update { it.copy(selectedStatusFilter = status) }
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
                selectedStatusFilter = null,
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
     * Genera viajes de ejemplo asignados al transportista.
     * Incluye diferentes estados para mostrar variedad.
     */
    private fun getMockTrips(userEmail: String): List<Trip> {
        val now = Instant.now()

        return listOf(
            // Viaje 1: EN PROGRESO
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
                    description = "Refrigeradores y lavadoras (10 unidades)",
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

            // Viaje 2: PENDIENTE
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
                    description = "Mezcladoras de concreto (2 unidades)",
                    specialRequirements = "Requiere escolta y permisos especiales"
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

            // Viaje 3: COMPLETADO
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
                    description = "Medicamentos refrigerados (500 cajas)",
                    specialRequirements = "Temperatura controlada 2-8°C, manejo certificado"
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
            ),

            // Viaje 4: PENDIENTE (otro ejemplo)
            Trip(
                id = "trip_004",
                cargoName = "Distribución de Refrescos - Coca-Cola FEMSA",
                origin = Location(
                    address = "Planta Coca-Cola FEMSA, Blvd. Díaz Ordaz 130",
                    city = "Monterrey",
                    state = "Nuevo León",
                    zipCode = "64988",
                    coordinates = Coordinates(25.7817, -100.2597)
                ),
                destination = Location(
                    address = "Centro de Distribución OXXO, Carr. Miguel Alemán 5678",
                    city = "Saltillo",
                    state = "Coahuila",
                    zipCode = "25000",
                    coordinates = Coordinates(25.4260, -100.9737)
                ),
                cargo = Cargo(
                    type = "Bebidas",
                    weight = 3500.0,
                    description = "Refrescos y bebidas embotelladas (200 cajas)",
                    specialRequirements = "Producto perecedero, entrega urgente"
                ),
                budget = Budget(
                    initial = 8000.0,
                    current = 8000.0,
                    currency = "MXN",
                    history = emptyList()
                ),
                assignedDriverId = userEmail,
                createdByAdminId = "admin@tickettrack.com",
                status = TripStatus.PENDING,
                statusHistory = listOf(
                    StatusChange(
                        status = TripStatus.PENDING,
                        changedAt = now.minus(1, ChronoUnit.HOURS).toString(),
                        changedBy = "admin@tickettrack.com",
                        changedByName = "Admin Principal"
                    )
                ),
                totalExpenses = 0.0,
                remainingBudget = 8000.0,
                expenseCount = 0,
                budgetIncreaseCount = 0,
                createdAt = now.minus(1, ChronoUnit.HOURS).toString(),
                updatedAt = now.minus(1, ChronoUnit.HOURS).toString(),
                completedAt = null
            ),

            // Viaje 5: EN PROGRESO (con gastos considerables)
            Trip(
                id = "trip_005",
                cargoName = "Importación de Autopartes - General Motors",
                origin = Location(
                    address = "GM Planta Ramos Arizpe, Carr. 57 Km 35",
                    city = "Ramos Arizpe",
                    state = "Coahuila",
                    zipCode = "25900",
                    coordinates = Coordinates(25.5407, -100.9584)
                ),
                destination = Location(
                    address = "GM Planta Silao, Blvd. Silao-León Km 8.5",
                    city = "Silao",
                    state = "Guanajuato",
                    zipCode = "36100",
                    coordinates = Coordinates(20.9253, -101.4295)
                ),
                cargo = Cargo(
                    type = "Autopartes",
                    weight = 6500.0,
                    description = "Componentes de transmisión y motor",
                    specialRequirements = "Alto valor, requiere seguro especial"
                ),
                budget = Budget(
                    initial = 18000.0,
                    current = 20000.0, // Se aumentó el presupuesto
                    currency = "MXN",
                    history = listOf(
                        BudgetIncrease(
                            previousAmount = 18000.0,
                            newAmount = 20000.0,
                            increase = 2000.0,
                            reason = "Gastos de mantenimiento imprevisto en ruta",
                            requestedBy = userEmail,
                            requestedByName = tokenManager.getUserName() ?: "Transportista",
                            approvedBy = "admin@tickettrack.com",
                            approvedByName = "Admin Principal",
                            requestedAt = now.minus(2, ChronoUnit.HOURS).toString(),
                            approvedAt = now.minus(1, ChronoUnit.HOURS).toString(),
                            urgency = Urgency.MEDIUM,
                            supportingExpenses = emptyList()
                        )
                    )
                ),
                assignedDriverId = userEmail,
                createdByAdminId = "admin@tickettrack.com",
                status = TripStatus.IN_PROGRESS,
                statusHistory = listOf(
                    StatusChange(
                        status = TripStatus.PENDING,
                        changedAt = now.minus(4, ChronoUnit.DAYS).toString(),
                        changedBy = "admin@tickettrack.com",
                        changedByName = "Admin Principal"
                    ),
                    StatusChange(
                        status = TripStatus.IN_PROGRESS,
                        changedAt = now.minus(3, ChronoUnit.DAYS).toString(),
                        changedBy = userEmail,
                        changedByName = tokenManager.getUserName() ?: "Transportista"
                    )
                ),
                totalExpenses = 12500.0,
                remainingBudget = 7500.0,
                expenseCount = 8,
                budgetIncreaseCount = 1,
                createdAt = now.minus(4, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(1, ChronoUnit.HOURS).toString(),
                completedAt = null
            )
        )
    }
}