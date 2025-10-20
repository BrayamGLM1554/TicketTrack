package com.tickettrack.app.ui.forgotpassword

/**
 * Estado de la pantalla de recuperación de contraseña.
 *
 * Maneja todos los datos y estados necesarios para la funcionalidad
 * de "olvidaste tu contraseña".
 */
data class ForgotPasswordState(
    // Datos del formulario
    val email: String = "",

    // Estados de validación
    val emailError: String? = null,

    // Estados de la operación
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val requestSent: Boolean = false
)