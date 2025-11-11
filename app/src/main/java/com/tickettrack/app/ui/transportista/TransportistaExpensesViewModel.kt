package com.tickettrack.app.ui.transportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.Expense
import com.tickettrack.app.domain.model.TripStatus
import com.tickettrack.app.domain.repository.IExpensesRepository
import com.tickettrack.app.domain.repository.ITripsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransportistaExpensesUiState(
    val isLoading: Boolean = false,
    val currentTripId: String? = null,
    val expenses: List<Expense> = emptyList(),
    val error: String? = null
)

class TransportistaExpensesViewModel(
    private val tripsRepository: ITripsRepository,
    private val expensesRepository: IExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportistaExpensesUiState())
    val uiState: StateFlow<TransportistaExpensesUiState> = _uiState.asStateFlow()

    fun loadCurrentTripExpenses(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Primero obtener el viaje activo
            val tripsResult = tripsRepository.getTrips(token)

            tripsResult.fold(
                onSuccess = { trips ->
                    val activeTrip = trips.firstOrNull {
                        it.status == TripStatus.PENDING || it.status == TripStatus.IN_PROGRESS
                    }

                    if (activeTrip != null) {
                        loadExpenses(token, activeTrip.id)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentTripId = null,
                            expenses = emptyList()
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar gastos"
                    )
                }
            )
        }
    }

    private suspend fun loadExpenses(token: String, tripId: String) {
        val expensesResult = expensesRepository.getExpenses(
            token = token,
            tripId = tripId,
            limit = 100
        )

        expensesResult.fold(
            onSuccess = { response ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTripId = tripId,
                    expenses = response.expenses
                )
            },
            onFailure = { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTripId = tripId,
                    expenses = emptyList(),
                    error = exception.message ?: "Error al cargar gastos"
                )
            }
        )
    }
}