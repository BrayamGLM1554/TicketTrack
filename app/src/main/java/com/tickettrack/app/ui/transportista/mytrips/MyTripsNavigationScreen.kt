package com.tickettrack.app.ui.transportista.mytrips

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.data.local.TokenManager

/**
 * Pantalla de navegación interna para el módulo de Mis Viajes.
 *
 * Gestiona la navegación entre:
 * - Lista de viajes (MyTripsScreen)
 * - Detalle de viaje (MyTripDetailScreen)
 * - Registro de gasto (vincula con ExpenseRegisterScreen)
 * - Solicitud de presupuesto (vincula con BudgetRequestScreen)
 */
@Composable
fun MyTripsNavigationScreen() {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    // Estado de navegación
    var currentScreen by remember { mutableStateOf("list") }
    var selectedTripId by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        "list" -> {
            MyTripsScreen(
                onNavigateToDetail = { tripId ->
                    selectedTripId = tripId
                    currentScreen = "detail"
                }
            )
        }

        "detail" -> {
            selectedTripId?.let { tripId ->
                MyTripDetailScreen(
                    tripId = tripId,
                    onNavigateBack = {
                        currentScreen = "list"
                        selectedTripId = null
                    },
                    onNavigateToRegisterExpense = { tripId ->
                        // TODO: Navegar a ExpenseRegisterScreen
                        // Por ahora, mostrar mensaje
                        selectedTripId = tripId
                        currentScreen = "registerExpense"
                    },
                    onNavigateToBudgetRequest = { tripId ->
                        // TODO: Navegar a BudgetRequestScreen
                        // Por ahora, mostrar mensaje
                        selectedTripId = tripId
                        currentScreen = "budgetRequest"
                    }
                )
            }
        }

        "registerExpense" -> {
            // TODO: Implementar cuando tengamos ExpenseRegisterScreen
            // Por ahora, pantalla temporal
            TemporaryScreen(
                message = "Pantalla de Registro de Gasto\n(Próximamente)",
                onBack = { currentScreen = "detail" }
            )
        }

        "budgetRequest" -> {
            // TODO: Implementar cuando tengamos BudgetRequestScreen
            // Por ahora, pantalla temporal
            TemporaryScreen(
                message = "Pantalla de Solicitud de Presupuesto\n(Próximamente)",
                onBack = { currentScreen = "detail" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemporaryScreen(
    message: String,
    onBack: () -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("En Desarrollo") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Construction,
                    contentDescription = null,
                    modifier = androidx.compose.ui.Modifier.size(80.dp),
                    tint = androidx.compose.ui.graphics.Color.Gray
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                androidx.compose.material3.Text(
                    text = message,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 18.sp
                )
            }
        }
    }
}