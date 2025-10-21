package com.tickettrack.app.data.model

/**
 * Modelo de datos para la respuesta de recuperación de contraseña.
 *
 * Este modelo representa la estructura que se recibirá del API Gateway
 * después de solicitar la recuperación de contraseña.
 */
data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String
)