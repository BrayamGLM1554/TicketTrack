package com.tickettrack.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChanged(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChanged(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun login() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            // ⚠️ Aquí se hará la llamada real al API Gateway cuando esté listo
            try {
                delay(1500) // Simula red
                // repository.login(email, password)

                // Simulación de éxito o error
                if (_state.value.email == "admin@tickettrack.com" && _state.value.password == "1234") {
                    // éxito
                    _state.value = _state.value.copy(isLoading = false)
                } else {
                    throw Exception("Credenciales inválidas")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error desconocido"
                )
            }
        }
    }
}
