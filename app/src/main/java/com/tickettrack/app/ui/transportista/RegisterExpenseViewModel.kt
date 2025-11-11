package com.tickettrack.app.ui.transportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.repository.IExpensesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class RegisterExpenseUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class RegisterExpenseViewModel(
    private val expensesRepository: IExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterExpenseUiState())
    val uiState: StateFlow<RegisterExpenseUiState> = _uiState.asStateFlow()

    fun createExpense(
        token: String,
        tripId: String,
        driverId: String,
        category: String,
        amount: Double,
        description: String,
        imageFile: File?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                success = false
            )

            val result = expensesRepository.createExpense(
                token = token,
                tripId = tripId,
                driverId = driverId,
                category = category,
                amount = amount,
                description = description,
                imageFile = imageFile
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = false,
                        error = exception.message ?: "Error al crear el gasto"
                    )
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = RegisterExpenseUiState()
    }
}