package com.tickettrack.app.domain.validator

/**
 * Clase de resultado de validación.
 *
 * Esta clase representa el resultado de cualquier validación,
 * indicando si es válida y el mensaje de error en caso de no serlo.
 */
object ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)