package com.tickettrack.app.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.model.AccountOwnerData
import com.tickettrack.app.data.model.CompanyData
import com.tickettrack.app.data.model.RegisterRequest
import com.tickettrack.app.data.repository.RegisterRepository
import com.tickettrack.app.domain.validator.CurpValidator
import com.tickettrack.app.domain.validator.EmailValidator
import com.tickettrack.app.domain.validator.PhoneValidator
import com.tickettrack.app.domain.validator.RfcValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el proceso de registro de usuarios.
 *
 * Maneja la lógica de negocio, validaciones y comunicación con el repositorio.
 */
class RegisterViewModel(
    private val repository: RegisterRepository = RegisterRepository()
) : ViewModel() {

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
        // Convertir a mayúsculas automáticamente
        val upperValue = value.uppercase()
        _state.value = _state.value.copy(
            companyRfc = upperValue,
            companyRfcError = null
        )
    }

    fun onCompanyPhoneChanged(value: String) {
        // Permitir solo números y guiones
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

    /**
     * Valida los datos de la empresa antes de avanzar a la siguiente pantalla.
     *
     * @return true si todos los campos son válidos, false en caso contrario
     */
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
        // Convertir a mayúsculas automáticamente
        val upperValue = value.uppercase()
        _state.value = _state.value.copy(
            ownerCurp = upperValue,
            ownerCurpError = null
        )
    }

    fun onOwnerPhoneChanged(value: String) {
        // Permitir solo números y guiones
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

    /**
     * Valida los datos del encargado y realiza el registro completo.
     */
    fun register() {
        // Primero validar los datos del encargado
        val nameValid = _state.value.ownerName.isNotBlank()
        val curpValidation = CurpValidator.validate(_state.value.ownerCurp)
        val phoneValidation = PhoneValidator.validate(_state.value.ownerPhone)
        val emailValidation = EmailValidator.validate(_state.value.ownerEmail)

        _state.value = _state.value.copy(
            ownerNameError = if (!nameValid) "El nombre del encargado es requerido" else null,
            ownerCurpError = curpValidation.errorMessage,
            ownerPhoneError = phoneValidation.errorMessage,
            ownerEmailError = emailValidation.errorMessage
        )

        // Si hay errores de validación, no continuar
        if (!nameValid || !curpValidation.isValid ||
            !phoneValidation.isValid || !emailValidation.isValid) {
            return
        }

        // Proceder con el registro
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val request = RegisterRequest(
                    company = CompanyData(
                        name = _state.value.companyName,
                        rfc = _state.value.companyRfc,
                        phone = _state.value.companyPhone,
                        email = _state.value.companyEmail
                    ),
                    accountOwner = AccountOwnerData(
                        name = _state.value.ownerName,
                        curp = _state.value.ownerCurp,
                        phone = _state.value.ownerPhone,
                        email = _state.value.ownerEmail
                    )
                )

                // TODO: Descomentar cuando el API esté listo
                // val response = repository.register(request)

                // Simulación temporal (eliminar cuando el API esté listo)
                kotlinx.coroutines.delay(1500)

                _state.value = _state.value.copy(
                    isLoading = false,
                    registrationSuccess = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al registrar usuario"
                )
            }
        }
    }

    /**
     * Limpia el mensaje de error general.
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}