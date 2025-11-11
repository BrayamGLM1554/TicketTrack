package com.tickettrack.app.ui.drivers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.CreateDriverRequest
import com.tickettrack.app.domain.model.Driver
import com.tickettrack.app.domain.model.UpdateDriverRequest
import com.tickettrack.app.domain.repository.IDriversRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DriversUiState(
    val isLoading: Boolean = false,
    val drivers: List<Driver> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false,
    val createError: String? = null,

    // Nuevos estados para detalles y edición
    val selectedDriver: Driver? = null,
    val isLoadingDetails: Boolean = false,
    val detailsError: String? = null,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val updateError: String? = null
)

class DriversViewModel(
    private val repository: IDriversRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriversUiState())
    val uiState: StateFlow<DriversUiState> = _uiState.asStateFlow()

    fun loadDrivers(token: String, search: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = repository.getDrivers(
                token = token,
                page = _uiState.value.currentPage,
                limit = 20,
                search = search
            )

            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        drivers = response.data,
                        totalPages = response.pagination.totalPages,
                        searchQuery = search ?: ""
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar transportistas"
                    )
                }
            )
        }
    }

    fun searchDrivers(token: String, query: String) {
        _uiState.value = _uiState.value.copy(currentPage = 1)
        loadDrivers(token, query.ifBlank { null })
    }

    fun createDriver(token: String, request: CreateDriverRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreating = true,
                createError = null,
                createSuccess = false
            )

            val result = repository.createDriver(token, request)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createSuccess = true
                    )
                    // Recargar lista
                    loadDrivers(token)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createError = exception.message ?: "Error al crear transportista"
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

    fun loadDriverDetails(token: String, uid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingDetails = true,
                detailsError = null
            )

            val result = repository.getDriverDetails(token, uid)

            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingDetails = false,
                        selectedDriver = response.data
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingDetails = false,
                        detailsError = exception.message ?: "Error al cargar detalles"
                    )
                }
            )
        }
    }

    fun updateDriver(token: String, uid: String, request: UpdateDriverRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdating = true,
                updateError = null,
                updateSuccess = false
            )

            val result = repository.updateDriver(token, uid, request)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateSuccess = true
                    )
                    // Recargar detalles
                    loadDriverDetails(token, uid)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateError = exception.message ?: "Error al actualizar"
                    )
                }
            )
        }
    }

    fun clearUpdateState() {
        _uiState.value = _uiState.value.copy(
            updateSuccess = false,
            updateError = null
        )
    }

    fun clearSelectedDriver() {
        _uiState.value = _uiState.value.copy(
            selectedDriver = null,
            detailsError = null
        )
    }

}