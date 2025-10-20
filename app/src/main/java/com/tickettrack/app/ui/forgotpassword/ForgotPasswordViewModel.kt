package com.tickettrack.app.ui.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.model.ForgotPasswordRequest
import com.tickettrack.app.data.repository.ForgotPasswordRepository
import com.tickettrack.app.domain.validator.EmailValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de recuperación de contraseña.
 *
 * Maneja la lógica de negocio, validaciones y comunicación con el repositorio.
 */
class ForgotPasswordViewModel(
    private val repository: ForgotPasswordRepository = ForgotPasswordRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()

    /**
     * Actualiza el email en el estado.
     */
    fun onEmailChanged(value: String) {
        _state.value = _state.value.copy(
            email = value,
            emailError = null,
            errorMessage = null
        )
    }

    /**
     * Envía la solicitud de recuperación de contraseña.
     *
     * Valida el email y, si es válido, solicita el envío del código
     * de recuperación al email del usuario.
     */
    fun sendResetCode() {
        // Validar email
        val emailValidation = EmailValidator.validate(_state.value.email)

        if (!emailValidation.isValid) {
            _state.value = _state.value.copy(
                emailError = emailValidation.errorMessage
            )
            return
        }

        // Proceder con el envío
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                val request = ForgotPasswordRequest(
                    email = _state.value.email
                )

                // TODO: Descomentar cuando el API esté listo
                // val response = repository.sendPasswordResetCode(request)

                // Simulación temporal (eliminar cuando el API esté listo)
                delay(1500)

                _state.value = _state.value.copy(
                    isLoading = false,
                    requestSent = true,
                    successMessage = "Hemos enviado un código de verificación a tu correo electrónico"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al enviar el código. Intenta nuevamente."
                )
            }
        }
    }

    /**
     * Limpia los mensajes de error y éxito.
     */
    fun clearMessages() {
        _state.value = _state.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    /**
     * Reinicia el estado para permitir un nuevo intento.
     */
    fun resetState() {
        _state.value = ForgotPasswordState()
    }
}