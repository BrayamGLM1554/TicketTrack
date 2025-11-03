package com.tickettrack.app.ui.trips.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.data.remote.TripRetrofitClient
import com.tickettrack.app.data.repository.trip.TripRepository
import com.tickettrack.app.domain.model.trip.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripCreateViewModel(
    private val tokenManager: TokenManager? = null
) : ViewModel() {

    companion object {
        private const val TAG = "TripCreateViewModel"
    }

    private val repository = TripRepository(
        apiService = TripRetrofitClient.tripApiService
    )

    private val _state = MutableStateFlow(TripCreateState())
    val state: StateFlow<TripCreateState> = _state.asStateFlow()

    init {
        loadDrivers()
    }

    fun initialize(userId: String, userName: String) {
        _state.update {
            it.copy(
                currentUserId = userId,
                currentUserName = userName
            )
        }
    }

    // ==========================================
    // Cargar Transportistas
    // ==========================================

    private fun loadDrivers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingDrivers = true) }

            try {
                val result = repository.getTransportistas()

                result.onSuccess { transportistas ->
                    Log.d(TAG, "Transportistas loaded: ${transportistas.size}")

                    // Convertir a DriverResponse
                    val drivers = transportistas.map { t ->
                        DriverResponse(
                            uid = t.uid,
                            name = t.nombre,
                            email = t.email,
                            phone = t.telefono ?: "",
                            isActive = t.estado == "activo"
                        )
                    }

                    _state.update {
                        it.copy(
                            availableDrivers = drivers,
                            isLoadingDrivers = false
                        )
                    }
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error loading transportistas: ${error.message}")
                    _state.update {
                        it.copy(
                            isLoadingDrivers = false,
                            errorMessage = "Error al cargar transportistas: ${error.message}"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception loading transportistas", e)
                _state.update {
                    it.copy(
                        isLoadingDrivers = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    // ==========================================
    // PASO 1: Información de Carga
    // ==========================================

    fun onCargoNameChanged(value: String) {
        _state.update { it.copy(cargoName = value, cargoNameError = null) }
    }

    fun onCargoTypeChanged(value: String) {
        _state.update { it.copy(cargoType = value, cargoTypeError = null) }
    }

    fun onWeightChanged(value: String) {
        _state.update { it.copy(weight = value, weightError = null) }
    }

    fun onDescriptionChanged(value: String) {
        _state.update { it.copy(description = value, descriptionError = null) }
    }

    fun onSpecialRequirementsChanged(value: String) {
        _state.update { it.copy(specialRequirements = value) }
    }

    fun validateStep1() {
        var hasErrors = false

        // Validar nombre
        if (_state.value.cargoName.isBlank()) {
            _state.update { it.copy(cargoNameError = "El nombre es requerido") }
            hasErrors = true
        } else if (_state.value.cargoName.length < 3) {
            _state.update { it.copy(cargoNameError = "Mínimo 3 caracteres") }
            hasErrors = true
        }

        // Validar tipo
        if (_state.value.cargoType.isBlank()) {
            _state.update { it.copy(cargoTypeError = "El tipo es requerido") }
            hasErrors = true
        }

        // Validar peso
        if (_state.value.weight.isBlank()) {
            _state.update { it.copy(weightError = "El peso es requerido") }
            hasErrors = true
        } else {
            val weightValue = _state.value.weight.toDoubleOrNull()
            if (weightValue == null || weightValue <= 0) {
                _state.update { it.copy(weightError = "Peso inválido") }
                hasErrors = true
            }
        }

        // Validar descripción
        if (_state.value.description.isBlank()) {
            _state.update { it.copy(descriptionError = "La descripción es requerida") }
            hasErrors = true
        } else if (_state.value.description.length < 10) {
            _state.update { it.copy(descriptionError = "Mínimo 10 caracteres") }
            hasErrors = true
        }
    }

    // ==========================================
    // PASO 2: Origen y Destino
    // ==========================================

    fun onOriginAddressChanged(value: String) {
        _state.update { it.copy(originAddress = value, originAddressError = null) }
    }

    fun onOriginCityChanged(value: String) {
        _state.update { it.copy(originCity = value, originCityError = null) }
    }

    fun onOriginStateChanged(value: String) {
        _state.update { it.copy(originState = value, originStateError = null) }
    }

    fun onOriginZipCodeChanged(value: String) {
        _state.update { it.copy(originZipCode = value, originZipCodeError = null) }
    }

    fun onDestinationAddressChanged(value: String) {
        _state.update { it.copy(destinationAddress = value, destinationAddressError = null) }
    }

    fun onDestinationCityChanged(value: String) {
        _state.update { it.copy(destinationCity = value, destinationCityError = null) }
    }

    fun onDestinationStateChanged(value: String) {
        _state.update { it.copy(destinationState = value, destinationStateError = null) }
    }

    fun onDestinationZipCodeChanged(value: String) {
        _state.update { it.copy(destinationZipCode = value, destinationZipCodeError = null) }
    }

    fun validateStep2() {
        var hasErrors = false

        // Validar origen
        if (_state.value.originAddress.isBlank()) {
            _state.update { it.copy(originAddressError = "La dirección es requerida") }
            hasErrors = true
        }
        if (_state.value.originCity.isBlank()) {
            _state.update { it.copy(originCityError = "La ciudad es requerida") }
            hasErrors = true
        }
        if (_state.value.originState.isBlank()) {
            _state.update { it.copy(originStateError = "El estado es requerido") }
            hasErrors = true
        }
        if (_state.value.originZipCode.isBlank()) {
            _state.update { it.copy(originZipCodeError = "El código postal es requerido") }
            hasErrors = true
        } else if (_state.value.originZipCode.length != 5) {
            _state.update { it.copy(originZipCodeError = "Debe tener 5 dígitos") }
            hasErrors = true
        }

        // Validar destino
        if (_state.value.destinationAddress.isBlank()) {
            _state.update { it.copy(destinationAddressError = "La dirección es requerida") }
            hasErrors = true
        }
        if (_state.value.destinationCity.isBlank()) {
            _state.update { it.copy(destinationCityError = "La ciudad es requerida") }
            hasErrors = true
        }
        if (_state.value.destinationState.isBlank()) {
            _state.update { it.copy(destinationStateError = "El estado es requerido") }
            hasErrors = true
        }
        if (_state.value.destinationZipCode.isBlank()) {
            _state.update { it.copy(destinationZipCodeError = "El código postal es requerido") }
            hasErrors = true
        } else if (_state.value.destinationZipCode.length != 5) {
            _state.update { it.copy(destinationZipCodeError = "Debe tener 5 dígitos") }
            hasErrors = true
        }
    }

    // ==========================================
    // PASO 3: Presupuesto y Transportista
    // ==========================================

    fun onBudgetChanged(value: String) {
        _state.update { it.copy(budget = value, budgetError = null) }
    }

    fun onDriverSelected(driverId: String) {
        _state.update { it.copy(selectedDriverId = driverId, selectedDriverError = null) }
    }

    fun validateStep3() {
        var hasErrors = false

        // Validar presupuesto
        if (_state.value.budget.isBlank()) {
            _state.update { it.copy(budgetError = "El presupuesto es requerido") }
            hasErrors = true
        } else {
            val budgetValue = _state.value.budget.toDoubleOrNull()
            if (budgetValue == null || budgetValue <= 0) {
                _state.update { it.copy(budgetError = "Presupuesto inválido") }
                hasErrors = true
            }
        }

        // Validar transportista
        if (_state.value.selectedDriverId.isBlank()) {
            _state.update { it.copy(selectedDriverError = "Debe seleccionar un transportista") }
            hasErrors = true
        }
    }

    // ==========================================
    // Navegación
    // ==========================================

    fun nextStep() {
        if (_state.value.currentStep < _state.value.totalSteps) {
            _state.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep() {
        if (_state.value.currentStep > 1) {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    // ==========================================
    // Crear Viaje
    // ==========================================

    fun createTrip() {
        validateStep3()

        if (!_state.value.isStep3Valid()) return

        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, errorMessage = null) }

            try {
                val currentState = _state.value

                // Construir strings de ubicación para la API
                val originString = buildLocationString(
                    address = currentState.originAddress,
                    city = currentState.originCity,
                    state = currentState.originState
                )

                val destinationString = buildLocationString(
                    address = currentState.destinationAddress,
                    city = currentState.destinationCity,
                    state = currentState.destinationState
                )

                // Construir modelo de dominio
                val trip = Trip(
                    id = "",
                    cargoName = currentState.cargoName,
                    origin = Location(
                        address = currentState.originAddress,
                        city = currentState.originCity,
                        state = currentState.originState,
                        zipCode = currentState.originZipCode,
                        coordinates = Coordinates(0.0, 0.0)
                    ),
                    destination = Location(
                        address = currentState.destinationAddress,
                        city = currentState.destinationCity,
                        state = currentState.destinationState,
                        zipCode = currentState.destinationZipCode,
                        coordinates = Coordinates(0.0, 0.0)
                    ),
                    cargo = Cargo(
                        type = currentState.cargoType,
                        weight = currentState.weight.toDoubleOrNull() ?: 0.0,
                        description = currentState.description,
                        specialRequirements = currentState.specialRequirements.ifBlank { null }
                    ),
                    budget = Budget(
                        initial = currentState.budget.toDoubleOrNull() ?: 0.0,
                        current = currentState.budget.toDoubleOrNull() ?: 0.0,
                        currency = "MXN",
                        history = emptyList()
                    ),
                    assignedDriverId = currentState.selectedDriverId,
                    createdByAdminId = currentState.currentUserId,
                    status = TripStatus.PENDING
                )

                Log.d(TAG, "Creating trip: ${trip.cargoName}")

                val result = repository.createTrip(trip)

                result.onSuccess { createdTrip ->
                    Log.d(TAG, "Trip created successfully: ${createdTrip.id}")
                    _state.update {
                        it.copy(
                            isCreating = false,
                            creationSuccess = true,
                            errorMessage = null
                        )
                    }
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error creating trip: ${error.message}")
                    _state.update {
                        it.copy(
                            isCreating = false,
                            errorMessage = "Error al crear viaje: ${error.message}"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception creating trip", e)
                _state.update {
                    it.copy(
                        isCreating = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    private fun buildLocationString(address: String, city: String, state: String): String {
        return "$address, $city, $state"
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}

// DriverResponse para compatibilidad con tu State
data class DriverResponse(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val isActive: Boolean
)