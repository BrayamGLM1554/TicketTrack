package com.tickettrack.app.ui.trips.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.data.remote.TripRetrofitClient
import com.tickettrack.app.data.repository.trip.TripRepository
import com.tickettrack.app.domain.model.trip.TripStatus
import com.tickettrack.app.domain.model.trip.Urgency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle de viaje.
 * Conectado con API real usando TripRetrofitClient.
 */
class TripDetailViewModel(
    private val tripId: String,
    private val tokenManager: TokenManager? = null
) : ViewModel() {

    companion object {
        private const val TAG = "TripDetailViewModel"
    }

    // Repository con API real
    private val repository = TripRepository(
        apiService = TripRetrofitClient.tripApiService
    )

    private val _state = MutableStateFlow(TripDetailState())
    val state: StateFlow<TripDetailState> = _state.asStateFlow()

    init {
        loadTripDetails()
    }

    // ==========================================
    // Cargar Detalles del Viaje
    // ==========================================

    /**
     * Carga los detalles del viaje desde la API.
     */
    fun loadTripDetails() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingTrip = true,
                    errorMessage = null,
                    currentUserId = tokenManager?.getUserEmail() ?: "",
                    currentUserName = tokenManager?.getUserName() ?: "",
                    currentUserRole = tokenManager?.getUserRole() ?: ""
                )
            }

            try {
                Log.d(TAG, "Loading trip details: $tripId")

                val result = repository.getTripById(tripId)

                result.onSuccess { trip ->
                    if (trip != null) {
                        Log.d(TAG, "Trip loaded successfully: ${trip.cargoName}")

                        _state.update {
                            it.copy(
                                trip = trip,
                                isLoadingTrip = false,
                                errorMessage = null
                            )
                        }

                        // Cargar info del transportista (si lo necesitas)
                        // loadDriver(trip.assignedDriverId)

                    } else {
                        Log.e(TAG, "Trip not found: $tripId")
                        _state.update {
                            it.copy(
                                isLoadingTrip = false,
                                errorMessage = "Viaje no encontrado"
                            )
                        }
                    }
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error loading trip: ${error.message}")
                    _state.update {
                        it.copy(
                            isLoadingTrip = false,
                            errorMessage = "Error al cargar viaje: ${error.message}"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception loading trip", e)
                _state.update {
                    it.copy(
                        isLoadingTrip = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Refresca los detalles del viaje.
     */
    fun refresh() {
        loadTripDetails()
    }

    // ==========================================
    // Secciones Expandibles
    // ==========================================

    fun toggleBudgetHistory() {
        _state.update { it.copy(isBudgetHistoryExpanded = !it.isBudgetHistoryExpanded) }
    }

    fun toggleStatusHistory() {
        _state.update { it.copy(isStatusHistoryExpanded = !it.isStatusHistoryExpanded) }
    }

    // ==========================================
    // Aumentar Presupuesto (Mock)
    // ==========================================

    fun showIncreaseBudgetDialog() {
        val currentBudget = _state.value.trip?.budget?.current ?: 0.0

        _state.update {
            it.copy(
                showIncreaseBudgetDialog = true,
                newBudgetAmount = currentBudget.toString(),
                increaseReason = "",
                selectedUrgency = Urgency.MEDIUM,
                newBudgetAmountError = null,
                increaseReasonError = null
            )
        }
    }

    fun hideIncreaseBudgetDialog() {
        _state.update { it.copy(showIncreaseBudgetDialog = false) }
    }

    fun onNewBudgetAmountChanged(value: String) {
        _state.update { it.copy(newBudgetAmount = value, newBudgetAmountError = null) }
    }

    fun onIncreaseReasonChanged(value: String) {
        _state.update { it.copy(increaseReason = value, increaseReasonError = null) }
    }

    fun onUrgencySelected(urgency: Urgency) {
        _state.update { it.copy(selectedUrgency = urgency) }
    }

    /**
     * Aumenta el presupuesto del viaje.
     * ⚠️ MOCK: Usa cache local hasta que exista endpoint en API.
     */
    fun increaseBudget() {
        val currentState = _state.value
        val trip = currentState.trip ?: return

        // Validar monto
        val newAmount = currentState.newBudgetAmount.toDoubleOrNull()
        if (newAmount == null || newAmount <= 0) {
            _state.update { it.copy(newBudgetAmountError = "Ingrese un monto válido") }
            return
        }

        // Validar que el nuevo monto sea mayor al actual
        if (newAmount <= trip.budget.current) {
            _state.update {
                it.copy(newBudgetAmountError = "El nuevo monto debe ser mayor al actual ($${trip.budget.current})")
            }
            return
        }

        // Validar razón
        if (currentState.increaseReason.isBlank()) {
            _state.update { it.copy(increaseReasonError = "La razón es requerida") }
            return
        }

        if (currentState.increaseReason.length < 10) {
            _state.update { it.copy(increaseReasonError = "La razón debe tener al menos 10 caracteres") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isIncreasingBudget = true, errorMessage = null) }

            try {
                Log.d(TAG, "[MOCK] Increasing budget for trip: $tripId")

                val result = repository.increaseBudget(
                    tripId = tripId,
                    newAmount = newAmount,
                    reason = currentState.increaseReason,
                    urgency = currentState.selectedUrgency,
                    requestedBy = currentState.currentUserId,
                    requestedByName = currentState.currentUserName
                )

                result.onSuccess { updatedTrip ->
                    Log.d(TAG, "[MOCK] Budget increased successfully")
                    _state.update {
                        it.copy(
                            trip = updatedTrip,
                            isIncreasingBudget = false,
                            showIncreaseBudgetDialog = false,
                            newBudgetAmount = "",
                            increaseReason = "",
                            selectedUrgency = Urgency.MEDIUM
                        )
                    }
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error increasing budget: ${error.message}")
                    _state.update {
                        it.copy(
                            isIncreasingBudget = false,
                            errorMessage = "Error al aumentar presupuesto: ${error.message}"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception increasing budget", e)
                _state.update {
                    it.copy(
                        isIncreasingBudget = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    // ==========================================
    // Cambiar Estado (Mock)
    // ==========================================

    /**
     * Inicia el viaje (pending → in_progress).
     */
    fun startTrip() {
        changeStatus(TripStatus.IN_PROGRESS)
    }

    /**
     * Finaliza el viaje (in_progress → completed).
     */
    fun completeTrip() {
        changeStatus(TripStatus.COMPLETED)
    }

    /**
     * Cancela el viaje.
     */
    fun cancelTrip() {
        changeStatus(TripStatus.CANCELLED)
    }

    /**
     * Cambia el estado del viaje.
     * ⚠️ MOCK: Usa cache local hasta que exista endpoint en API.
     */
    private fun changeStatus(newStatus: TripStatus) {
        viewModelScope.launch {
            _state.update { it.copy(isChangingStatus = true, errorMessage = null) }

            try {
                val currentState = _state.value

                Log.d(TAG, "[MOCK] Changing status for trip: $tripId to $newStatus")

                val result = repository.updateTripStatus(
                    tripId = tripId,
                    newStatus = newStatus,
                    changedBy = currentState.currentUserId,
                    changedByName = currentState.currentUserName
                )

                result.onSuccess { updatedTrip ->
                    Log.d(TAG, "[MOCK] Status changed successfully")
                    _state.update {
                        it.copy(
                            trip = updatedTrip,
                            isChangingStatus = false,
                            statusChangeSuccess = true
                        )
                    }

                    // Reset success después de 2 segundos
                    kotlinx.coroutines.delay(2000)
                    _state.update { it.copy(statusChangeSuccess = false) }
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error changing status: ${error.message}")
                    _state.update {
                        it.copy(
                            isChangingStatus = false,
                            errorMessage = "Error al cambiar estado: ${error.message}"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception changing status", e)
                _state.update {
                    it.copy(
                        isChangingStatus = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    // ==========================================
    // Helpers
    // ==========================================

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Verifica si el usuario puede aumentar presupuesto.
     */
    fun canIncreaseBudget(): Boolean {
        return _state.value.canIncreaseBudget()
    }

    /**
     * Verifica si el usuario puede cambiar el estado.
     */
    fun canChangeStatus(): Boolean {
        return _state.value.canChangeStatus()
    }
}