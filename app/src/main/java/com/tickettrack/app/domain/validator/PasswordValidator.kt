package com.tickettrack.app.domain.validator

/**
 * Validador de contraseñas.
 *
 * Valida que una contraseña cumpla con los requisitos de seguridad:
 * - Mínimo 8 caracteres
 * - Al menos una letra mayúscula
 * - Al menos una letra minúscula
 * - Al menos un número
 * - Al menos un carácter especial (@#$%^&+=!*()_-?)
 *
 * Uso:
 * ```
 * val result = PasswordValidator.validate("MiPassword123!")
 * if (result.isValid) {
 *     // Contraseña válida
 * } else {
 *     // Mostrar result.errorMessage
 * }
 * ```
 */
object PasswordValidator {

    private const val MIN_LENGTH = 8
    private const val SPECIAL_CHARS = "@#$%^&+=!*()_-?"

    /**
     * Valida una contraseña según los requisitos de seguridad.
     *
     * @param password La contraseña a validar
     * @return ValidationResult indicando si es válida y mensaje de error si aplica
     */
    fun validate(password: String): ValidationResult {
        // Validar que no esté vacía
        if (password.isBlank()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "La contraseña es requerida"
            )
        }

        // Validar longitud mínima
        if (password.length < MIN_LENGTH) {
            return ValidationResult(
                isValid = false,
                errorMessage = "La contraseña debe tener al menos $MIN_LENGTH caracteres"
            )
        }

        // Validar que contenga al menos una letra mayúscula
        if (!password.any { it.isUpperCase() }) {
            return ValidationResult(
                isValid = false,
                errorMessage = "La contraseña debe contener al menos una letra mayúscula"
            )
        }

        // Validar que contenga al menos una letra minúscula
        if (!password.any { it.isLowerCase() }) {
            return ValidationResult(
                isValid = false,
                errorMessage = "La contraseña debe contener al menos una letra minúscula"
            )
        }

        // Validar que contenga al menos un número
        if (!password.any { it.isDigit() }) {
            return ValidationResult(
                isValid = false,
                errorMessage = "La contraseña debe contener al menos un número"
            )
        }

        // Validar que contenga al menos un carácter especial
        if (!password.any { it in SPECIAL_CHARS }) {
            return ValidationResult(
                isValid = false,
                errorMessage = "La contraseña debe contener al menos un carácter especial ($SPECIAL_CHARS)"
            )
        }

        // Si pasa todas las validaciones
        return ValidationResult(isValid = true)
    }

    /**
     * Verifica la fortaleza de la contraseña.
     *
     * @param password La contraseña a evaluar
     * @return String indicando el nivel: "Débil", "Media", "Fuerte"
     */
    fun getPasswordStrength(password: String): String {
        if (password.length < MIN_LENGTH) return "Débil"

        var strength = 0

        // +1 por longitud adecuada
        if (password.length >= MIN_LENGTH) strength++

        // +1 por longitud mayor a 12
        if (password.length >= 12) strength++

        // +1 por tener mayúsculas
        if (password.any { it.isUpperCase() }) strength++

        // +1 por tener minúsculas
        if (password.any { it.isLowerCase() }) strength++

        // +1 por tener números
        if (password.any { it.isDigit() }) strength++

        // +1 por tener caracteres especiales
        if (password.any { it in SPECIAL_CHARS }) strength++

        return when (strength) {
            in 0..2 -> "Débil"
            in 3..4 -> "Media"
            else -> "Fuerte"
        }
    }

    /**
     * Obtiene una lista de requisitos faltantes para la contraseña.
     *
     * @param password La contraseña a evaluar
     * @return Lista de strings con los requisitos que faltan
     */
    fun getMissingRequirements(password: String): List<String> {
        val missing = mutableListOf<String>()

        if (password.length < MIN_LENGTH) {
            missing.add("Mínimo $MIN_LENGTH caracteres")
        }
        if (!password.any { it.isUpperCase() }) {
            missing.add("Una letra mayúscula")
        }
        if (!password.any { it.isLowerCase() }) {
            missing.add("Una letra minúscula")
        }
        if (!password.any { it.isDigit() }) {
            missing.add("Un número")
        }
        if (!password.any { it in SPECIAL_CHARS }) {
            missing.add("Un carácter especial ($SPECIAL_CHARS)")
        }

        return missing
    }
}