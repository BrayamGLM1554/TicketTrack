package com.tickettrack.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.DashboardResponse
import com.tickettrack.app.domain.repository.IReportsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val dashboard: DashboardResponse? = null,
    val error: String? = null,
    val selectedPeriod: String = "today"
)

class DashboardViewModel(
    private val repository: IReportsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard(token: String, period: String = "today") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                selectedPeriod = period
            )

            val result = repository.getDashboard(token, period)

            result.fold(
                onSuccess = { dashboard ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        dashboard = dashboard
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar dashboard"
                    )
                }
            )
        }
    }

    fun changePeriod(token: String, period: String) {
        loadDashboard(token, period)
    }
}