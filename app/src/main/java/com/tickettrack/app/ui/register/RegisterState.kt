package com.tickettrack.app.ui.register

/**
 * Estado de la pantalla de registro.
 *
 * Maneja todos los datos de ambas pantallas (empresa y encargado)
 * en un solo estado para facilitar el envío final al backend.
 */
data class RegisterState(
    // Datos de la empresa (Pantalla 1)
    val companyName: String = "",
    val companyRfc: String = "",
    val companyPhone: String = "",
    val companyEmail: String = "",

    // Datos del encargado (Pantalla 2)
    val ownerName: String = "",
    val ownerCurp: String = "",
    val ownerPhone: String = "",
    val ownerEmail: String = "",

    // NUEVOS CAMPOS: Contraseñas
    val password: String = "",
    val confirmPassword: String = "",

    // Estados de validación (Pantalla 1)
    val companyNameError: String? = null,
    val companyRfcError: String? = null,
    val companyPhoneError: String? = null,
    val companyEmailError: String? = null,

    // Estados de validación (Pantalla 2)
    val ownerNameError: String? = null,
    val ownerCurpError: String? = null,
    val ownerPhoneError: String? = null,
    val ownerEmailError: String? = null,

    // NUEVOS: Estados de validación de contraseñas
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    // Estados generales
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrationSuccess: Boolean = false
)