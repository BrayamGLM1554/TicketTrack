package com.tickettrack.app.ui.main.trips

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.ui.main.trips.create.TripCreateScreen
import com.tickettrack.app.ui.main.trips.detail.TripDetailScreen
import com.tickettrack.app.ui.main.trips.list.TripListScreen

/**
 * Navegación del módulo de Viajes.
 *
 * Maneja la navegación entre:
 * - Lista de viajes
 * - Crear viaje
 * - Detalle de viaje
 *
 * Usa el TokenManager para obtener datos del usuario actual.
 */
@Composable
fun TripNavigationScreen() {
    // Obtener datos del usuario desde TokenManager
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    // Datos del usuario actual
    val currentUserId = tokenManager.getUserEmail() ?: ""
    val currentUserName = tokenManager.getUserName() ?: "Usuario"
    val currentUserRole = tokenManager.getUserRole() ?: "ADMIN"

    // Estado de navegación
    var currentScreen by remember { mutableStateOf("list") }
    var selectedTripId by remember { mutableStateOf("") }

    // Navegación entre pantallas
    when (currentScreen) {
        "list" -> {
            TripListScreen(
                onNavigateToCreateTrip = {
                    currentScreen = "create"
                },
                onNavigateToTripDetail = { tripId ->
                    selectedTripId = tripId
                    currentScreen = "detail"
                },
                currentUserId = currentUserId,
                currentUserRole = currentUserRole
            )
        }

        "create" -> {
            TripCreateScreen(
                onNavigateBack = {
                    currentScreen = "list"
                },
                onTripCreated = {
                    currentScreen = "list"
                },
                currentUserId = currentUserId,
                currentUserName = currentUserName
            )
        }

        "detail" -> {
            // ✅ CORRECCIÓN: TripDetailScreen solo necesita tripId y callback
            TripDetailScreen(
                tripId = selectedTripId,
                onNavigateBack = {
                    currentScreen = "list"
                }
            )
        }
    }
}