package com.tickettrack.app.domain.validator

import android.net.Uri
import com.tickettrack.app.domain.model.expense.ExpenseCategory

/**
 * Validador para gastos
 * Contiene todas las reglas de validación para el registro de gastos
 */
object ExpenseValidator {

    /**
     * Valida el monto del gasto
     */
    fun validateAmount(amount: String): ValidationResult {
        if (amount.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "El monto es requerido")
        }

        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null) {
            return ValidationResult(isValid = false, errorMessage = "El monto debe ser un número válido")
        }

        if (amountValue <= 0) {
            return ValidationResult(isValid = false, errorMessage = "El monto debe ser mayor a 0")
        }

        if (amountValue > 1000000) {
            return ValidationResult(isValid = false, errorMessage = "El monto no puede exceder $1,000,000")
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Valida la categoría del gasto
     */
    fun validateCategory(category: String): ValidationResult {
        if (category.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "La categoría es requerida")
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Valida la descripción del gasto
     */
    fun validateDescription(description: String): ValidationResult {
        if (description.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "La descripción es requerida")
        }

        if (description.length < 5) {
            return ValidationResult(isValid = false, errorMessage = "La descripción debe tener al menos 5 caracteres")
        }

        if (description.length > 500) {
            return ValidationResult(isValid = false, errorMessage = "La descripción no puede exceder 500 caracteres")
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Valida la fecha del gasto
     */
    fun validateDate(dateMillis: Long?): ValidationResult {
        if (dateMillis == null) {
            return ValidationResult(isValid = false, errorMessage = "La fecha es requerida")
        }

        val currentTime = System.currentTimeMillis()
        if (dateMillis > currentTime) {
            return ValidationResult(isValid = false, errorMessage = "La fecha no puede ser futura")
        }

        // No puede ser mayor a 1 año atrás
        val oneYearAgo = currentTime - (365L * 24 * 60 * 60 * 1000)
        if (dateMillis < oneYearAgo) {
            return ValidationResult(isValid = false, errorMessage = "La fecha no puede ser mayor a 1 año atrás")
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Valida la imagen del ticket
     */
    fun validateTicketImage(imageUri: Uri?): ValidationResult {
        if (imageUri == null) {
            return ValidationResult(isValid = false, errorMessage = "La imagen del ticket es requerida")
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Valida todos los campos del gasto
     */
    fun validateExpenseData(
        amount: String,
        category: String,
        description: String,
        dateMillis: Long?,
        imageUri: Uri?
    ): ValidationResult {
        val amountResult = validateAmount(amount)
        if (!amountResult.isValid) return amountResult

        val categoryResult = validateCategory(category)
        if (!categoryResult.isValid) return categoryResult

        val descriptionResult = validateDescription(description)
        if (!descriptionResult.isValid) return descriptionResult

        val dateResult = validateDate(dateMillis)
        if (!dateResult.isValid) return dateResult

        val imageResult = validateTicketImage(imageUri)
        if (!imageResult.isValid) return imageResult

        return ValidationResult(isValid = true)
    }

    /**
     * Valida el motivo de solicitud de presupuesto
     */
    fun validateBudgetReason(reason: String): ValidationResult {
        if (reason.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "El motivo es requerido")
        }

        if (reason.length < 10) {
            return ValidationResult(isValid = false, errorMessage = "El motivo debe tener al menos 10 caracteres")
        }

        if (reason.length > 1000) {
            return ValidationResult(isValid = false, errorMessage = "El motivo no puede exceder 1000 caracteres")
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Valida el monto solicitado de aumento
     */
    fun validateRequestedAmount(currentBudget: Double, requestedAmount: String): ValidationResult {
        if (requestedAmount.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "El monto solicitado es requerido")
        }

        val amount = requestedAmount.toDoubleOrNull()
        if (amount == null) {
            return ValidationResult(isValid = false, errorMessage = "El monto debe ser un número válido")
        }

        if (amount <= currentBudget) {
            return ValidationResult(isValid = false, errorMessage = "El monto solicitado debe ser mayor al presupuesto actual")
        }

        val increasePercentage = ((amount - currentBudget) / currentBudget) * 100
        if (increasePercentage > 100) {
            return ValidationResult(isValid = false, errorMessage = "El aumento no puede ser mayor al 100% del presupuesto actual")
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Valida la nota de revisión (cuando se aprueba/rechaza)
     */
    fun validateReviewNote(note: String, isRejecting: Boolean): ValidationResult {
        if (isRejecting && note.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Debes proporcionar un motivo para rechazar")
        }

        if (note.length > 500) {
            return ValidationResult(isValid = false, errorMessage = "La nota no puede exceder 500 caracteres")
        }

        return ValidationResult(isValid = true)
    }
}