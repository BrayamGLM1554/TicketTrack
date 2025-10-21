package com.tickettrack.app.data.repository

import com.tickettrack.app.data.model.LoginRequest
import com.tickettrack.app.data.model.LoginResponse
import kotlinx.coroutines.delay

class AuthRepository {

    suspend fun login(request: LoginRequest): LoginResponse {
        // Simulamos una llamada a red
        delay(1200)

        // Credenciales ficticias válidas
        if (request.email == "admin@tickettrack.com" && request.password == "1234") {
            return LoginResponse(
                token = "fake_jwt_token_12345",
                userId = "user_001"
            )
        } else {
            throw Exception("Credenciales inválidas")
        }
    }
}
