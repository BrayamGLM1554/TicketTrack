package com.tickettrack.app.data.repository

import android.util.Log
import com.tickettrack.app.data.model.Claims
import com.tickettrack.app.data.model.LoginRequest
import com.tickettrack.app.data.model.LoginResponse
import com.tickettrack.app.data.remote.RetrofitClient
import kotlinx.coroutines.delay
import retrofit2.Response

class AuthRepository {

    private val authApiService = RetrofitClient.authApiService

    // ✅ NUEVO: Flag para habilitar/deshabilitar fallback a mock
    // Cambiar a false cuando la API esté 100% lista
    private val enableMockFallback = true  // ← Cambiar a false en producción

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            Log.d(TAG, "🔄 Intentando login con API real: ${request.email}")

            // ✅ Llamada a la API real
            val response = authApiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val loginData = response.body()!!
                Log.d(TAG, "✅ Login exitoso con API real")
                Log.d(TAG, "👤 Usuario: ${loginData.name}")
                Log.d(TAG, "🎭 Rol: ${loginData.claims.role}")
                Log.d(TAG, "🏢 Company: ${loginData.claims.companyEmail ?: loginData.claims.companyName ?: "N/A"}")

                Result.success(loginData)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error en API: ${response.code()} - ${response.message()}")
                Log.e(TAG, "📄 Error body: $errorBody")

                // ✅ Intentar con mock solo si está habilitado
                if (enableMockFallback) {
                    Log.w(TAG, "⚠️ Fallback activado: usando datos ficticios")
                    loginWithMockData(request)
                } else {
                    Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Excepción en API: ${e.message}", e)

            // ✅ Intentar con mock solo si está habilitado
            if (enableMockFallback) {
                Log.w(TAG, "⚠️ Fallback activado por excepción: usando datos ficticios")
                loginWithMockData(request)
            } else {
                Result.failure(Exception("Error de conexión: ${e.message}"))
            }
        }
    }

    // ✅ MEJORADO: Datos mock para ADMIN y USER
    private suspend fun loginWithMockData(request: LoginRequest): Result<LoginResponse> {
        delay(1200) // Simular latencia de red

        // ✅ Credenciales ficticias para ADMIN
        if (request.email == "admin@tickettrack.com" && request.password == "1234") {
            Log.d(TAG, "🎭 Login mock exitoso como ADMIN")
            return Result.success(
                LoginResponse(
                    name = "Admin Ficticio",
                    email = request.email,
                    token = "fake_jwt_token_admin_${System.currentTimeMillis()}",
                    loginAt = java.time.Instant.now().toString(),
                    expiresAt = java.time.Instant.now().plusSeconds(3600).toString(),
                    claims = Claims(
                        role = "ADMIN",
                        companyEmail = request.email,
                        companyName = null  // ✅ ADMIN no tiene companyName
                    ),
                    profileFilePath = null,
                    profileImageUrl = ""
                )
            )
        }

        // ✅ NUEVO: Credenciales ficticias para USER (transportista)
        if (request.email == "chofer@example.com" && request.password == "1234") {
            Log.d(TAG, "🚚 Login mock exitoso como USER (Transportista)")
            return Result.success(
                LoginResponse(
                    name = "Juan Pérez Ficticio",
                    email = request.email,
                    token = "fake_jwt_token_user_${System.currentTimeMillis()}",
                    loginAt = java.time.Instant.now().toString(),
                    expiresAt = java.time.Instant.now().plusSeconds(3600).toString(),
                    claims = Claims(
                        role = "USER",
                        companyEmail = null,           // ✅ USER no tiene companyEmail
                        companyName = "Empresa Mock"   // ✅ USER tiene companyName
                    ),
                    profileFilePath = null,
                    profileImageUrl = ""
                )
            )
        }

        // ✅ Si no coincide ninguna credencial
        Log.d(TAG, "❌ Credenciales mock inválidas")
        return Result.failure(Exception("Credenciales inválidas"))
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}