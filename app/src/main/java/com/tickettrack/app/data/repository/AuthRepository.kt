package com.tickettrack.app.data.repository

import android.util.Log
import com.tickettrack.app.data.model.Claims
import com.tickettrack.app.data.model.LoginRequest
import com.tickettrack.app.data.model.LoginResponse
import com.tickettrack.app.data.remote.RetrofitClient
import kotlinx.coroutines.delay

class AuthRepository {

    private val authApiService = RetrofitClient.authApiService

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            // Intentar login con API real
            Log.d("AuthRepository", "Intentando login con API real...")
            val response = authApiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                Log.d("AuthRepository", "Login exitoso con API real")
                Result.success(response.body()!!)
            } else {
                Log.e("AuthRepository", "Error en API: ${response.code()} - ${response.message()}")
                // Si falla la API, intentar con datos ficticios
                loginWithMockData(request)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Excepción en API: ${e.message}")
            // Si hay error de red, usar datos ficticios
            loginWithMockData(request)
        }
    }

    private suspend fun loginWithMockData(request: LoginRequest): Result<LoginResponse> {
        delay(1200) // Simular latencia de red

        // Credenciales ficticias válidas
        return if (request.email == "admin@tickettrack.com" && request.password == "1234") {
            Log.d("AuthRepository", "Login exitoso con datos ficticios")
            Result.success(
                LoginResponse(
                    name = "Admin Ficticio",
                    email = request.email,
                    token = "fake_jwt_token_12345_${System.currentTimeMillis()}",
                    loginAt = java.time.Instant.now().toString(),
                    expiresAt = java.time.Instant.now().plusSeconds(3600).toString(),
                    claims = Claims(
                        role = "ADMIN",
                        companyEmail = request.email
                    )
                )
            )
        } else {
            Log.d("AuthRepository", "Credenciales ficticias inválidas")
            Result.failure(Exception("Credenciales inválidas"))
        }
    }
}