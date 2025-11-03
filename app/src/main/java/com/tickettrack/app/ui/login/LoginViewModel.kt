package com.tickettrack.app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.domain.useCase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val loginUseCase = LoginUseCase()
    private val tokenManager = TokenManager(application)

    init {
        // Verificar si ya hay una sesión activa
        if (tokenManager.isLoggedIn()) {
            _state.value = _state.value.copy(
                isLoggedIn = true,
                token = tokenManager.getToken(),
                userName = tokenManager.getUserName(),
                userEmail = tokenManager.getUserEmail(),
                role = tokenManager.getUserRole(),
                companyEmail = tokenManager.getCompanyEmail()
            )
        }
    }

    fun onEmailChanged(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChanged(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun login(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            val result = loginUseCase(
                email = _state.value.email,
                password = _state.value.password
            )

            result.fold(
                onSuccess = { response ->
                    // Guardar token y datos del usuario
                    tokenManager.saveToken(response.token)
                    tokenManager.saveUserData(
                        name = response.name,
                        email = response.email,
                        role = response.claims.role,
                        companyEmail = response.claims.companyEmail ?: response.email,  // ✅ Usar email si no hay companyEmail
                        expiresAt = response.expiresAt
                    )

                    if (response.claims.companyName != null) {
                        tokenManager.saveCompanyName(response.claims.companyName)
                    }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        token = response.token,
                        userName = response.name,
                        userEmail = response.email,
                        role = response.claims.role,
                        companyEmail = response.claims.companyEmail
                    )

                    onSuccess()
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error desconocido"
                    )
                }
            )
        }
    }

    fun logout() {
        tokenManager.clearAll()
        _state.value = LoginState()
    }
}