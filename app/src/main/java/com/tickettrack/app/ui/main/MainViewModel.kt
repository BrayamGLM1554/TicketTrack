package com.tickettrack.app.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MainState(
    val companyName: String = "TicketTrack S.A.",
    val viajesActivos: Int = 4,
    val enViaje: Double = 12500.50,
    val pendientes: Int = 2,
    val saldoTotal: Double = 87500.00,
    val selectedTab: String = "Inicio"
)

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state

    fun onTabSelected(tab: String) {
        _state.value = _state.value.copy(selectedTab = tab)
    }
}
