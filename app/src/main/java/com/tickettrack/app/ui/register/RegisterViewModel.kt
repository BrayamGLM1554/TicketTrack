package com.tickettrack.app.ui.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.model.RegisterRequest
import com.tickettrack.app.data.repository.RegisterRepository
import com.tickettrack.app.domain.validator.CurpValidator
import com.tickettrack.app.domain.validator.EmailValidator
import com.tickettrack.app.domain.validator.PhoneValidator
import com.tickettrack.app.domain.validator.RfcValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.tickettrack.app.domain.validator.PasswordValidator
/**
 * ViewModel para el proceso de registro de usuarios.
 *
 * Maneja la lógica de negocio, validaciones y comunicación con el repositorio.
 */
class RegisterViewModel(
    private val repository: RegisterRepository = RegisterRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "RegisterViewModel"
    }

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    // ========== Funciones para Pantalla 1 (Empresa) ==========

    fun onCompanyNameChanged(value: String) {
        _state.value = _state.value.copy(
            companyName = value,
            companyNameError = null
        )
    }

    fun onCompanyRfcChanged(value: String) {
        val upperValue = value.uppercase()
        _state.value = _state.value.copy(
            companyRfc = upperValue,
            companyRfcError = null
        )
    }

    fun onCompanyPhoneChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '-' }
        _state.value = _state.value.copy(
            companyPhone = filtered,
            companyPhoneError = null
        )
    }

    fun onCompanyEmailChanged(value: String) {
        _state.value = _state.value.copy(
            companyEmail = value,
            companyEmailError = null
        )
    }

    fun validateCompanyData(): Boolean {
        val nameValid = _state.value.companyName.isNotBlank()
        val rfcValidation = RfcValidator.validate(_state.value.companyRfc)
        val phoneValidation = PhoneValidator.validate(_state.value.companyPhone)
        val emailValidation = EmailValidator.validate(_state.value.companyEmail)

        _state.value = _state.value.copy(
            companyNameError = if (!nameValid) "El nombre de la empresa es requerido" else null,
            companyRfcError = rfcValidation.errorMessage,
            companyPhoneError = phoneValidation.errorMessage,
            companyEmailError = emailValidation.errorMessage
        )

        return nameValid && rfcValidation.isValid &&
                phoneValidation.isValid && emailValidation.isValid
    }

    // ========== Funciones para Pantalla 2 (Encargado) ==========

    fun onOwnerNameChanged(value: String) {
        _state.value = _state.value.copy(
            ownerName = value,
            ownerNameError = null
        )
    }

    fun onOwnerCurpChanged(value: String) {
        val upperValue = value.uppercase()
        _state.value = _state.value.copy(
            ownerCurp = upperValue,
            ownerCurpError = null
        )
    }

    fun onOwnerPhoneChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '-' }
        _state.value = _state.value.copy(
            ownerPhone = filtered,
            ownerPhoneError = null
        )
    }

    fun onOwnerEmailChanged(value: String) {
        _state.value = _state.value.copy(
            ownerEmail = value,
            ownerEmailError = null
        )
    }


    fun register() {
        val nameValid = _state.value.ownerName.isNotBlank()
        val curpValidation = CurpValidator.validate(_state.value.ownerCurp)
        val phoneValidation = PhoneValidator.validate(_state.value.ownerPhone)
        val emailValidation = EmailValidator.validate(_state.value.ownerEmail)
        val passwordValidation = PasswordValidator.validate(_state.value.password)
        val passwordsMatch = _state.value.password == _state.value.confirmPassword

        _state.value = _state.value.copy(
            ownerNameError = if (!nameValid) "El nombre del encargado es requerido" else null,
            ownerCurpError = curpValidation.errorMessage,
            ownerPhoneError = phoneValidation.errorMessage,
            ownerEmailError = emailValidation.errorMessage,
            passwordError = passwordValidation.errorMessage,
            confirmPasswordError = if (!passwordsMatch) "Las contraseñas no coinciden" else null
        )

        if (!nameValid || !curpValidation.isValid ||
            !phoneValidation.isValid || !emailValidation.isValid ||
            !passwordValidation.isValid || !passwordsMatch) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val cleanCompanyPhone = _state.value.companyPhone.replace("-", "")
                val cleanOwnerPhone = _state.value.ownerPhone.replace("-", "")

                val request = RegisterRequest(
                    companyName = _state.value.companyName,
                    rfc = _state.value.companyRfc,
                    officePhone = cleanCompanyPhone,
                    companyEmail = _state.value.companyEmail,
                    nameOfManager = _state.value.ownerName,
                    curp = _state.value.ownerCurp,
                    workPhone = cleanOwnerPhone,
                    personalEmail = _state.value.ownerEmail,
                    password = _state.value.password
                )

                Log.d(TAG, "Starting registration...")
                val response = repository.register(request)
                Log.d(TAG, "Registration response: $response")

                if (response.success) {
                    // ✅ Registro exitoso
                    Log.i(TAG, "✅ Registration successful with uid: ${response.uid}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        registrationSuccess = true,
                        errorMessage = null
                    )
                } else {
                    // ❌ Error (incluyendo email duplicado)
                    Log.e(TAG, "❌ Registration failed: ${response.errorMessage}")

                    // Si es error de email duplicado, marcarlo en el campo específico
                    if (response.isEmailAlreadyExists) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            registrationSuccess = false,
                            ownerEmailError = response.errorMessage,
                            errorMessage = response.errorMessage
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            registrationSuccess = false,
                            errorMessage = response.errorMessage
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Registration error", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error al registrar usuario: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    /**
     * Resetea todo el estado del registro.
     * Se debe llamar cuando el usuario abandona el flujo de registro.
     */
    fun resetState() {
        _state.value = RegisterState()
    }

    // ========== Funciones para Pantalla 2 (Encargado) ==========

// ... tus funciones existentes ...

    fun onPasswordChanged(value: String) {
        _state.value = _state.value.copy(
            password = value,
            passwordError = null
        )
    }

    fun onConfirmPasswordChanged(value: String) {
        _state.value = _state.value.copy(
            confirmPassword = value,
            confirmPasswordError = null
        )
    }
}