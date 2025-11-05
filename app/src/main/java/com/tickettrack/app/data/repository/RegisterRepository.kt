package com.tickettrack.app.data.repository

import android.util.Log
import com.tickettrack.app.data.model.RegisterRequest
import com.tickettrack.app.data.model.RegisterResponse
import com.tickettrack.app.data.network.HttpClientProvider
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.io.IOException

class RegisterRepository(
    private val httpClient: io.ktor.client.HttpClient = HttpClientProvider.client
) {
    companion object {
        private const val TAG = "RegisterRepository"
        private const val BASE_URL = "http://apigatewayticket.somee.com"
        private const val REGISTER_ENDPOINT = "$BASE_URL/api/Auth/register"
    }

    suspend fun register(request: RegisterRequest): RegisterResponse {
        return try {
            Log.d(TAG, "Sending registration request to: $REGISTER_ENDPOINT")
            Log.d(TAG, "Request data: $request")

            val response = httpClient.post(REGISTER_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            Log.d(TAG, "Response status: ${response.status}")

            // Intentar parsear el body como RegisterResponse
            val body: RegisterResponse = response.body()
            Log.d(TAG, "Response body: $body")

            // Si el código es 200 o 201 Y hay uid, fue exitoso
            if (response.status.value in 200..201) {
                if (body.success && body.uid != null) {
                    Log.d(TAG, "✅ Registration successful with uid: ${body.uid}")
                    body
                } else if (body.isEmailAlreadyExists) {
                    Log.w(TAG, "❌ Email already exists")
                    body
                } else {
                    Log.w(TAG, "⚠️ Unexpected response: ${body.message}")
                    body
                }
            } else {
                Log.w(TAG, "Unexpected status code: ${response.status}")
                RegisterResponse(
                    message = "Respuesta inesperada del servidor"
                )
            }

        } catch (e: ClientRequestException) {
            Log.e(TAG, "Client error: ${e.response.status}", e)

            try {
                val errorBody: RegisterResponse = e.response.body()
                Log.d(TAG, "Error body parsed: $errorBody")
                errorBody
            } catch (parseError: Exception) {
                Log.e(TAG, "Could not parse error body", parseError)
                when (e.response.status.value) {
                    400 -> RegisterResponse(
                        message = "Los datos ingresados no son válidos. Por favor verifica la información."
                    )
                    409 -> RegisterResponse(
                        message = "El correo electrónico o RFC ya están registrados. Intenta con otros datos."
                    )
                    422 -> RegisterResponse(
                        message = "Los datos no cumplen con el formato requerido."
                    )
                    else -> RegisterResponse(
                        message = "Error en la solicitud. Por favor intenta nuevamente."
                    )
                }
            }
        } catch (e: ServerResponseException) {
            Log.e(TAG, "Server error: ${e.response.status}", e)
            RegisterResponse(
                message = "El servidor está experimentando problemas. Por favor intenta más tarde."
            )
        } catch (e: HttpRequestTimeoutException) {
            Log.e(TAG, "Timeout error", e)
            RegisterResponse(
                message = "La conexión tardó demasiado tiempo. Verifica tu conexión a internet e intenta nuevamente."
            )
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            RegisterResponse(
                message = "No se pudo conectar al servidor. Verifica tu conexión a internet."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            RegisterResponse(
                message = "Ocurrió un error inesperado: ${e.message ?: "Por favor intenta nuevamente"}"
            )
        }
    }
}