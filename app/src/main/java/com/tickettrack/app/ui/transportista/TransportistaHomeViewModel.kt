package com.tickettrack.app.ui.transportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.Trip
import com.tickettrack.app.domain.model.TripStatus
import com.tickettrack.app.domain.repository.IExpensesRepository
import com.tickettrack.app.domain.repository.ITripsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransportistaHomeUiState(
    val isLoading: Boolean = false,
    val currentTrip: Trip? = null,
    val totalExpenses: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val error: String? = null
)

class TransportistaHomeViewModel(
    private val tripsRepository: ITripsRepository,
    private val expensesRepository: IExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportistaHomeUiState())
    val uiState: StateFlow<TransportistaHomeUiState> = _uiState.asStateFlow()

    fun loadCurrentTrip(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Usar el nuevo endpoint para usuarios USER
            val tripsResult = tripsRepository.getAssignedTrips(token)

            tripsResult.fold(
                onSuccess = { trips ->
                    // Buscar el viaje activo (PENDING o IN_PROGRESS)
                    val activeTrip = trips.firstOrNull {
                        it.status == TripStatus.PENDING || it.status == TripStatus.IN_PROGRESS
                    }

                    if (activeTrip != null) {
                        loadTripExpenses(token, activeTrip)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentTrip = null,
                            totalExpenses = 0.0,
                            remainingBudget = 0.0
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar viaje actual"
                    )
                }
            )
        }
    }

    private suspend fun loadTripExpenses(token: String, trip: Trip) {
        val expensesResult = expensesRepository.getExpenses(
            token = token,
            tripId = trip.id,
            limit = 100
        )

        expensesResult.fold(
            onSuccess = { response ->
                val totalExpenses = response.expenses.sumOf { it.amount }
                val remainingBudget = trip.budgetAssigned - totalExpenses

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTrip = trip,
                    totalExpenses = totalExpenses,
                    remainingBudget = remainingBudget
                )
            },
            onFailure = {
                // Si falla cargar gastos, al menos mostramos el viaje
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTrip = trip,
                    totalExpenses = 0.0,
                    remainingBudget = trip.budgetAssigned
                )
            }
        )
    }
}