package com.tickettrack.app.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.data.model.driver.Driver
import com.tickettrack.app.ui.main.components.BottomNavBar
import com.tickettrack.app.ui.main.components.TopBar
import com.tickettrack.app.ui.main.dashboard.MainDashboardScreen
import com.tickettrack.app.ui.main.drivers.DriversScreen
import com.tickettrack.app.ui.main.drivers.DriverViewModel
import com.tickettrack.app.ui.main.drivers.DriverViewModelFactory
import com.tickettrack.app.ui.main.trips.TripNavigationScreen
import com.tickettrack.app.ui.expenses.ExpenseMainNavigationScreen
import com.tickettrack.app.ui.main.drivers.AddDriverScreen
import com.tickettrack.app.ui.main.drivers.DriverDetailScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    // MainViewModel (ya lo tienes funcionando)
    val viewModel: MainViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    val state = viewModel.state.collectAsState()

    // ✅ SOLUCIÓN: Crear TokenManager y Factory para DriverViewModel
    val tokenManager = remember { TokenManager(context) }
    val driverViewModelFactory = remember { DriverViewModelFactory(tokenManager) }
    val driverViewModel: DriverViewModel = viewModel(factory = driverViewModelFactory)

    var showAddDriver by remember { mutableStateOf(false) }
    var selectedDriver by remember { mutableStateOf<Driver?>(null) }

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
                onItemSelected = {
                    showAddDriver = false
                    selectedDriver = null
                    viewModel.onTabSelected(it)
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                selectedDriver != null -> {
                    DriverDetailScreen(
                        driver = selectedDriver!!,
                        onBack = { selectedDriver = null }
                    )
                }

                showAddDriver -> {
                    AddDriverScreen(
                        viewModel = driverViewModel,
                        onBack = {
                            showAddDriver = false
                        },
                        onDriverAdded = {
                            showAddDriver = false
                        }
                    )
                }

                else -> {
                    // Navegación por tabs
                    when (state.value.selectedTab) {
                        "Inicio" -> MainDashboardScreen(viewModel = viewModel)
                        "Viajes" -> TripNavigationScreen()
                        "Gastos" -> ExpenseMainNavigationScreen()
                        "Transportista" -> DriversScreen(
                            viewModel = driverViewModel,
                            onNavigateToAddDriver = { showAddDriver = true },
                            onDriverClick = { driver ->
                                selectedDriver = driver
                            }
                        )
                    }
                }
            }
        }
    }
}