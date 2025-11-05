package com.tickettrack.app.ui.main.drivers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tickettrack.app.data.local.TokenManager

/**
 * Factory para crear instancias de DriverViewModel con dependencias.
 */
class DriverViewModelFactory(
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DriverViewModel::class.java)) {
            return DriverViewModel(tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}