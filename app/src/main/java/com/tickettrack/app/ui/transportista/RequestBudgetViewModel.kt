package com.tickettrack.app.ui.transportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.CreateBudgetRequest
import com.tickettrack.app.domain.repository.IBudgetRequestsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RequestBudgetUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class RequestBudgetViewModel(
    private val budgetRequestsRepository: IBudgetRequestsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestBudgetUiState())
    val uiState: StateFlow<RequestBudgetUiState> = _uiState.asStateFlow()

    fun submitBudgetRequest(
        token: String,
        tripId: String,
        driverId: String,
        requestedBudget: Double,
        reason: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                success = false
            )

            val request = CreateBudgetRequest(
                tripId = tripId,
                driverId = driverId,
                requestedBudget = requestedBudget,
                reason = reason
            )

            budgetRequestsRepository.createBudgetRequest(token, request)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = true,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = false,
                        error = exception.message ?: "Error al enviar la solicitud"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = RequestBudgetUiState()
    }
}