package com.tickettrack.app.domain.validator

/**
 * Validador para números telefónicos mexicanos.
 *
 * Formato válido: 10 dígitos
 * Acepta formatos con guiones, espacios o paréntesis que se limpian automáticamente.
 */
object PhoneValidator {

    private const val PHONE_LENGTH = 10
    private val PHONE_REGEX = Regex("^[0-9]{10}$")

    /**
     * Valida si el teléfono tiene el formato correcto.
     *
     * @param phone String a validar
     * @return ValidationResult con el resultado de la validación
     */
    fun validate(phone: String): ValidationResult {
        // Limpiar el teléfono de caracteres no numéricos
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")

        return when {
            phone.trim().isEmpty() -> ValidationResult(
                isValid = false,
                errorMessage = "El teléfono es requerido"
            )
            cleanPhone.length != PHONE_LENGTH -> ValidationResult(
                isValid = false,
                errorMessage = "El teléfono debe tener 10 dígitos"
            )
            !cleanPhone.matches(PHONE_REGEX) -> ValidationResult(
                isValid = false,
                errorMessage = "El teléfono solo debe contener números"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Formatea el teléfono a un formato legible (XXX-XXX-XXXX)
     */
    fun format(phone: String): String {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return if (cleanPhone.length == PHONE_LENGTH) {
            "${cleanPhone.substring(0, 3)}-${cleanPhone.substring(3, 6)}-${cleanPhone.substring(6)}"
        } else {
            phone
        }
    }
}