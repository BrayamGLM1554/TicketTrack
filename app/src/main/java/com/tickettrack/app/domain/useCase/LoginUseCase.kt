package com.tickettrack.app.domain.useCase

import android.util.Patterns
import com.tickettrack.app.data.model.LoginRequest
import com.tickettrack.app.data.model.LoginResponse
import com.tickettrack.app.data.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository = AuthRepository()
) {
    suspend operator fun invoke(email: String, password: String): Result<LoginResponse> {
        // 1. Validar que no estén vacíos
        if (email.isBlank()) {
            return Result.failure(Exception("El correo electrónico no puede estar vacío"))
        }

        if (password.isBlank()) {
            return Result.failure(Exception("La contraseña no puede estar vacía"))
        }

        // 2. Validar formato de email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("El formato del correo electrónico no es válido"))
        }

        // 3. Validar longitud mínima de contraseña
        if (password.length < 4) {
            return Result.failure(Exception("La contraseña debe tener al menos 4 caracteres"))
        }

        // 4. Sanitizar entrada (quitar espacios)
        val sanitizedEmail = email.trim()
        val sanitizedPassword = password.trim()

        // 5. Hacer el login
        val request = LoginRequest(email = sanitizedEmail, password = sanitizedPassword)
        return authRepository.login(request)
    }
}