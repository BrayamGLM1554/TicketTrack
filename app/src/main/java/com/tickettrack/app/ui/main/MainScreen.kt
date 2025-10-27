package com.tickettrack.app.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.ui.main.components.BottomNavBar
import com.tickettrack.app.ui.main.components.TopBar
import com.tickettrack.app.ui.main.dashboard.MainDashboardScreen
import com.tickettrack.app.ui.main.drivers.DriversScreen
import com.tickettrack.app.ui.main.drivers.DriverViewModel
import com.tickettrack.app.ui.trips.TripNavigationScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
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
            when (state.value.selectedTab) {
                "Inicio" -> MainDashboardScreen(viewModel = viewModel)
                "Viajes" -> TripNavigationScreen() // ✅ Integración del módulo de viajes
                "Gastos" -> GastosScreen()
                "Transportista" -> DriversScreen(viewModel = driverViewModel)
            }
        }
    }
}

@Composable
fun GastosScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Pantalla de Gastos - Por implementar")
    }
}