package com.tickettrack.app.domain.validator

/**
 * Validador de datos de viajes.
 * Valida todos los campos necesarios para crear o modificar un viaje.
 */
object TripValidator {

    /**
     * Valida el nombre de la carga.
     */
    fun validateCargoName(cargoName: String): ValidationResult {
        return when {
            cargoName.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "El nombre de la carga es requerido"
            )
            cargoName.length < 3 -> ValidationResult(
                isValid = false,
                errorMessage = "El nombre debe tener al menos 3 caracteres"
            )
            cargoName.length > 100 -> ValidationResult(
                isValid = false,
                errorMessage = "El nombre no puede exceder 100 caracteres"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida el tipo de carga.
     */
    fun validateCargoType(type: String): ValidationResult {
        return when {
            type.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "El tipo de carga es requerido"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida el peso de la carga.
     */
    fun validateWeight(weight: String): ValidationResult {
        return when {
            weight.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "El peso es requerido"
            )
            weight.toDoubleOrNull() == null -> ValidationResult(
                isValid = false,
                errorMessage = "El peso debe ser un número válido"
            )
            weight.toDouble() <= 0 -> ValidationResult(
                isValid = false,
                errorMessage = "El peso debe ser mayor a 0"
            )
            weight.toDouble() > 50000 -> ValidationResult(
                isValid = false,
                errorMessage = "El peso no puede exceder 50,000 kg"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida la descripción de la carga.
     */
    fun validateDescription(description: String): ValidationResult {
        return when {
            description.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "La descripción es requerida"
            )
            description.length < 10 -> ValidationResult(
                isValid = false,
                errorMessage = "La descripción debe tener al menos 10 caracteres"
            )
            description.length > 500 -> ValidationResult(
                isValid = false,
                errorMessage = "La descripción no puede exceder 500 caracteres"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida una dirección.
     */
    fun validateAddress(address: String): ValidationResult {
        return when {
            address.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "La dirección es requerida"
            )
            address.length < 10 -> ValidationResult(
                isValid = false,
                errorMessage = "La dirección debe tener al menos 10 caracteres"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida una ciudad.
     */
    fun validateCity(city: String): ValidationResult {
        return when {
            city.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "La ciudad es requerida"
            )
            city.length < 3 -> ValidationResult(
                isValid = false,
                errorMessage = "La ciudad debe tener al menos 3 caracteres"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida un estado.
     */
    fun validateState(state: String): ValidationResult {
        return when {
            state.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "El estado es requerido"
            )
            state.length < 4 -> ValidationResult(
                isValid = false,
                errorMessage = "El estado debe tener al menos 4 caracteres"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida un código postal.
     */
    fun validateZipCode(zipCode: String): ValidationResult {
        val cleanedZipCode = zipCode.replace("-", "").replace(" ", "")
        return when {
            cleanedZipCode.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "El código postal es requerido"
            )
            !cleanedZipCode.matches(Regex("^\\d{5}$")) -> ValidationResult(
                isValid = false,
                errorMessage = "El código postal debe tener 5 dígitos"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida el presupuesto inicial.
     */
    fun validateBudget(budget: String): ValidationResult {
        return when {
            budget.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "El presupuesto es requerido"
            )
            budget.toDoubleOrNull() == null -> ValidationResult(
                isValid = false,
                errorMessage = "El presupuesto debe ser un número válido"
            )
            budget.toDouble() <= 0 -> ValidationResult(
                isValid = false,
                errorMessage = "El presupuesto debe ser mayor a 0"
            )
            budget.toDouble() > 1000000 -> ValidationResult(
                isValid = false,
                errorMessage = "El presupuesto no puede exceder $1,000,000"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida que un transportista esté seleccionado.
     */
    fun validateDriverSelection(driverId: String): ValidationResult {
        return when {
            driverId.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Debe seleccionar un transportista"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida el motivo de aumento de presupuesto.
     */
    fun validateIncreaseReason(reason: String): ValidationResult {
        return when {
            reason.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "El motivo es requerido"
            )
            reason.length < 10 -> ValidationResult(
                isValid = false,
                errorMessage = "El motivo debe tener al menos 10 caracteres"
            )
            reason.length > 500 -> ValidationResult(
                isValid = false,
                errorMessage = "El motivo no puede exceder 500 caracteres"
            )
            else -> ValidationResult(isValid = true)
        }
    }

    /**
     * Valida el nuevo monto de presupuesto para un aumento.
     */
    fun validateNewBudget(newAmount: String, currentAmount: Double): ValidationResult {
        return when {
            newAmount.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "El nuevo monto es requerido"
            )
            newAmount.toDoubleOrNull() == null -> ValidationResult(
                isValid = false,
                errorMessage = "El monto debe ser un número válido"
            )
            newAmount.toDouble() <= currentAmount -> ValidationResult(
                isValid = false,
                errorMessage = "El nuevo monto debe ser mayor al actual"
            )
            newAmount.toDouble() > 2000000 -> ValidationResult(
                isValid = false,
                errorMessage = "El monto no puede exceder $2,000,000"
            )
            else -> ValidationResult(isValid = true)
        }
    }
}