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

class GoogleAuthViewModel(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _googleAuthState = MutableStateFlow<GoogleAuthState>(GoogleAuthState.Idle)
    val googleAuthState: StateFlow<GoogleAuthState> = _googleAuthState.asStateFlow()

    private val _registerWithGoogleState = MutableStateFlow<RegisterWithGoogleState>(RegisterWithGoogleState.Idle)
    val registerWithGoogleState: StateFlow<RegisterWithGoogleState> = _registerWithGoogleState.asStateFlow()

    // Datos temporales del usuario de Google
    private var googleEmail: String = ""
    private var googleName: String = ""
    private var googlePhotoUrl: String? = null

    // Intentar login con Google (usuario ya registrado)
    fun tryLoginWithGoogle(email: String, name: String, photoUrl: String?) {
        googleEmail = email
        googleName = name
        googlePhotoUrl = photoUrl

        _googleAuthState.value = GoogleAuthState.Loading

        viewModelScope.launch {
            // Generar la misma contraseña que se usa en el registro
            val emailHash = email.hashCode().toString().replace("-", "")
            val googlePassword = "GoogleAuth_${emailHash}!Aa123"

            // Intentar login con email de Google y contraseña generada
            when (val result = authRepository.login(email, googlePassword)) {
                is AuthResult.Success -> {
                    val userProfile = UserProfile(
                        name = result.name,
                        email = result.email,
                        token = result.token,
                        role = result.role,
                        companyEmail = result.companyEmail,
                        companyName = result.companyName,
                        profileImageUrl = photoUrl // Usar foto de Google
                    )
                    _googleAuthState.value = GoogleAuthState.Success(userProfile)
                }
                is AuthResult.Error -> {
                    // Usuario no existe, necesita registrarse
                    _googleAuthState.value = GoogleAuthState.NeedsRegistration(
                        email = email,
                        name = name,
                        photoUrl = photoUrl
                    )
                }
            }
        }
    }

    // Registrar con Google + datos empresariales
    fun registerWithGoogle(
        companyName: String,
        rfc: String,
        officePhone: String,
        companyEmail: String,
        curp: String,
        workPhone: String
    ) {
        // Validaciones
        if (companyName.isBlank()) {
            _registerWithGoogleState.value = RegisterWithGoogleState.Error("Ingresa el nombre de la empresa")
            return
        }
        if (rfc.isBlank() || rfc.length !in 12..13) {
            _registerWithGoogleState.value = RegisterWithGoogleState.Error("RFC inválido (12-13 caracteres)")
            return
        }
        if (officePhone.isBlank() || officePhone.length != 10) {
            _registerWithGoogleState.value = RegisterWithGoogleState.Error("Teléfono inválido (10 dígitos)")
            return
        }
        if (curp.isBlank() || curp.length != 18) {
            _registerWithGoogleState.value = RegisterWithGoogleState.Error("CURP inválido (18 caracteres)")
            return
        }
        if (workPhone.isBlank() || workPhone.length != 10) {
            _registerWithGoogleState.value = RegisterWithGoogleState.Error("Teléfono de trabajo inválido")
            return
        }
        if (!isValidEmail(companyEmail)) {
            _registerWithGoogleState.value = RegisterWithGoogleState.Error("Correo empresarial inválido")
            return
        }

        _registerWithGoogleState.value = RegisterWithGoogleState.Loading

        viewModelScope.launch {
            // Generar contraseña robusta que cumpla requisitos de seguridad
            val emailHash = googleEmail.hashCode().toString().replace("-", "")
            val generatedPassword = "GoogleAuth_${emailHash}!Aa123"

            when (val result = authRepository.register(
                companyName = companyName,
                rfc = rfc,
                officePhone = officePhone,
                companyEmail = companyEmail,
                nameOfManager = googleName,
                curp = curp,
                workPhone = workPhone,
                personalEmail = googleEmail,
                password = generatedPassword
            )) {
                is AuthResult.Success -> {
                    // Ahora hacer login automático
                    loginAfterRegistration(googleEmail, generatedPassword)
                }
                is AuthResult.Error -> {
                    _registerWithGoogleState.value = RegisterWithGoogleState.Error(result.message)
                }
            }
        }
    }

    private suspend fun loginAfterRegistration(email: String, password: String) {
        when (val result = authRepository.login(email, password)) {
            is AuthResult.Success -> {
                val userProfile = UserProfile(
                    name = result.name,
                    email = result.email,
                    token = result.token,
                    role = result.role,
                    companyEmail = result.companyEmail,
                    companyName = result.companyName,
                    profileImageUrl = googlePhotoUrl // Usar foto de Google
                )
                _registerWithGoogleState.value = RegisterWithGoogleState.Success(userProfile)
            }
            is AuthResult.Error -> {
                _registerWithGoogleState.value = RegisterWithGoogleState.Error("Registro exitoso pero no se pudo iniciar sesión automáticamente")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearGoogleAuthError() {
        _googleAuthState.value = GoogleAuthState.Idle
    }

    fun clearRegisterError() {
        _registerWithGoogleState.value = RegisterWithGoogleState.Idle
    }

    // NUEVO: Limpiar todos los estados al cerrar sesión
    fun resetStates() {
        _googleAuthState.value = GoogleAuthState.Idle
        _registerWithGoogleState.value = RegisterWithGoogleState.Idle
        googleEmail = ""
        googleName = ""
        googlePhotoUrl = null
    }
}

sealed class GoogleAuthState {
    object Idle : GoogleAuthState()
    object Loading : GoogleAuthState()
    data class NeedsRegistration(
        val email: String,
        val name: String,
        val photoUrl: String?
    ) : GoogleAuthState()
    data class Success(val userProfile: UserProfile) : GoogleAuthState()
    data class Error(val message: String) : GoogleAuthState()
}

sealed class RegisterWithGoogleState {
    object Idle : RegisterWithGoogleState()
    object Loading : RegisterWithGoogleState()
    data class Success(val userProfile: UserProfile) : RegisterWithGoogleState()
    data class Error(val message: String) : RegisterWithGoogleState()
}