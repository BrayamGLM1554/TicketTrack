package com.tickettrack.app.domain.validator

/**
 * Validador para RFC (Registro Federal de Contribuyentes) mexicano.
 *
 * Formatos válidos:
 * - Persona Moral: 12 caracteres (Ej: EMP123456ABC)
 * - Persona Física: 13 caracteres (Ej: PEMJ850101ABC)
 */
object RfcValidator {

    private const val RFC_MORAL_LENGTH = 12
    private const val RFC_FISICA_LENGTH = 13
    private val RFC_REGEX = Regex("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$")

    /**
     * Valida si el RFC tiene el formato correcto.
     *
     * @param rfc String a validar
     * @return ValidationResult con el resultado de la validación
     */
    fun validate(rfc: String): ValidationResult {
        val cleanRfc = rfc.trim().uppercase()

        return when {
            cleanRfc.isEmpty() -> ValidationResult(
                isValid = false,
                errorMessage = "El RFC es requerido"
            )
            cleanRfc.length !in listOf(RFC_MORAL_LENGTH, RFC_FISICA_LENGTH) -> ValidationResult(
                isValid = false,
                errorMessage = "El RFC debe tener 12 o 13 caracteres"
            )
            !cleanRfc.matches(RFC_REGEX) -> ValidationResult(
                isValid = false,
                errorMessage = "El RFC tiene un formato inválido"
            )
            else -> ValidationResult(isValid = true)
        }
    }
}

/**
 * Clase de resultado de validación.
 */
//data class ValidationResult(
//    val isValid: Boolean,
//    val errorMessage: String? = null
//)