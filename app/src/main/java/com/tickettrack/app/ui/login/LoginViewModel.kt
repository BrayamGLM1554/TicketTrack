package com.tickettrack.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.model.LoginRequest
import com.tickettrack.app.data.repository.AuthRepository
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

    // Agregamos el callback onSuccess
    fun login(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                delay(1500) // Simula red

                if (_state.value.email == "admin@tickettrack.com" && _state.value.password == "1234") {
                    _state.value = _state.value.copy(isLoading = false)
                    onSuccess() // <--- esto dispara la navegación
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
