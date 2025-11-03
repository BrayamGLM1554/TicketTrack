package com.tickettrack.app.ui.transportista.budgetrequest

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
 * ViewModel para solicitar aumento de presupuesto.
 *
 * Responsabilidades:
 * - Cargar viajes activos del transportista
 * - Validar formulario de solicitud
 * - Enviar solicitud al backend
 * - Calcular aumentos y porcentajes
 *
 * TODO: Conectar con API POST /api/budget-requests
 */
class BudgetRequestViewModel(
    application: Application,
    private val preselectedTripId: String? = null
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "BudgetRequestViewModel"
        private const val MIN_REASON_LENGTH = 20
        private const val MAX_REASON_LENGTH = 500
    }

    private val tokenManager = TokenManager(application)

    private val _state = MutableStateFlow(BudgetRequestState())
    val state: StateFlow<BudgetRequestState> = _state.asStateFlow()

    init {
        loadAvailableTrips()
    }

    /**
     * Carga los viajes activos del transportista.
     * Solo viajes en estado PENDING o IN_PROGRESS.
     *
     * TODO: Reemplazar con:
     * val result = tripRepository.getTripsByDriver(userEmail)
     *     .filter { it.status == PENDING || it.status == IN_PROGRESS }
     */
    private fun loadAvailableTrips() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingTrips = true,
                    currentUserId = tokenManager.getUserEmail() ?: "",
                    currentUserName = tokenManager.getUserName() ?: ""
                )
            }

            try {
                Log.d(TAG, "Loading available trips for budget request")

                // Simular delay de red
                delay(1000)

                // TODO: Llamada real a API
                // val result = repository.getTripsByDriver(_state.value.currentUserId)

                // Por ahora, datos mock
                val mockTrips = getMockActiveTrips(_state.value.currentUserId)

                // Si hay un viaje preseleccionado, buscarlo
                val preselectedTrip = if (preselectedTripId != null) {
                    mockTrips.find { it.id == preselectedTripId }
                } else null

                _state.update {
                    it.copy(
                        availableTrips = mockTrips,
                        selectedTrip = preselectedTrip,
                        currentBudget = preselectedTrip?.budget?.current ?: 0.0,
                        isLoadingTrips = false
                    )
                }

                Log.d(TAG, "Trips loaded: ${mockTrips.size}")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading trips", e)
                _state.update {
                    it.copy(
                        isLoadingTrips = false,
                        errorMessage = "Error al cargar viajes: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Selecciona un viaje para la solicitud.
     */
    fun onTripSelected(trip: Trip) {
        _state.update {
            it.copy(
                selectedTrip = trip,
                currentBudget = trip.budget.current,
                tripError = null,
                showTripDropdown = false,
                // Limpiar campos al cambiar de viaje
                requestedBudget = "",
                reason = "",
                requestedBudgetError = null,
                reasonError = null
            )
        }
    }

    /**
     * Actualiza el monto solicitado.
     */
    fun onRequestedBudgetChanged(value: String) {
        // Solo permitir números y punto decimal
        val filtered = value.filter { it.isDigit() || it == '.' }

        _state.update { it.copy(requestedBudget = filtered) }

        // Validar en tiempo real
        validateRequestedBudget(filtered)
    }

    /**
     * Actualiza la razón de la solicitud.
     */
    fun onReasonChanged(value: String) {
        if (value.length <= MAX_REASON_LENGTH) {
            _state.update { it.copy(reason = value) }
            validateReason(value)
        }
    }

    /**
     * Actualiza el nivel de urgencia.
     */
    fun onUrgencyChanged(urgency: Urgency) {
        _state.update { it.copy(urgency = urgency) }
    }

    /**
     * Alterna la visibilidad del dropdown de viajes.
     */
    fun toggleTripDropdown() {
        _state.update { it.copy(showTripDropdown = !it.showTripDropdown) }
    }

    /**
     * Valida el monto solicitado.
     */
    private fun validateRequestedBudget(value: String) {
        val error = when {
            value.isBlank() -> "Ingresa el monto solicitado"

            value.toDoubleOrNull() == null -> "Monto inválido"

            value.toDouble() <= _state.value.currentBudget ->
                "El monto debe ser mayor al presupuesto actual"

            value.toDouble() > (_state.value.currentBudget * 3) ->
                "El aumento no puede ser mayor al 200% del presupuesto actual"

            else -> null
        }

        _state.update {
            it.copy(
                requestedBudgetError = error,
                increaseAmount = if (error == null) it.calculateIncrease() else 0.0
            )
        }
    }

    /**
     * Valida la razón de la solicitud.
     */
    private fun validateReason(value: String) {
        val error = when {
            value.isBlank() -> "Ingresa la razón de la solicitud"

            value.length < MIN_REASON_LENGTH ->
                "La razón debe tener al menos $MIN_REASON_LENGTH caracteres"

            else -> null
        }

        _state.update { it.copy(reasonError = error) }
    }

    /**
     * Valida todo el formulario antes de enviar.
     */
    private fun validateForm(): Boolean {
        val state = _state.value

        // Validar viaje seleccionado
        if (state.selectedTrip == null) {
            _state.update { it.copy(tripError = "Selecciona un viaje") }
            return false
        }

        // Validar monto
        validateRequestedBudget(state.requestedBudget)
        if (state.requestedBudgetError != null) {
            return false
        }

        // Validar razón
        validateReason(state.reason)
        if (state.reasonError != null) {
            return false
        }

        return true
    }

    /**
     * Envía la solicitud de aumento de presupuesto.
     *
     * TODO: Reemplazar con:
     * val result = budgetRequestRepository.createBudgetRequest(
     *     BudgetRequestDTO(...)
     * )
     */
    fun submitRequest() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }

            try {
                Log.d(TAG, "Submitting budget request")

                val state = _state.value

                // Simular delay de API
                delay(2000)

                // TODO: Llamada real a API
                // val request = BudgetRequestDTO(
                //     tripId = state.selectedTrip!!.id,
                //     currentBudget = state.currentBudget,
                //     requestedBudget = state.requestedBudget.toDouble(),
                //     reason = state.reason,
                //     urgency = state.urgency,
                //     requestedBy = state.currentUserId,
                //     requestedByName = state.currentUserName
                // )
                // val result = repository.createBudgetRequest(request)

                // Mock: Simular éxito
                Log.d(TAG, "Budget request submitted successfully")

                _state.update {
                    it.copy(
                        isSubmitting = false,
                        submissionSuccess = true
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error submitting budget request", e)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Error al enviar solicitud: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Limpia el mensaje de error.
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Resetea el formulario después de enviar.
     */
    fun resetForm() {
        _state.update {
            BudgetRequestState(
                currentUserId = it.currentUserId,
                currentUserName = it.currentUserName,
                availableTrips = it.availableTrips,
                isLoadingTrips = false
            )
        }
    }

    // ==========================================
    // DATOS MOCK (Remover cuando API esté lista)
    // ==========================================

    /**
     * Obtiene viajes activos mock (solo PENDING e IN_PROGRESS).
     */
    private fun getMockActiveTrips(userEmail: String): List<Trip> {
        val now = Instant.now()

        return listOf(
            // Viaje 1: EN PROGRESO - con gastos cerca del límite
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
                    specialRequirements = "Manejo con cuidado"
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
                statusHistory = emptyList(),
                totalExpenses = 13500.0, // 90% del presupuesto usado
                remainingBudget = 1500.0,
                expenseCount = 8,
                budgetIncreaseCount = 0,
                createdAt = now.minus(3, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(1, ChronoUnit.DAYS).toString(),
                completedAt = null
            ),

            // Viaje 2: PENDIENTE - sin gastos aún
            Trip(
                id = "trip_002",
                cargoName = "Transporte de Maquinaria - CEMEX",
                origin = Location(
                    address = "CEMEX Planta Monterrey",
                    city = "Monterrey",
                    state = "Nuevo León",
                    zipCode = "64000",
                    coordinates = Coordinates(25.6866, -100.3161)
                ),
                destination = Location(
                    address = "Construcción Torre Reforma",
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
                statusHistory = emptyList(),
                totalExpenses = 0.0,
                remainingBudget = 25000.0,
                expenseCount = 0,
                budgetIncreaseCount = 0,
                createdAt = now.toString(),
                updatedAt = now.toString(),
                completedAt = null
            ),

            // Viaje 5: EN PROGRESO - ya tuvo un aumento
            Trip(
                id = "trip_005",
                cargoName = "Importación de Autopartes - General Motors",
                origin = Location(
                    address = "GM Planta Ramos Arizpe",
                    city = "Ramos Arizpe",
                    state = "Coahuila",
                    zipCode = "25900",
                    coordinates = Coordinates(25.5407, -100.9584)
                ),
                destination = Location(
                    address = "GM Planta Silao",
                    city = "Silao",
                    state = "Guanajuato",
                    zipCode = "36100",
                    coordinates = Coordinates(20.9253, -101.4295)
                ),
                cargo = Cargo(
                    type = "Autopartes",
                    weight = 6500.0,
                    description = "Componentes de transmisión",
                    specialRequirements = "Alto valor"
                ),
                budget = Budget(
                    initial = 18000.0,
                    current = 20000.0, // Ya se aumentó
                    currency = "MXN",
                    history = listOf(
                        BudgetIncrease(
                            previousAmount = 18000.0,
                            newAmount = 20000.0,
                            increase = 2000.0,
                            reason = "Mantenimiento imprevisto",
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
                statusHistory = emptyList(),
                totalExpenses = 17500.0, // 87.5% del nuevo presupuesto
                remainingBudget = 2500.0,
                expenseCount = 10,
                budgetIncreaseCount = 1,
                createdAt = now.minus(4, ChronoUnit.DAYS).toString(),
                updatedAt = now.minus(1, ChronoUnit.HOURS).toString(),
                completedAt = null
            )
        )
    }
}