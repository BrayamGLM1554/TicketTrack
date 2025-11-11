package com.tickettrack.app.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.ApproveBudgetRequest
import com.tickettrack.app.domain.model.BudgetRequest
import com.tickettrack.app.domain.model.RejectBudgetRequest
import com.tickettrack.app.domain.repository.IBudgetRequestsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BudgetRequestDetailsUiState(
    val isLoading: Boolean = false,
    val budgetRequest: BudgetRequest? = null,
    val error: String? = null,
    val isProcessing: Boolean = false,
    val actionSuccess: Boolean = false,
    val actionError: String? = null
)

class BudgetRequestDetailsViewModel(
    private val repository: IBudgetRequestsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetRequestDetailsUiState())
    val uiState: StateFlow<BudgetRequestDetailsUiState> = _uiState.asStateFlow()

    fun loadBudgetRequestDetails(token: String, requestId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val result = repository.getBudgetRequestById(token, requestId)

            result.fold(
                onSuccess = { request ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        budgetRequest = request
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar detalles de la solicitud"
                    )
                }
            )
        }
    }

    fun approveBudgetRequest(
        token: String,
        requestId: String,
        approvedBudget: Double?,
        approvalNotes: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                actionError = null
            )

            val request = ApproveBudgetRequest(
                approvedBudget = approvedBudget,
                approvalNotes = approvalNotes
            )

            val result = repository.approveBudgetRequest(token, requestId, request)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        actionSuccess = true
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        actionError = exception.message ?: "Error al aprobar solicitud"
                    )
                }
            )
        }
    }

    fun rejectBudgetRequest(
        token: String,
        requestId: String,
        rejectionReason: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                actionError = null
            )

            val request = RejectBudgetRequest(rejectionReason = rejectionReason)

            val result = repository.rejectBudgetRequest(token, requestId, request)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        actionSuccess = true
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        actionError = exception.message ?: "Error al rechazar solicitud"
                    )
                }
            )
        }
    }
}