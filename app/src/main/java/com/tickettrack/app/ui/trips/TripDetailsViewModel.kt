package com.tickettrack.app.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.Expense
import com.tickettrack.app.domain.model.Trip
import com.tickettrack.app.domain.model.TripStatus
import com.tickettrack.app.domain.repository.IExpensesRepository
import com.tickettrack.app.domain.repository.ITripsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripDetailsUiState(
    val isLoading: Boolean = false,
    val trip: Trip? = null,
    val error: String? = null,
    val isLoadingExpenses: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val expensesError: String? = null,
    val isUpdatingStatus: Boolean = false,
    val statusUpdateError: String? = null,
    val statusUpdateSuccess: Boolean = false,
    val userRole: String = ""
)

class TripDetailsViewModel(
    private val repository: ITripsRepository,
    private val expensesRepository: IExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailsUiState())
    val uiState: StateFlow<TripDetailsUiState> = _uiState.asStateFlow()

    fun loadTripDetails(token: String, tripId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val result = repository.getTripById(token, tripId)

            result.fold(
                onSuccess = { trip ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        trip = trip
                    )
                    loadTripExpenses(token, tripId)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar detalles del viaje"
                    )
                }
            )
        }
    }

    private fun loadTripExpenses(token: String, tripId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingExpenses = true,
                expensesError = null
            )

            val result = expensesRepository.getExpenses(
                token = token,
                tripId = tripId,
                limit = 100
            )

            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingExpenses = false,
                        expenses = response.expenses
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingExpenses = false,
                        expensesError = exception.message ?: "Error al cargar gastos"
                    )
                }
            )
        }
    }

    fun startTrip(token: String, tripId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdatingStatus = true,
                statusUpdateError = null,
                statusUpdateSuccess = false
            )

            val result = repository.updateTripStatus(
                token = token,
                tripId = tripId,
                newStatus = TripStatus.IN_PROGRESS
            )

            result.fold(
                onSuccess = { updatedTrip ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingStatus = false,
                        statusUpdateSuccess = true,
                        trip = updatedTrip
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingStatus = false,
                        statusUpdateError = exception.message ?: "Error al iniciar viaje"
                    )
                }
            )
        }
    }

    fun completeTrip(token: String, tripId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdatingStatus = true,
                statusUpdateError = null,
                statusUpdateSuccess = false
            )

            val result = repository.updateTripStatus(
                token = token,
                tripId = tripId,
                newStatus = TripStatus.COMPLETED
            )

            result.fold(
                onSuccess = { updatedTrip ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingStatus = false,
                        statusUpdateSuccess = true,
                        trip = updatedTrip
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingStatus = false,
                        statusUpdateError = exception.message ?: "Error al completar viaje"
                    )
                }
            )
        }
    }

    fun clearStatusUpdateState() {
        _uiState.value = _uiState.value.copy(
            statusUpdateSuccess = false,
            statusUpdateError = null
        )
    }
}