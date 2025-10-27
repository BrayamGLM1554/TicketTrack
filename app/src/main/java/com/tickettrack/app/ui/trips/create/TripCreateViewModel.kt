package com.tickettrack.app.ui.trips.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.model.trip.*
import com.tickettrack.app.data.repository.trip.DriverRepository
import com.tickettrack.app.data.repository.trip.TripRepository
import com.tickettrack.app.domain.validator.TripValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * ViewModel para el formulario de creación de viaje.
 *
 * Maneja:
 * - Navegación entre pasos
 * - Validación de cada paso
 * - Carga de transportistas
 * - Creación del viaje
 * - Obtención automática de coordenadas (simulado)
 */
class TripCreateViewModel(
    private val tripRepository: TripRepository = TripRepository(),
    private val driverRepository: DriverRepository = DriverRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "TripCreateViewModel"
    }

    private val _state = MutableStateFlow(TripCreateState())
    val state: StateFlow<TripCreateState> = _state.asStateFlow()

    /**
     * Inicializa el estado con el usuario actual.
     */
    fun initialize(userId: String, userName: String) {
        _state.update { it.copy(
            currentUserId = userId,
            currentUserName = userName
        )}
        loadAvailableDrivers()
    }

    // ==================== NAVEGACIÓN DE PASOS ====================

    /**
     * Avanza al siguiente paso.
     */
    fun nextStep() {
        val currentState = _state.value

        if (currentState.canProceedToNextStep() && currentState.currentStep < currentState.totalSteps) {
            Log.d(TAG, "Moving to step ${currentState.currentStep + 1}")

            _state.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    /**
     * Retrocede al paso anterior.
     */
    fun previousStep() {
        val currentState = _state.value

        if (currentState.canGoBack()) {
            Log.d(TAG, "Moving back to step ${currentState.currentStep - 1}")

            _state.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    // ==================== PASO 1: CARGA ====================

    fun onCargoNameChanged(value: String) {
        _state.update { it.copy(
            cargoName = value,
            cargoNameError = null
        )}
    }

    fun validateCargoName() {
        val result = TripValidator.validateCargoName(_state.value.cargoName)
        _state.update { it.copy(cargoNameError = result.errorMessage) }
    }

    fun onCargoTypeChanged(value: String) {
        _state.update { it.copy(
            cargoType = value,
            cargoTypeError = null
        )}
    }

    fun validateCargoType() {
        val result = TripValidator.validateCargoType(_state.value.cargoType)
        _state.update { it.copy(cargoTypeError = result.errorMessage) }
    }

    fun onWeightChanged(value: String) {
        _state.update { it.copy(
            weight = value,
            weightError = null
        )}
    }

    fun validateWeight() {
        val result = TripValidator.validateWeight(_state.value.weight)
        _state.update { it.copy(weightError = result.errorMessage) }
    }

    fun onDescriptionChanged(value: String) {
        _state.update { it.copy(
            description = value,
            descriptionError = null
        )}
    }

    fun validateDescription() {
        val result = TripValidator.validateDescription(_state.value.description)
        _state.update { it.copy(descriptionError = result.errorMessage) }
    }

    fun onSpecialRequirementsChanged(value: String) {
        _state.update { it.copy(specialRequirements = value) }
    }

    /**
     * Valida el paso 1 completo.
     */
    fun validateStep1() {
        validateCargoName()
        validateCargoType()
        validateWeight()
        validateDescription()
    }

    // ==================== PASO 2: UBICACIONES ====================

    // Origen
    fun onOriginAddressChanged(value: String) {
        _state.update { it.copy(
            originAddress = value,
            originAddressError = null
        )}
    }

    fun validateOriginAddress() {
        val result = TripValidator.validateAddress(_state.value.originAddress)
        _state.update { it.copy(originAddressError = result.errorMessage) }
    }

    fun onOriginCityChanged(value: String) {
        _state.update { it.copy(
            originCity = value,
            originCityError = null
        )}
    }

    fun validateOriginCity() {
        val result = TripValidator.validateCity(_state.value.originCity)
        _state.update { it.copy(originCityError = result.errorMessage) }
    }

    fun onOriginStateChanged(value: String) {
        _state.update { it.copy(
            originState = value,
            originStateError = null
        )}
    }

    fun validateOriginState() {
        val result = TripValidator.validateState(_state.value.originState)
        _state.update { it.copy(originStateError = result.errorMessage) }
    }

    fun onOriginZipCodeChanged(value: String) {
        _state.update { it.copy(
            originZipCode = value,
            originZipCodeError = null
        )}
    }

    fun validateOriginZipCode() {
        val result = TripValidator.validateZipCode(_state.value.originZipCode)
        _state.update { it.copy(originZipCodeError = result.errorMessage) }
    }

    // Destino
    fun onDestinationAddressChanged(value: String) {
        _state.update { it.copy(
            destinationAddress = value,
            destinationAddressError = null
        )}
    }

    fun validateDestinationAddress() {
        val result = TripValidator.validateAddress(_state.value.destinationAddress)
        _state.update { it.copy(destinationAddressError = result.errorMessage) }
    }

    fun onDestinationCityChanged(value: String) {
        _state.update { it.copy(
            destinationCity = value,
            destinationCityError = null
        )}
    }

    fun validateDestinationCity() {
        val result = TripValidator.validateCity(_state.value.destinationCity)
        _state.update { it.copy(destinationCityError = result.errorMessage) }
    }

    fun onDestinationStateChanged(value: String) {
        _state.update { it.copy(
            destinationState = value,
            destinationStateError = null
        )}
    }

    fun validateDestinationState() {
        val result = TripValidator.validateState(_state.value.destinationState)
        _state.update { it.copy(destinationStateError = result.errorMessage) }
    }

    fun onDestinationZipCodeChanged(value: String) {
        _state.update { it.copy(
            destinationZipCode = value,
            destinationZipCodeError = null
        )}
    }

    fun validateDestinationZipCode() {
        val result = TripValidator.validateZipCode(_state.value.destinationZipCode)
        _state.update { it.copy(destinationZipCodeError = result.errorMessage) }
    }

    /**
     * Valida el paso 2 completo.
     */
    fun validateStep2() {
        validateOriginAddress()
        validateOriginCity()
        validateOriginState()
        validateOriginZipCode()
        validateDestinationAddress()
        validateDestinationCity()
        validateDestinationState()
        validateDestinationZipCode()
    }

    // ==================== PASO 3: PRESUPUESTO ====================

    fun onBudgetChanged(value: String) {
        _state.update { it.copy(
            budget = value,
            budgetError = null
        )}
    }

    fun validateBudget() {
        val result = TripValidator.validateBudget(_state.value.budget)
        _state.update { it.copy(budgetError = result.errorMessage) }
    }

    fun onDriverSelected(driverId: String) {
        _state.update { it.copy(
            selectedDriverId = driverId,
            selectedDriverError = null
        )}
    }

    fun validateDriverSelection() {
        val result = TripValidator.validateDriverSelection(_state.value.selectedDriverId)
        _state.update { it.copy(selectedDriverError = result.errorMessage) }
    }

    /**
     * Valida el paso 3 completo.
     */
    fun validateStep3() {
        validateBudget()
        validateDriverSelection()
    }

    /**
     * Carga los transportistas disponibles.
     */
    private fun loadAvailableDrivers() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading available drivers")

                _state.update { it.copy(isLoadingDrivers = true) }

                val result = driverRepository.getAvailableDrivers()

                result.fold(
                    onSuccess = { response ->
                        _state.update { it.copy(
                            availableDrivers = response.drivers,
                            isLoadingDrivers = false
                        )}

                        Log.d(TAG, "Loaded ${response.drivers.size} drivers")
                    },
                    onFailure = { exception ->
                        _state.update { it.copy(
                            isLoadingDrivers = false,
                            errorMessage = "Error al cargar transportistas: ${exception.message}"
                        )}

                        Log.e(TAG, "Error loading drivers", exception)
                    }
                )

            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoadingDrivers = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )}

                Log.e(TAG, "Unexpected error loading drivers", e)
            }
        }
    }

    // ==================== CREACIÓN DEL VIAJE ====================

    /**
     * Crea el viaje con todos los datos validados.
     */
    fun createTrip() {
        viewModelScope.launch {
            try {
                val currentState = _state.value

                // Validar todos los pasos
                validateStep1()
                validateStep2()
                validateStep3()

                if (!currentState.isStep1Valid() || !currentState.isStep2Valid() || !currentState.isStep3Valid()) {
                    Log.w(TAG, "Validation failed, cannot create trip")
                    return@launch
                }

                Log.d(TAG, "Creating trip: ${currentState.cargoName}")

                _state.update { it.copy(isCreating = true, errorMessage = null) }

                // Obtener coordenadas automáticamente (simulado)
                val originCoordinates = getCoordinatesForLocation(
                    currentState.originCity,
                    currentState.originState
                )
                val destinationCoordinates = getCoordinatesForLocation(
                    currentState.destinationCity,
                    currentState.destinationState
                )

                // Construir request
                val request = TripRequest(
                    cargoName = currentState.cargoName,
                    origin = LocationData(
                        address = currentState.originAddress,
                        city = currentState.originCity,
                        state = currentState.originState,
                        zipCode = currentState.originZipCode,
                        coordinates = originCoordinates
                    ),
                    destination = LocationData(
                        address = currentState.destinationAddress,
                        city = currentState.destinationCity,
                        state = currentState.destinationState,
                        zipCode = currentState.destinationZipCode,
                        coordinates = destinationCoordinates
                    ),
                    cargo = CargoData(
                        type = currentState.cargoType,
                        weight = currentState.weight.toDouble(),
                        description = currentState.description,
                        specialRequirements = currentState.specialRequirements.takeIf { it.isNotBlank() }
                    ),
                    budget = BudgetData(
                        initial = currentState.budget.toDouble(),
                        current = currentState.budget.toDouble(),
                        currency = "MXN",
                        history = emptyList()
                    ),
                    assignedDriverId = currentState.selectedDriverId,
                    createdByAdminId = currentState.currentUserId
                )

                val result = tripRepository.createTrip(request)

                result.fold(
                    onSuccess = { tripResponse ->
                        _state.update { it.copy(
                            isCreating = false,
                            creationSuccess = true
                        )}

                        Log.d(TAG, "Trip created successfully: ${tripResponse.id}")
                    },
                    onFailure = { exception ->
                        _state.update { it.copy(
                            isCreating = false,
                            errorMessage = "Error al crear viaje: ${exception.message}"
                        )}

                        Log.e(TAG, "Error creating trip", exception)
                    }
                )

            } catch (e: Exception) {
                _state.update { it.copy(
                    isCreating = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )}

                Log.e(TAG, "Unexpected error creating trip", e)
            }
        }
    }

    /**
     * Obtiene coordenadas para una ubicación (simulado).
     * TODO: Integrar con Google Maps Geocoding API.
     */
    private fun getCoordinatesForLocation(city: String, state: String): CoordinatesData {
        // Simulación de coordenadas basadas en ciudades conocidas
        // TODO: Reemplazar con llamada real a Geocoding API

        return when {
            city.contains("México", ignoreCase = true) || city.contains("CDMX", ignoreCase = true) -> {
                CoordinatesData(latitude = 19.4326, longitude = -99.1332)
            }
            city.contains("Querétaro", ignoreCase = true) -> {
                CoordinatesData(latitude = 20.5888, longitude = -100.3899)
            }
            city.contains("Hidalgo", ignoreCase = true) -> {
                CoordinatesData(latitude = 20.0910, longitude = -98.7624)
            }
            city.contains("Veracruz", ignoreCase = true) -> {
                CoordinatesData(latitude = 19.1738, longitude = -96.1342)
            }
            city.contains("Guadalajara", ignoreCase = true) -> {
                CoordinatesData(latitude = 20.6597, longitude = -103.3496)
            }
            city.contains("Monterrey", ignoreCase = true) -> {
                CoordinatesData(latitude = 25.6866, longitude = -100.3161)
            }
            else -> {
                // Coordenadas por defecto (centro de México)
                CoordinatesData(latitude = 23.6345, longitude = -102.5528)
            }
        }
    }

    /**
     * Limpia el mensaje de error.
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Resetea el estado completo.
     */
    fun resetState() {
        _state.value = TripCreateState()
    }
}