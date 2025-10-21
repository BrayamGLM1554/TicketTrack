package com.tickettrack.app.data.model

/**
 * Modelo de datos para la petición de recuperación de contraseña.
 *
 * Este modelo representa la estructura que se enviará al API Gateway
 * cuando el usuario solicite recuperar su contraseña.
 */
data class ForgotPasswordRequest(
    val email: String
)