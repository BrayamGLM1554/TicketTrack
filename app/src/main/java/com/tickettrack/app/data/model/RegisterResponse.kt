package com.tickettrack.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de datos para la respuesta del API Gateway.
 */
@Serializable
data class RegisterResponse(
    @SerialName("message")
    val message: String = "",

    @SerialName("uid")
    val uid: String? = null,

    // Campos calculados
    val success: Boolean = true, // Si llegó aquí sin excepción, fue exitoso
    val userId: String? = uid,
    val companyId: String? = null
)