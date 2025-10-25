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

/**
 * Repositorio para manejar las operaciones de registro de usuarios.
 */
class RegisterRepository(
    private val httpClient: io.ktor.client.HttpClient = HttpClientProvider.client
) {
    companion object {
        private const val TAG = "RegisterRepository"
        private const val BASE_URL = "http://ApiGatewayTicket.somee.com"
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

            // Si el código de respuesta es 200 o 201, consideramos exitoso
            if (response.status.value in 200..201) {
                Log.d(TAG, "Registration successful with status: ${response.status}")
                RegisterResponse(
                    success = true,
                    message = "Usuario registrado exitosamente",
                    uid = null
                )
            } else {
                Log.w(TAG, "Unexpected status code: ${response.status}")
                RegisterResponse(
                    success = false,
                    message = "Respuesta inesperada del servidor"
                )
            }

        } catch (e: ClientRequestException) {
            // Error 4xx (errores del cliente)
            Log.e(TAG, "Client error: ${e.response.status}", e)
            when (e.response.status.value) {
                400 -> RegisterResponse(
                    success = false,
                    message = "Los datos ingresados no son válidos. Por favor verifica la información."
                )
                401 -> RegisterResponse(
                    success = false,
                    message = "No tienes autorización para realizar esta acción."
                )
                409 -> RegisterResponse(
                    success = false,
                    message = "El correo electrónico o RFC ya están registrados. Intenta con otros datos."
                )
                422 -> RegisterResponse(
                    success = false,
                    message = "Los datos no cumplen con el formato requerido."
                )
                429 -> RegisterResponse(
                    success = false,
                    message = "Demasiados intentos. Por favor espera unos minutos e intenta de nuevo."
                )
                else -> RegisterResponse(
                    success = false,
                    message = "Error en la solicitud. Por favor intenta nuevamente."
                )
            }
        } catch (e: ServerResponseException) {
            // Error 5xx (errores del servidor)
            Log.e(TAG, "Server error: ${e.response.status}", e)
            RegisterResponse(
                success = false,
                message = "El servidor está experimentando problemas. Por favor intenta más tarde."
            )
        } catch (e: HttpRequestTimeoutException) {
            // Timeout
            Log.e(TAG, "Timeout error", e)
            RegisterResponse(
                success = false,
                message = "La conexión tardó demasiado tiempo. Verifica tu conexión a internet e intenta nuevamente."
            )
        } catch (e: IOException) {
            // Error de red
            Log.e(TAG, "Network error", e)
            RegisterResponse(
                success = false,
                message = "No se pudo conectar al servidor. Verifica tu conexión a internet."
            )
        } catch (e: Exception) {
            // Error desconocido
            Log.e(TAG, "Unexpected error", e)
            RegisterResponse(
                success = false,
                message = "Ocurrió un error inesperado: ${e.message ?: "Por favor intenta nuevamente"}"
            )
        }
    }
}