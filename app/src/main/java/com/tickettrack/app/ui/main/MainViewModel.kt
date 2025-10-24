package com.tickettrack.app.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tickettrack.app.data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MainState(
    val companyName: String = "Ticket Track",
    val userName: String = "",
    val userEmail: String = "",
    val selectedTab: String = "Inicio",
    val viajesActivos: Int = 0,
    val enViaje: Double = 0.0,
    val pendientes: Int = 0,
    val saldoTotal: Double = 0.0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state

    private val tokenManager = TokenManager(application)

    init {
        // Cargar datos del usuario desde TokenManager
        loadUserData()
    }

    private fun loadUserData() {
        val userName = tokenManager.getUserName() ?: "Ticket Track"
        val userEmail = tokenManager.getUserEmail() ?: ""

        _state.value = _state.value.copy(
            companyName = userName, // ✅ Aquí viene el nombre del login
            userName = userName,
            userEmail = userEmail
        )
    }

    fun onTabSelected(tab: String) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    fun logout() {
        tokenManager.clearAll() // ✅ Limpia el token
    }
}