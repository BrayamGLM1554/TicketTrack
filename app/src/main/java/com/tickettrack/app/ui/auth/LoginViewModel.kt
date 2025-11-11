package com.tickettrack.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.AuthResult
import com.tickettrack.app.domain.model.UserProfile
import com.tickettrack.app.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Por favor completa todos los campos")
            return
        }

        if (!isValidEmail(email)) {
            _loginState.value = LoginState.Error("Correo electrónico inválido")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    val userProfile = UserProfile(
                        name = result.name,
                        email = result.email,
                        token = result.token,
                        role = result.role,
                        companyEmail = result.companyEmail,
                        companyName = result.companyName,
                        profileImageUrl = result.profileImageUrl
                    )
                    _loginState.value = LoginState.Success(userProfile)
                }
                is AuthResult.Error -> {
                    _loginState.value = LoginState.Error("Login fallido. Intenta nuevamente más tarde.")
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearError() {
        _loginState.value = LoginState.Idle
    }

    // NUEVO: Resetear estado al cerrar sesión
    fun resetStates() {
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val userProfile: UserProfile) : LoginState()
    data class Error(val message: String) : LoginState()
}