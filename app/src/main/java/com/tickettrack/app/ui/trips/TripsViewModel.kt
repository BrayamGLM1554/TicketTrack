package com.tickettrack.app.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.CreateTripRequest
import com.tickettrack.app.domain.model.TransportistaAvailable
import com.tickettrack.app.domain.model.Trip
import com.tickettrack.app.domain.model.TripStatus
import com.tickettrack.app.domain.repository.ITripsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripsUiState(
    val isLoading: Boolean = false,
    val trips: List<Trip> = emptyList(),
    val filteredTrips: List<Trip> = emptyList(),
    val error: String? = null,
    val selectedFilter: TripStatus? = null,

    // Para crear viaje
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false,
    val createError: String? = null,
    val availableTransportistas: List<TransportistaAvailable> = emptyList(),
    val isLoadingTransportistas: Boolean = false
)

class TripsViewModel(
    private val repository: ITripsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    fun loadTrips(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = repository.getTrips(token)

            result.fold(
                onSuccess = { trips ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        trips = trips,
                        filteredTrips = trips
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

    fun filterByStatus(status: TripStatus?) {
        val filtered = if (status == null) {
            _uiState.value.trips
        } else {
            _uiState.value.trips.filter { it.status == status }
        }

        _uiState.value = _uiState.value.copy(
            selectedFilter = status,
            filteredTrips = filtered
        )
    }

// En TripsViewModel.kt, reemplaza la función loadAvailableTransportistas:

    fun loadAvailableTransportistas(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingTransportistas = true)

            // Primero asegurarse de tener los viajes cargados
            if (_uiState.value.trips.isEmpty()) {
                val tripsResult = repository.getTrips(token)
                tripsResult.fold(
                    onSuccess = { trips ->
                        _uiState.value = _uiState.value.copy(trips = trips)
                    },
                    onFailure = { }
                )
            }

            val result = repository.getAvailableTransportistas(token)

            result.fold(
                onSuccess = { transportistas ->
                    // Filtrar transportistas que NO están en viajes activos
                    val busyUids = _uiState.value.trips
                        .filter { it.status == TripStatus.PENDING || it.status == TripStatus.IN_PROGRESS }
                        .map { it.transportistaUid }
                        .toSet()

                    val available = transportistas.filter { it.uid !in busyUids }

                    _uiState.value = _uiState.value.copy(
                        isLoadingTransportistas = false,
                        availableTransportistas = available
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingTransportistas = false,
                        createError = exception.message ?: "Error al cargar transportistas"
                    )
                }
            )
        }
    }

    fun createTrip(token: String, request: CreateTripRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreating = true,
                createError = null,
                createSuccess = false
            )

            val result = repository.createTrip(token, request)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createSuccess = true
                    )
                    // Recargar viajes
                    loadTrips(token)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createError = exception.message ?: "Error al crear viaje"
                    )
                }
            )
        }
    }

    fun clearCreateState() {
        _uiState.value = _uiState.value.copy(
            createSuccess = false,
            createError = null
        )
    }
}