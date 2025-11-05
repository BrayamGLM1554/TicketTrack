package com.tickettrack.app.ui.main.drivers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.data.model.driver.Driver
import com.tickettrack.app.data.remote.RetrofitClient
import com.tickettrack.app.data.repository.driver.DriverRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AddDriverState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val licenseNumber: String = "",
    val licenseCaducation: String = "",
    val unitNumber: String = "",
    val password: String = "",
    val isActive: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val licenseNumberError: String? = null,
    val licenseCaducationError: String? = null,
    val unitNumberError: String? = null,
    val passwordError: String? = null
)

class DriverViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    companion object {
        private const val TAG = "DriverViewModel"
    }

    // Repository con API real
    private val repository = DriverRepository(
        apiService = RetrofitClient.transportApiService,
        authRegisterService = RetrofitClient.authRegisterService,
        tokenManager = tokenManager
    )

    private val _transportistas = mutableStateListOf<Driver>()
    val transportistas: List<Driver> get() = _transportistas

    private val _addDriverState = MutableStateFlow(AddDriverState())
    val addDriverState: StateFlow<AddDriverState> = _addDriverState

    private val _isLoadingList = MutableStateFlow(false)
    val isLoadingList: StateFlow<Boolean> = _isLoadingList

    private val _listErrorMessage = MutableStateFlow<String?>(null)
    val listErrorMessage: StateFlow<String?> = _listErrorMessage

    init {
        loadDrivers()
    }

    // ==========================================
    // Cargar lista de transportistas
    // ==========================================

    /**
     * Carga todos los transportistas desde la API.
     */
    fun loadDrivers() {
        viewModelScope.launch {
            _isLoadingList.value = true
            _listErrorMessage.value = null

            try {
                Log.d(TAG, "Loading drivers from API...")

                val result = repository.getAllDrivers()

                result.onSuccess { drivers ->
                    _transportistas.clear()
                    _transportistas.addAll(drivers)
                    _isLoadingList.value = false

                    Log.d(TAG, "Drivers loaded successfully: ${drivers.size}")
                }

                result.onFailure { error ->
                    _isLoadingList.value = false
                    _listErrorMessage.value = error.message ?: "Error al cargar transportistas"

                    Log.e(TAG, "Error loading drivers: ${error.message}")
                }

            } catch (e: Exception) {
                _isLoadingList.value = false
                _listErrorMessage.value = "Error inesperado: ${e.message}"

                Log.e(TAG, "Exception loading drivers", e)
            }
        }
    }

    /**
     * Refresca la lista de transportistas.
     */
    fun refreshDrivers() {
        loadDrivers()
    }

    /**
     * Limpia el mensaje de error de la lista.
     */
    fun clearListError() {
        _listErrorMessage.value = null
    }

    // ==========================================
    // Funciones para actualizar el estado del formulario
    // ==========================================

    fun onFullNameChanged(value: String) {
        _addDriverState.value = _addDriverState.value.copy(
            fullName = value,
            fullNameError = null
        )
    }

    fun onEmailChanged(value: String) {
        _addDriverState.value = _addDriverState.value.copy(
            email = value,
            emailError = null
        )
    }

    fun onPhoneChanged(value: String) {
        _addDriverState.value = _addDriverState.value.copy(
            phone = value,
            phoneError = null
        )
    }

    fun onLicenseNumberChanged(value: String) {
        _addDriverState.value = _addDriverState.value.copy(
            licenseNumber = value,
            licenseNumberError = null
        )
    }

    fun onLicenseCaducationChanged(value: String) {
        _addDriverState.value = _addDriverState.value.copy(
            licenseCaducation = value,
            licenseCaducationError = null
        )
    }

    fun onUnitNumberChanged(value: String) {
        _addDriverState.value = _addDriverState.value.copy(
            unitNumber = value,
            unitNumberError = null
        )
    }

    fun onPasswordChanged(value: String) {
        _addDriverState.value = _addDriverState.value.copy(
            password = value,
            passwordError = null
        )
    }

    fun onIsActiveChanged(value: Boolean) {
        _addDriverState.value = _addDriverState.value.copy(isActive = value)
    }

    // ==========================================
    // Agregar nuevo transportista (API)
    // ==========================================

    /**
     * Registra un nuevo transportista usando la API.
     */
    fun addDriver(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!validateForm()) {
                return@launch
            }

            _addDriverState.value = _addDriverState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                Log.d(TAG, "Registering new driver: ${_addDriverState.value.fullName}")

                val result = repository.registerDriver(
                    fullName = _addDriverState.value.fullName,
                    email = _addDriverState.value.email,
                    phone = _addDriverState.value.phone,
                    licenseNumber = _addDriverState.value.licenseNumber,
                    password = _addDriverState.value.password.ifBlank { "TempPass123!" }, // Password temporal si está vacío
                    licenseImageBase64 = "" // TODO: Implementar captura de imagen
                )

                result.onSuccess { driver ->
                    Log.d(TAG, "Driver registered successfully: ${driver.id}")

                    // Agregar al inicio de la lista
                    _transportistas.add(0, driver)

                    // Resetear formulario
                    _addDriverState.value = AddDriverState()

                    onSuccess()
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error registering driver: ${error.message}")

                    _addDriverState.value = _addDriverState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al registrar transportista: ${error.message}"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception registering driver", e)

                _addDriverState.value = _addDriverState.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    // ==========================================
    // Validación del formulario
    // ==========================================

    private fun validateForm(): Boolean {
        var isValid = true
        val state = _addDriverState.value

        // Validar nombre completo
        if (state.fullName.isBlank()) {
            _addDriverState.value = _addDriverState.value.copy(
                fullNameError = "El nombre completo es requerido"
            )
            isValid = false
        } else if (state.fullName.length < 3) {
            _addDriverState.value = _addDriverState.value.copy(
                fullNameError = "El nombre debe tener al menos 3 caracteres"
            )
            isValid = false
        }

        // Validar email
        if (state.email.isBlank()) {
            _addDriverState.value = _addDriverState.value.copy(
                emailError = "El correo electrónico es requerido"
            )
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _addDriverState.value = _addDriverState.value.copy(
                emailError = "Formato de correo inválido"
            )
            isValid = false
        }

        // Validar teléfono
        if (state.phone.isBlank()) {
            _addDriverState.value = _addDriverState.value.copy(
                phoneError = "El teléfono es requerido"
            )
            isValid = false
        } else if (state.phone.length < 10) {
            _addDriverState.value = _addDriverState.value.copy(
                phoneError = "El teléfono debe tener al menos 10 dígitos"
            )
            isValid = false
        }

        // Validar número de licencia
        if (state.licenseNumber.isBlank()) {
            _addDriverState.value = _addDriverState.value.copy(
                licenseNumberError = "El número de licencia es requerido"
            )
            isValid = false
        }

        // Validar fecha de caducidad (opcional ahora)
        if (state.licenseCaducation.isNotBlank()) {
            try {
                val date = LocalDate.parse(state.licenseCaducation, DateTimeFormatter.ISO_DATE)
                if (date.isBefore(LocalDate.now())) {
                    _addDriverState.value = _addDriverState.value.copy(
                        licenseCaducationError = "La licencia está caducada"
                    )
                    isValid = false
                }
            } catch (e: Exception) {
                _addDriverState.value = _addDriverState.value.copy(
                    licenseCaducationError = "Formato de fecha inválido (YYYY-MM-DD)"
                )
                isValid = false
            }
        }

        // Validar contraseña (opcional, se genera automática si está vacía)
        if (state.password.isNotBlank() && state.password.length < 6) {
            _addDriverState.value = _addDriverState.value.copy(
                passwordError = "La contraseña debe tener al menos 6 caracteres"
            )
            isValid = false
        }

        return isValid
    }

    /**
     * Resetea el estado del formulario.
     */
    fun resetAddDriverState() {
        _addDriverState.value = AddDriverState()
    }
}