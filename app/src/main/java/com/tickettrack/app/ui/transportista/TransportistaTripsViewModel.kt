package com.tickettrack.app.ui.transportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.Trip
import com.tickettrack.app.domain.repository.ITripsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransportistaTripsUiState(
    val isLoading: Boolean = false,
    val trips: List<Trip> = emptyList(),
    val error: String? = null
)

class TransportistaTripsViewModel(
    private val repository: ITripsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportistaTripsUiState())
    val uiState: StateFlow<TransportistaTripsUiState> = _uiState.asStateFlow()

    fun loadTrips(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Usar el nuevo endpoint para usuarios USER
            val result = repository.getAssignedTrips(token)

            result.fold(
                onSuccess = { trips ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        trips = trips
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar viajes"
                    )
                }
            )
        }
    }
}