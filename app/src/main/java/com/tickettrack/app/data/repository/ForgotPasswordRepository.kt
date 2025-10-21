package com.tickettrack.app.data.repository

import com.tickettrack.app.data.model.ForgotPasswordRequest
import com.tickettrack.app.data.model.ForgotPasswordResponse

/**
 * Repositorio para manejar las operaciones de recuperación de contraseña.
 *
 * Este repositorio encapsula la lógica de acceso a datos para la recuperación
 * de contraseña, siguiendo el principio de separación de responsabilidades.
 */
class ForgotPasswordRepository {

    /**
     * Solicita el envío de código de recuperación de contraseña al email del usuario.
     *
     * @param request Datos de la solicitud (email del usuario)
     * @return ForgotPasswordResponse con el resultado de la operación
     *
     * TODO: Implementar la llamada real al API Gateway cuando esté disponible
     * Ejemplo de implementación futura:
     *
     * val response = httpClient.post("https://api-gateway-tickettrack/forgot-password") {
     *     contentType(ContentType.Application.Json)
     *     setBody(request)
     * }
     * return response.body()
     */
    suspend fun sendPasswordResetCode(request: ForgotPasswordRequest): ForgotPasswordResponse {
        // Space reserved for future API integration
        throw NotImplementedError("Forgot Password API call not yet implemented")
    }
}