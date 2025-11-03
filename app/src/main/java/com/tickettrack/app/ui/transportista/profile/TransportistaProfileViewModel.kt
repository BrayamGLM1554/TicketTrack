package com.tickettrack.app.ui.transportista.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.local.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ViewModel para el perfil del transportista.
 *
 * Responsabilidades:
 * - Cargar información personal del transportista
 * - Mostrar estadísticas básicas
 * - Solo lectura (RF15)
 *
 * TODO: Conectar con API GET /api/transportistas/{id}
 */
class TransportistaProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "TransportistaProfileVM"
    }

    private val tokenManager = TokenManager(application)

    private val _state = MutableStateFlow(TransportistaProfileState())
    val state: StateFlow<TransportistaProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Carga el perfil del transportista.
     *
     * TODO: Reemplazar con:
     * val result = transportistaRepository.getTransportistaByEmail(userEmail)
     */
    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                Log.d(TAG, "Loading transportista profile")

                // Obtener datos básicos del TokenManager
                val name = tokenManager.getUserName() ?: "Usuario"
                val email = tokenManager.getUserEmail() ?: ""
                val companyEmail = tokenManager.getCompanyEmail() ?: ""

                // Simular delay de red
                delay(1000)

                // TODO: Llamada real a API
                // val result = repository.getTransportistaByEmail(email)

                // Mock: Datos simulados
                val mockStats = getMockStats()

                _state.update {
                    it.copy(
                        name = name,
                        email = email,
                        phone = mockStats.phone,
                        companyEmail = companyEmail,
                        licenseNumber = mockStats.licenseNumber,
                        licenseExpiry = mockStats.licenseExpiry,
                        totalTripsCompleted = mockStats.totalTripsCompleted,
                        totalTripsInProgress = mockStats.totalTripsInProgress,
                        totalExpensesRegistered = mockStats.totalExpensesRegistered,
                        averageExpensePerTrip = mockStats.averageExpensePerTrip,
                        memberSince = mockStats.memberSince,
                        isLoading = false
                    )
                }

                Log.d(TAG, "Profile loaded successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar perfil: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Refresca el perfil.
     */
    fun refreshProfile() {
        loadProfile()
    }

    /**
     * Limpia el mensaje de error.
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    // ==========================================
    // DATOS MOCK (Remover cuando API esté lista)
    // ==========================================

    /**
     * Obtiene estadísticas mock del transportista.
     */
    private fun getMockStats(): MockTransportistaStats {
        val now = Instant.now()
        val memberSince = now.minusSeconds(90 * 24 * 60 * 60) // 90 días atrás

        val formatter = DateTimeFormatter
            .ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.systemDefault())

        return MockTransportistaStats(
            phone = "5551234567",
            licenseNumber = "MTY123456",
            licenseExpiry = formatter.format(now.plusSeconds(365 * 24 * 60 * 60)), // 1 año adelante
            totalTripsCompleted = 12,
            totalTripsInProgress = 2,
            totalExpensesRegistered = 85430.50,
            averageExpensePerTrip = 7119.21,
            memberSince = formatter.format(memberSince)
        )
    }
}

/**
 * Clase auxiliar para datos mock.
 */
data class MockTransportistaStats(
    val phone: String,
    val licenseNumber: String,
    val licenseExpiry: String,
    val totalTripsCompleted: Int,
    val totalTripsInProgress: Int,
    val totalExpensesRegistered: Double,
    val averageExpensePerTrip: Double,
    val memberSince: String
)