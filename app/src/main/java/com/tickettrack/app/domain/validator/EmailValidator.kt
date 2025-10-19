package com.tickettrack.app.domain.validator

import android.util.Patterns

/**
 * Validador para direcciones de correo electrónico.
 */
object EmailValidator {

    /**
     * Valida si el email tiene el formato correcto.
     *
     * @param email String a validar
     * @return ValidationResult con el resultado de la validación
     */
    fun validate(email: String): ValidationResult {
        val cleanEmail = email.trim()

        return when {
            cleanEmail.isEmpty() -> ValidationResult(
                isValid = false,
                errorMessage = "El correo electrónico es requerido"
            )
            !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> ValidationResult(
                isValid = false,
                errorMessage = "El correo electrónico no es válido"
            )
            else -> ValidationResult(isValid = true)
        }
    }
}