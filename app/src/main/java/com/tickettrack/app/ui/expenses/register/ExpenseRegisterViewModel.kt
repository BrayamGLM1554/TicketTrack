package com.tickettrack.app.ui.expenses.register

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.model.expense.ExpenseRequest
import com.tickettrack.app.data.repository.expense.ExpenseRepository
import com.tickettrack.app.domain.model.expense.ExpenseCategory
import com.tickettrack.app.domain.validator.ExpenseValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * ViewModel para registro de gastos
 */
class ExpenseRegisterViewModel(
    private val repository: ExpenseRepository = ExpenseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseRegisterState())
    val state: StateFlow<ExpenseRegisterState> = _state.asStateFlow()

    /**
     * Actualiza el monto
     */
    fun onAmountChanged(value: String) {
        // Filtrar solo números y punto decimal
        val filtered = value.filter { it.isDigit() || it == '.' }
        _state.value = _state.value.copy(
            amount = filtered,
            amountError = null
        )
    }

    /**
     * Actualiza la categoría
     */
    fun onCategoryChanged(value: String) {
        _state.value = _state.value.copy(
            selectedCategory = value,
            categoryError = null,
            showCategoryPicker = false
        )
    }

    /**
     * Actualiza la descripción
     */
    fun onDescriptionChanged(value: String) {
        _state.value = _state.value.copy(
            description = value,
            descriptionError = null
        )
    }

    /**
     * Actualiza la fecha
     */
    fun onDateSelected(dateMillis: Long) {
        _state.value = _state.value.copy(
            selectedDateMillis = dateMillis,
            dateError = null,
            showDatePicker = false
        )
    }

    /**
     * Actualiza la imagen del ticket
     */
    fun onImageSelected(uri: Uri?) {
        _state.value = _state.value.copy(
            ticketImageUri = uri,
            imageError = null
        )

        // Simular moderación de imagen
        if (uri != null) {
            simulateImageModeration()
        }
    }

    /**
     * Muestra el selector de categoría
     */
    fun showCategoryPicker() {
        _state.value = _state.value.copy(showCategoryPicker = true)
    }

    /**
     * Oculta el selector de categoría
     */
    fun hideCategoryPicker() {
        _state.value = _state.value.copy(showCategoryPicker = false)
    }

    /**
     * Muestra el selector de fecha
     */
    fun showDatePicker() {
        _state.value = _state.value.copy(showDatePicker = true)
    }

    /**
     * Oculta el selector de fecha
     */
    fun hideDatePicker() {
        _state.value = _state.value.copy(showDatePicker = false)
    }

    /**
     * Simula moderación de imagen con IA
     * TODO: Reemplazar con moderación real cuando esté la API
     */
    private fun simulateImageModeration() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isModerating = true)

            // Simular proceso de moderación (2 segundos)
            kotlinx.coroutines.delay(2000)

            // TODO: Aquí irá la llamada real a la API de moderación
            // val result = moderationService.moderateImage(imageBase64)

            // Simulamos que siempre es aprobada
            _state.value = _state.value.copy(
                isModerating = false,
                moderationStatus = "approved"
            )
        }
    }

    /**
     * Registra el gasto
     */
    fun registerExpense(
        tripId: String,
        userId: String,
        userName: String
    ) {
        viewModelScope.launch {
            // Validar todos los campos
            val currentState = _state.value

            val amountResult = ExpenseValidator.validateAmount(currentState.amount)
            val categoryResult = ExpenseValidator.validateCategory(currentState.selectedCategory)
            val descriptionResult = ExpenseValidator.validateDescription(currentState.description)
            val dateResult = ExpenseValidator.validateDate(currentState.selectedDateMillis)
            val imageResult = ExpenseValidator.validateTicketImage(currentState.ticketImageUri)

            // Actualizar errores
            _state.value = currentState.copy(
                amountError = amountResult.errorMessage,
                categoryError = categoryResult.errorMessage,
                descriptionError = descriptionResult.errorMessage,
                dateError = dateResult.errorMessage,
                imageError = imageResult.errorMessage
            )

            // Si hay errores, no continuar
            if (!_state.value.hasNoErrors()) {
                return@launch
            }

            // Validar que la imagen esté moderada
            if (currentState.moderationStatus != "approved") {
                _state.value = _state.value.copy(
                    errorMessage = "La imagen del ticket está siendo moderada. Por favor espera."
                )
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                // Convertir categoría
                val category = ExpenseCategory.fromDisplayName(currentState.selectedCategory)

                // TODO: Convertir imagen a base64 para enviar a la API
                // val imageBase64 = convertUriToBase64(currentState.ticketImageUri!!)
                val imageBase64 = "mock_base64_image" // Mock

                // Crear request
                val request = ExpenseRequest.fromDomain(
                    tripId = tripId,
                    amount = currentState.amount.toDouble(),
                    category = category,
                    description = currentState.description,
                    date = Instant.ofEpochMilli(currentState.selectedDateMillis!!),
                    ticketImageBase64 = imageBase64,
                    location = null, // TODO: Agregar ubicación si es necesario
                    createdBy = userId,
                    createdByName = userName
                )

                // Llamar al repositorio
                val result = repository.createExpense(request)

                result.onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        registrationSuccess = true
                    )
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al registrar el gasto"
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

    /**
     * Resetea el estado
     */
    fun resetState() {
        _state.value = ExpenseRegisterState()
    }
}