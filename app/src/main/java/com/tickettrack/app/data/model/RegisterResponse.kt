package com.tickettrack.app.data.model

/**
 * Modelo de datos para la respuesta del registro de usuario.
 *
 * Este modelo representa la estructura que se recibirá del API Gateway
 * después de completar el registro.
 */
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null,
    val companyId: String? = null
)