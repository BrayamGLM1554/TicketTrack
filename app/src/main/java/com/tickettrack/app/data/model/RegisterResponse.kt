package com.tickettrack.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de datos para la respuesta del API Gateway de registro.
 */
@Serializable
data class RegisterResponse(
    @SerialName("message")
    val message: String = "",

    @SerialName("uid")
    val uid: String? = null
) {
    // Propiedad calculada: si hay uid, fue exitoso
    val success: Boolean
        get() = uid != null && !message.contains("already exists", ignoreCase = true)
                && !message.contains("EMAIL_EXISTS", ignoreCase = true)
                && !message.contains("error", ignoreCase = true)

    // Propiedad calculada: si es un error conocido
    val isEmailAlreadyExists: Boolean
        get() = message.contains("already exists", ignoreCase = true) ||
                message.contains("EMAIL_EXISTS", ignoreCase = true)

    // Usuario ID limpio
    val userId: String?
        get() = uid

    // Mensaje de error amigable
    val errorMessage: String?
        get() = when {
            isEmailAlreadyExists -> "Este correo electrónico ya está registrado. Por favor usa otro correo."
            !success && message.isNotEmpty() -> message
            !success -> "Error desconocido al registrar"
            else -> null
        }
}