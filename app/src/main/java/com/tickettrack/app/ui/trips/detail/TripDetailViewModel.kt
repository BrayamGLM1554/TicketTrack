package com.tickettrack.app.ui.trips.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.model.trip.IncreaseBudgetRequest
import com.tickettrack.app.data.model.trip.UpdateTripStatusRequest
import com.tickettrack.app.data.model.trip.toDomain
import com.tickettrack.app.data.repository.trip.DriverRepository
import com.tickettrack.app.data.repository.trip.TripRepository
import com.tickettrack.app.domain.model.trip.TripStatus
import com.tickettrack.app.domain.model.trip.Urgency
import com.tickettrack.app.domain.validator.TripValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * ViewModel para la pantalla de detalle de viaje.
 *
 * Maneja:
 * - Carga del viaje y transportista
 * - Aumento de presupuesto
 * - Cambio de estado del viaje
 * - Expansión de secciones
 */
class TripDetailViewModel(
    private val tripRepository: TripRepository = TripRepository(),
    private val driverRepository: DriverRepository = DriverRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "TripDetailViewModel"
    }

    private val _state = MutableStateFlow(TripDetailState())
    val state: StateFlow<TripDetailState> = _state.asStateFlow()

    /**
     * Carga el viaje por ID.
     */
    fun loadTrip(tripId: String, userId: String, userName: String, userRole: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading trip: $tripId")

                _state.update { it.copy(
                    isLoadingTrip = true,
                    currentUserId = userId,
                    currentUserName = userName,
                    currentUserRole = userRole
                )}

                val result = tripRepository.getTripById(tripId)

                result.fold(
                    onSuccess = { tripResponse ->
                        if (tripResponse != null) {
                            val trip = tripResponse.toDomain()

                            _state.update { it.copy(
                                trip = trip,
                                isLoadingTrip = false
                            )}

                            // Cargar datos del transportista
                            loadDriver(trip.assignedDriverId)

                            Log.d(TAG, "Trip loaded successfully")
                        } else {
                            _state.update { it.copy(
                                isLoadingTrip = false,
                                errorMessage = "Viaje no encontrado"
                            )}
                        }
                    },
                    onFailure = { exception ->
                        _state.update { it.copy(
                            isLoadingTrip = false,
                            errorMessage = "Error al cargar viaje: ${exception.message}"
                        )}

                        Log.e(TAG, "Error loading trip", exception)
                    }
                )

            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoadingTrip = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )}

                Log.e(TAG, "Unexpected error loading trip", e)
            }
        }
    }

    /**
     * Carga los datos del transportista.
     */
    private fun loadDriver(driverId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading driver: $driverId")

                _state.update { it.copy(isLoadingDriver = true) }

                val result = driverRepository.getDriverById(driverId)

                result.fold(
                    onSuccess = { driver ->
                        _state.update { it.copy(
                            driver = driver,
                            isLoadingDriver = false
                        )}

                        Log.d(TAG, "Driver loaded successfully")
                    },
                    onFailure = { exception ->
                        _state.update { it.copy(
                            isLoadingDriver = false,
                            errorMessage = "Error al cargar transportista: ${exception.message}"
                        )}

                        Log.e(TAG, "Error loading driver", exception)
                    }
                )

            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoadingDriver = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )}

                Log.e(TAG, "Unexpected error loading driver", e)
            }
        }
    }

    // ==================== SECCIONES EXPANDIBLES ====================

    fun toggleBudgetHistory() {
        _state.update { it.copy(isBudgetHistoryExpanded = !it.isBudgetHistoryExpanded) }
    }

    fun toggleStatusHistory() {
        _state.update { it.copy(isStatusHistoryExpanded = !it.isStatusHistoryExpanded) }
    }

    // ==================== AUMENTAR PRESUPUESTO ====================

    fun showIncreaseBudgetDialog() {
        val currentBudget = _state.value.trip?.budget?.current ?: 0.0

        _state.update { it.copy(
            showIncreaseBudgetDialog = true,
            newBudgetAmount = currentBudget.toString(),
            increaseReason = "",
            selectedUrgency = Urgency.MEDIUM,
            newBudgetAmountError = null,
            increaseReasonError = null
        )}
    }

    fun hideIncreaseBudgetDialog() {
        _state.update { it.copy(showIncreaseBudgetDialog = false) }
    }

    fun onNewBudgetAmountChanged(value: String) {
        _state.update { it.copy(
            newBudgetAmount = value,
            newBudgetAmountError = null
        )}
    }

    fun validateNewBudgetAmount() {
        val currentAmount = _state.value.trip?.budget?.current ?: 0.0
        val result = TripValidator.validateNewBudget(_state.value.newBudgetAmount, currentAmount)
        _state.update { it.copy(newBudgetAmountError = result.errorMessage) }
    }

    fun onIncreaseReasonChanged(value: String) {
        _state.update { it.copy(
            increaseReason = value,
            increaseReasonError = null
        )}
    }

    fun validateIncreaseReason() {
        val result = TripValidator.validateIncreaseReason(_state.value.increaseReason)
        _state.update { it.copy(increaseReasonError = result.errorMessage) }
    }

    fun onUrgencySelected(urgency: Urgency) {
        _state.update { it.copy(selectedUrgency = urgency) }
    }

    /**
     * Aumenta el presupuesto del viaje.
     */
    fun increaseBudget() {
        viewModelScope.launch {
            try {
                val currentState = _state.value
                val trip = currentState.trip ?: return@launch

                // Validar
                validateNewBudgetAmount()
                validateIncreaseReason()

                if (!currentState.isIncreaseBudgetFormValid()) {
                    Log.w(TAG, "Validation failed")
                    return@launch
                }

                Log.d(TAG, "Increasing budget for trip: ${trip.id}")

                _state.update { it.copy(isIncreasingBudget = true) }

                val newAmount = currentState.newBudgetAmount.toDouble()
                val currentAmount = trip.budget.current
                val increase = newAmount - currentAmount

                val request = IncreaseBudgetRequest(
                    tripId = trip.id,
                    previousAmount = currentAmount,
                    newAmount = newAmount,
                    increase = increase,
                    reason = currentState.increaseReason,
                    requestedBy = currentState.currentUserId,
                    requestedByName = currentState.currentUserName,
                    approvedBy = currentState.currentUserId,
                    approvedByName = currentState.currentUserName,
                    urgency = currentState.selectedUrgency.toFirebaseString(),
                    supportingExpenses = emptyList()
                )

                val result = tripRepository.increaseBudget(request)

                result.fold(
                    onSuccess = { tripResponse ->
                        val updatedTrip = tripResponse.toDomain()

                        _state.update { it.copy(
                            trip = updatedTrip,
                            isIncreasingBudget = false,
                            showIncreaseBudgetDialog = false
                        )}

                        Log.d(TAG, "Budget increased successfully")
                    },
                    onFailure = { exception ->
                        _state.update { it.copy(
                            isIncreasingBudget = false,
                            errorMessage = "Error al aumentar presupuesto: ${exception.message}"
                        )}

                        Log.e(TAG, "Error increasing budget", exception)
                    }
                )

            } catch (e: Exception) {
                _state.update { it.copy(
                    isIncreasingBudget = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )}

                Log.e(TAG, "Unexpected error increasing budget", e)
            }
        }
    }

    // ==================== CAMBIAR ESTADO ====================

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
     */
    private fun changeStatus(newStatus: TripStatus) {
        viewModelScope.launch {
            try {
                val currentState = _state.value
                val trip = currentState.trip ?: return@launch

                Log.d(TAG, "Changing trip status to: ${newStatus.name}")

                _state.update { it.copy(isChangingStatus = true) }

                val request = UpdateTripStatusRequest(
                    tripId = trip.id,
                    newStatus = newStatus.toFirebaseString(),
                    changedBy = currentState.currentUserId,
                    changedByName = currentState.currentUserName
                )

                val result = tripRepository.updateTripStatus(request)

                result.fold(
                    onSuccess = { tripResponse ->
                        val updatedTrip = tripResponse.toDomain()

                        _state.update { it.copy(
                            trip = updatedTrip,
                            isChangingStatus = false,
                            statusChangeSuccess = true
                        )}

                        Log.d(TAG, "Status changed successfully")

                        // Reset success después de 2 segundos
                        kotlinx.coroutines.delay(2000)
                        _state.update { it.copy(statusChangeSuccess = false) }
                    },
                    onFailure = { exception ->
                        _state.update { it.copy(
                            isChangingStatus = false,
                            errorMessage = "Error al cambiar estado: ${exception.message}"
                        )}

                        Log.e(TAG, "Error changing status", exception)
                    }
                )

            } catch (e: Exception) {
                _state.update { it.copy(
                    isChangingStatus = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )}

                Log.e(TAG, "Unexpected error changing status", e)
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
     * Resetea el estado.
     */
    fun resetState() {
        _state.value = TripDetailState()
    }
}