package com.tickettrack.app.ui.main.trips.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.data.remote.TripRetrofitClient
import com.tickettrack.app.data.repository.trip.TripRepository
import com.tickettrack.app.domain.model.trip.TripStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la lista de viajes.
 * Conectado con API real usando TripRetrofitClient.
 */
class TripListViewModel(
    private val tokenManager: TokenManager? = null
) : ViewModel() {

    companion object {
        private const val TAG = "TripListViewModel"
    }

    // Repository con API real
    private val repository = TripRepository(
        apiService = TripRetrofitClient.tripApiService
    )

    private val _state = MutableStateFlow(TripListState())
    val state: StateFlow<TripListState> = _state.asStateFlow()

    /**
     * Carga todos los viajes desde la API.
     */
    fun loadTrips(userId: String, userRole: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    currentUserId = userId,
                    currentUserRole = userRole
                )
            }

            try {
                Log.d(TAG, "Loading trips for user: $userId (role: $userRole)")

                val result = if (userRole == "ADMIN" || userRole == "consignatario") {
                    // Admin ve todos los viajes
                    repository.getTripsByAdmin(userId)
                } else {
                    // Driver ve solo sus viajes asignados
                    repository.getTripsByDriver(userId)
                }

                result.onSuccess { trips ->
                    Log.d(TAG, "Trips loaded successfully: ${trips.size}")
                    _state.update {
                        it.copy(
                            trips = trips,
                            isLoading = false,
                            isEmpty = trips.isEmpty(),
                            errorMessage = null
                        )
                    }
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error loading trips: ${error.message}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al cargar viajes"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception loading trips", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}"
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

            try {
                val userId = _state.value.currentUserId
                val userRole = _state.value.currentUserRole

                val result = if (userRole == "ADMIN" || userRole == "consignatario") {
                    repository.getTripsByAdmin(userId)
                } else {
                    repository.getTripsByDriver(userId)
                }

                result.onSuccess { trips ->
                    _state.update {
                        it.copy(
                            trips = trips,
                            isRefreshing = false,
                            isEmpty = trips.isEmpty()
                        )
                    }
                }

                result.onFailure { error ->
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = error.message
                        )
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    /**
     * Filtra viajes por estado.
     */
    fun filterByStatus(status: TripStatus?) {
        _state.update { it.copy(selectedStatusFilter = status) }
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
     * Limpia el mensaje de error.
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}