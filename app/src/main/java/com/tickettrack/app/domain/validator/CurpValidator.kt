package com.tickettrack.app.domain.validator

/**
 * Validador para CURP (Clave Única de Registro de Población) mexicana.
 *
 * Formato válido: 18 caracteres alfanuméricos
 * Ejemplo: PEMJ850101HDFRNN01
 */
object CurpValidator {

    private const val CURP_LENGTH = 18
    private val CURP_REGEX = Regex("^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[0-9A-Z][0-9]$")

    /**
     * Valida si la CURP tiene el formato correcto.
     *
     * @param curp String a validar
     * @return ValidationResult con el resultado de la validación
     */
    fun validate(curp: String): ValidationResult {
        val cleanCurp = curp.trim().uppercase()

        return when {
            cleanCurp.isEmpty() -> ValidationResult(
                isValid = false,
                errorMessage = "La CURP es requerida"
            )
            cleanCurp.length != CURP_LENGTH -> ValidationResult(
                isValid = false,
                errorMessage = "La CURP debe tener exactamente 18 caracteres"
            )
            !cleanCurp.matches(CURP_REGEX) -> ValidationResult(
                isValid = false,
                errorMessage = "La CURP tiene un formato inválido"
            )
            else -> ValidationResult(isValid = true)
        }
    }
}