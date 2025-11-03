package com.tickettrack.app.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.ui.main.components.BottomNavBar
import com.tickettrack.app.ui.main.components.TopBar
import com.tickettrack.app.ui.main.dashboard.MainDashboardScreen
import com.tickettrack.app.ui.main.drivers.DriversScreen
import com.tickettrack.app.ui.main.drivers.DriverViewModel
import com.tickettrack.app.ui.trips.TripNavigationScreen
import com.tickettrack.app.ui.expenses.ExpenseMainNavigationScreen
import com.tickettrack.app.ui.transportista.TransportistaMainNavigationScreen // ✅ NUEVO IMPORT

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    // ✅ NUEVO: Obtener TokenManager para leer el rol
    val tokenManager = remember { TokenManager(context) }
    val userRole = tokenManager.getUserRole() ?: "USER"

    val viewModel: MainViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    val state = viewModel.state.collectAsState()
    val driverViewModel: DriverViewModel = viewModel()

    Scaffold(
        topBar = {
            TopBar(
                companyName = state.value.companyName,
                onLogout = {
                    viewModel.logout()
                    onLogout()
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedItem = state.value.selectedTab,
                onItemSelected = { viewModel.onTabSelected(it) }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // ✅ MODIFICACIÓN PRINCIPAL: Decidir qué mostrar según el rol
            if (userRole.equals("USER", ignoreCase = true)) {
                // ✅ TRANSPORTISTA (USER) - Usar el nuevo módulo
                TransportistaMainNavigationScreen(
                    selectedTab = state.value.selectedTab,
                    onLogout = {
                        viewModel.logout()
                        onLogout()
                    }
                )
            } else {
                // ✅ ADMIN/CONSIGNATARIO - Usar pantallas existentes
                when (state.value.selectedTab) {
                    "Inicio" -> MainDashboardScreen(viewModel = viewModel)
                    "Viajes" -> TripNavigationScreen()
                    "Gastos" -> ExpenseMainNavigationScreen()
                    "Transportista" -> DriversScreen(viewModel = driverViewModel)
                }
            }
        }
    }
}