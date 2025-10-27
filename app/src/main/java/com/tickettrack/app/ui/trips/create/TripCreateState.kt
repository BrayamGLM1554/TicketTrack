package com.tickettrack.app.ui.trips.create

import com.tickettrack.app.data.model.trip.DriverResponse

/**
 * Estado del formulario de creación de viaje.
 *
 * Siguiendo principios de Compose:
 * - Estado inmutable
 * - Multi-step: 3 pasos (Carga, Ubicaciones, Presupuesto)
 */
data class TripCreateState(
    // Control de pasos
    val currentStep: Int = 1,
    val totalSteps: Int = 3,

    // PASO 1: Información de Carga
    val cargoName: String = "",
    val cargoNameError: String? = null,

    val cargoType: String = "",
    val cargoTypeError: String? = null,

    val weight: String = "",
    val weightError: String? = null,

    val description: String = "",
    val descriptionError: String? = null,

    val specialRequirements: String = "",

    // PASO 2: Origen y Destino
    // Origen
    val originAddress: String = "",
    val originAddressError: String? = null,

    val originCity: String = "",
    val originCityError: String? = null,

    val originState: String = "",
    val originStateError: String? = null,

    val originZipCode: String = "",
    val originZipCodeError: String? = null,

    // Destino
    val destinationAddress: String = "",
    val destinationAddressError: String? = null,

    val destinationCity: String = "",
    val destinationCityError: String? = null,

    val destinationState: String = "",
    val destinationStateError: String? = null,

    val destinationZipCode: String = "",
    val destinationZipCodeError: String? = null,

    // PASO 3: Presupuesto y Transportista
    val budget: String = "",
    val budgetError: String? = null,

    val selectedDriverId: String = "",
    val selectedDriverError: String? = null,

    // Lista de transportistas disponibles
    val availableDrivers: List<DriverResponse> = emptyList(),
    val isLoadingDrivers: Boolean = false,

    // Estados generales
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val creationSuccess: Boolean = false,
    val errorMessage: String? = null,

    // Usuario actual
    val currentUserId: String = "",
    val currentUserName: String = ""
) {
    /**
     * Verifica si el paso actual es válido para avanzar.
     */
    fun canProceedToNextStep(): Boolean {
        return when (currentStep) {
            1 -> isStep1Valid()
            2 -> isStep2Valid()
            3 -> isStep3Valid()
            else -> false
        }
    }

    /**
     * Verifica si el paso 1 (Carga) es válido.
     */
    fun isStep1Valid(): Boolean {
        return cargoName.isNotBlank() &&
                cargoType.isNotBlank() &&
                weight.isNotBlank() &&
                description.isNotBlank() &&
                cargoNameError == null &&
                cargoTypeError == null &&
                weightError == null &&
                descriptionError == null
    }

    /**
     * Verifica si el paso 2 (Ubicaciones) es válido.
     */
    fun isStep2Valid(): Boolean {
        return originAddress.isNotBlank() &&
                originCity.isNotBlank() &&
                originState.isNotBlank() &&
                originZipCode.isNotBlank() &&
                destinationAddress.isNotBlank() &&
                destinationCity.isNotBlank() &&
                destinationState.isNotBlank() &&
                destinationZipCode.isNotBlank() &&
                originAddressError == null &&
                originCityError == null &&
                originStateError == null &&
                originZipCodeError == null &&
                destinationAddressError == null &&
                destinationCityError == null &&
                destinationStateError == null &&
                destinationZipCodeError == null
    }

    /**
     * Verifica si el paso 3 (Presupuesto) es válido.
     */
    fun isStep3Valid(): Boolean {
        return budget.isNotBlank() &&
                selectedDriverId.isNotBlank() &&
                budgetError == null &&
                selectedDriverError == null
    }

    /**
     * Verifica si puede retroceder.
     */
    fun canGoBack(): Boolean {
        return currentStep > 1
    }

    /**
     * Obtiene el progreso actual (0.0 a 1.0).
     */
    fun getProgress(): Float {
        return currentStep.toFloat() / totalSteps.toFloat()
    }
}