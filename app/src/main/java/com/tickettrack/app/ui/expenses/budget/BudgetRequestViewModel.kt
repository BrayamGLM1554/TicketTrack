package com.tickettrack.app.ui.expenses.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.repository.expense.BudgetRequestRepository
import com.tickettrack.app.domain.model.expense.BudgetRequest
import com.tickettrack.app.domain.validator.ExpenseValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de solicitudes de presupuesto
 * HU20 - Ver y gestionar solicitudes de aumento de presupuesto
 */
class BudgetRequestViewModel(
    private val repository: BudgetRequestRepository = BudgetRequestRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetRequestState())
    val state: StateFlow<BudgetRequestState> = _state.asStateFlow()

    /**
     * Carga las solicitudes pendientes (para consignatario)
     */
    fun loadPendingRequests(companyEmail: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = repository.getPendingRequests(companyEmail)

                result.onSuccess { requests ->
                    _state.value = _state.value.copy(
                        requests = requests,
                        pendingCount = requests.count { it.isPending() },
                        isLoading = false
                    )
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar solicitudes"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Carga las solicitudes del usuario (para transportista)
     */
    fun loadUserRequests(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = repository.getRequestsByUser(userId)

                result.onSuccess { requests ->
                    _state.value = _state.value.copy(
                        requests = requests,
                        pendingCount = requests.count { it.isPending() },
                        isLoading = false
                    )
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar solicitudes"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Refresca las solicitudes
     */
    fun refreshRequests(email: String, isConsignatario: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)

            try {
                val result = if (isConsignatario) {
                    repository.getPendingRequests(email)
                } else {
                    repository.getRequestsByUser(email)
                }

                result.onSuccess { requests ->
                    _state.value = _state.value.copy(
                        requests = requests,
                        pendingCount = requests.count { it.isPending() },
                        isRefreshing = false
                    )
                }.onFailure {
                    _state.value = _state.value.copy(isRefreshing = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isRefreshing = false)
            }
        }
    }

    /**
     * Expande/colapsa un card de solicitud
     */
    fun toggleRequestExpansion(requestId: String) {
        _state.value = _state.value.copy(
            expandedRequestId = if (_state.value.expandedRequestId == requestId) null else requestId
        )
    }

    /**
     * Muestra el diálogo de aprobación
     */
    fun showApproveDialog(request: BudgetRequest) {
        _state.value = _state.value.copy(
            requestToReview = request,
            isApproving = true,
            reviewNote = "",
            reviewNoteError = null
        )
    }

    /**
     * Muestra el diálogo de rechazo
     */
    fun showRejectDialog(request: BudgetRequest) {
        _state.value = _state.value.copy(
            requestToReview = request,
            isApproving = false,
            reviewNote = "",
            reviewNoteError = null
        )
    }

    /**
     * Oculta el diálogo de revisión
     */
    fun hideReviewDialog() {
        _state.value = _state.value.copy(
            requestToReview = null,
            reviewNote = "",
            reviewNoteError = null
        )
    }

    /**
     * Actualiza la nota de revisión
     */
    fun onReviewNoteChanged(note: String) {
        _state.value = _state.value.copy(
            reviewNote = note,
            reviewNoteError = null
        )
    }

    /**
     * Aprueba una solicitud
     */
    fun approveRequest(
        requestId: String,
        reviewNote: String,
        reviewedBy: String,
        reviewedByName: String,
        companyEmail: String
    ) {
        viewModelScope.launch {
            // Validar nota (opcional para aprobación)
            val validation = ExpenseValidator.validateReviewNote(reviewNote, false)
            if (!validation.isValid) {
                _state.value = _state.value.copy(reviewNoteError = validation.errorMessage)
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true)

            try {
                val result = repository.approveRequest(
                    requestId = requestId,
                    reviewNote = reviewNote,
                    reviewedBy = reviewedBy,
                    reviewedByName = reviewedByName
                )

                result.onSuccess {
                    // Recargar solicitudes
                    loadPendingRequests(companyEmail)
                    _state.value = _state.value.copy(
                        requestToReview = null,
                        reviewNote = "",
                        isLoading = false
                    )
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al aprobar solicitud"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Rechaza una solicitud
     */
    fun rejectRequest(
        requestId: String,
        reviewNote: String,
        reviewedBy: String,
        reviewedByName: String,
        companyEmail: String
    ) {
        viewModelScope.launch {
            // Validar nota (obligatoria para rechazo)
            val validation = ExpenseValidator.validateReviewNote(reviewNote, true)
            if (!validation.isValid) {
                _state.value = _state.value.copy(reviewNoteError = validation.errorMessage)
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true)

            try {
                val result = repository.rejectRequest(
                    requestId = requestId,
                    reviewNote = reviewNote,
                    reviewedBy = reviewedBy,
                    reviewedByName = reviewedByName
                )

                result.onSuccess {
                    // Recargar solicitudes
                    loadPendingRequests(companyEmail)
                    _state.value = _state.value.copy(
                        requestToReview = null,
                        reviewNote = "",
                        isLoading = false
                    )
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al rechazar solicitud"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}