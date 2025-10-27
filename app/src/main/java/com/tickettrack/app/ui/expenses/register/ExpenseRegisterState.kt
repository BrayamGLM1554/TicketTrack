package com.tickettrack.app.ui.expenses.register

import android.net.Uri

/**
 * Estado para la pantalla de registro de gastos
 */
data class ExpenseRegisterState(
    // Datos del gasto
    val amount: String = "",
    val selectedCategory: String = "",
    val description: String = "",
    val selectedDateMillis: Long? = null,
    val ticketImageUri: Uri? = null,

    // Errores de validación
    val amountError: String? = null,
    val categoryError: String? = null,
    val descriptionError: String? = null,
    val dateError: String? = null,
    val imageError: String? = null,

    // Estados de UI
    val isLoading: Boolean = false,
    val isModerating: Boolean = false,
    val moderationStatus: String? = null, // "approved", "rejected", "flagged"
    val showDatePicker: Boolean = false,
    val showCategoryPicker: Boolean = false,

    // Resultado
    val errorMessage: String? = null,
    val registrationSuccess: Boolean = false
) {
    /**
     * Verifica si todos los campos son válidos
     */
    fun hasNoErrors(): Boolean {
        return amountError == null &&
                categoryError == null &&
                descriptionError == null &&
                dateError == null &&
                imageError == null
    }

    /**
     * Verifica si hay datos completos
     */
    fun hasCompleteData(): Boolean {
        return amount.isNotBlank() &&
                selectedCategory.isNotBlank() &&
                description.isNotBlank() &&
                selectedDateMillis != null &&
                ticketImageUri != null
    }
}