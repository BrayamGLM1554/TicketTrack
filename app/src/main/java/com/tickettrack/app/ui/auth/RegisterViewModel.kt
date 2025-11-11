package com.tickettrack.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.AuthResult
import com.tickettrack.app.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Datos del paso 1
    var companyName = ""
    var rfc = ""
    var officePhone = ""
    var companyEmail = ""

    fun nextStep() {
        if (validateStep1()) {
            _currentStep.value = 2
        }
    }

    fun previousStep() {
        _currentStep.value = 1
    }

    private fun validateStep1(): Boolean {
        if (companyName.isBlank()) {
            _registerState.value = RegisterState.Error("Ingresa el nombre de la empresa")
            return false
        }
        if (rfc.isBlank() || rfc.length !in 12..13) {
            _registerState.value = RegisterState.Error("RFC inválido (debe tener 12-13 caracteres)")
            return false
        }
        if (officePhone.isBlank() || officePhone.length != 10) {
            _registerState.value = RegisterState.Error("Teléfono inválido (10 dígitos)")
            return false
        }
        if (!isValidEmail(companyEmail)) {
            _registerState.value = RegisterState.Error("Correo empresarial inválido")
            return false
        }
        return true
    }

    fun register(
        nameOfManager: String,
        curp: String,
        workPhone: String,
        personalEmail: String,
        password: String,
        confirmPassword: String
    ) {
        if (nameOfManager.isBlank()) {
            _registerState.value = RegisterState.Error("Ingresa el nombre del encargado")
            return
        }
        if (curp.isBlank() || curp.length != 18) {
            _registerState.value = RegisterState.Error("CURP inválido (debe tener 18 caracteres)")
            return
        }
        if (workPhone.isBlank() || workPhone.length != 10) {
            _registerState.value = RegisterState.Error("Teléfono de trabajo inválido")
            return
        }
        if (!isValidEmail(personalEmail)) {
            _registerState.value = RegisterState.Error("Correo personal inválido")
            return
        }
        if (password.length < 8) {
            _registerState.value = RegisterState.Error("La contraseña debe tener al menos 8 caracteres")
            return
        }
        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("Las contraseñas no coinciden")
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            when (val result = authRepository.register(
                companyName = companyName,
                rfc = rfc,
                officePhone = officePhone,
                companyEmail = companyEmail,
                nameOfManager = nameOfManager,
                curp = curp,
                workPhone = workPhone,
                personalEmail = personalEmail,
                password = password
            )) {
                is AuthResult.Success -> {
                    _registerState.value = RegisterState.Success
                }
                is AuthResult.Error -> {
                    _registerState.value = RegisterState.Error(result.message)
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearError() {
        _registerState.value = RegisterState.Idle
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}