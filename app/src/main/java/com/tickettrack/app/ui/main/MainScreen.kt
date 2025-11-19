package com.tickettrack.app.ui.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.tickettrack.app.domain.model.NotificationType
import com.tickettrack.app.domain.model.UserProfile
import com.tickettrack.app.ui.dashboard.DashboardScreen
import com.tickettrack.app.ui.drivers.DriversListScreen
import com.tickettrack.app.ui.expenses.ExpensesScreen
import com.tickettrack.app.ui.notifications.NotificationPopupCard
import com.tickettrack.app.ui.notifications.NotificationViewModel
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import com.tickettrack.app.ui.transportista.TransportistaExpensesScreen
import com.tickettrack.app.ui.transportista.TransportistaHomeScreen
import com.tickettrack.app.ui.transportista.TransportistaTripsScreen
import com.tickettrack.app.ui.trips.TripsListScreen
import com.tickettrack.app.utils.JwtDecoder
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userProfile: UserProfile,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit, // NUEVO
    onNavigateToCreateDriver: () -> Unit,
    onNavigateToDriverDetails: (String) -> Unit,
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToTripDetails: (String) -> Unit,
    onNavigateToBudgetRequestDetails: (String) -> Unit,
    onNavigateToExpenseDetails: (String) -> Unit,
    onNavigateToRegisterExpense: (String, String) -> Unit,
    onNavigateToRequestBudget: (String, String, Double) -> Unit,
    onLogout: () -> Unit,
    notificationViewModel: NotificationViewModel
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // Estados de notificaciones
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val newNotification by notificationViewModel.newNotification.collectAsState()

    // Iniciar listeners de notificaciones
    LaunchedEffect(userProfile.token) {
        Log.d("MainScreen", "🚀 Iniciando listeners para rol: ${userProfile.role}")
        Log.d("MainScreen", "🔑 UID extraído: ${com.tickettrack.app.utils.JwtDecoder.extractUid(userProfile.token)}")
        notificationViewModel.startListening(userProfile.token)
    }

    LaunchedEffect(userProfile.token) {
        val userId = JwtDecoder.extractUid(userProfile.token)

        if (userId != null) {
            // Obtener token FCM
            FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                Log.d("MainScreen", "🔑 FCM Token obtenido: $fcmToken")

                // Guardar en Firestore
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", fcmToken)
                    .addOnSuccessListener {
                        Log.d("MainScreen", "✅ FCM Token guardado en Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainScreen", "❌ Error al guardar token: ${e.message}")
                    }
            }
        }

        // Iniciar listeners de notificaciones
        notificationViewModel.startListening(userProfile.token)
    }

    // Determinar items del bottom nav según el rol
    val bottomNavItems = if (userProfile.role == "ADMIN") {
        BottomNavItem.adminItems
    } else {
        BottomNavItem.userItems
    }

    LaunchedEffect(userProfile.token) {
        // Obtener token FCM
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            val userId = JwtDecoder.extractUid(userProfile.token)
            if (userId != null) {
                notificationViewModel.saveFcmToken(userId, fcmToken)
            }
        }

        // Iniciar listeners de notificaciones in-app
        notificationViewModel.startListening(userProfile.token)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Hola, ${userProfile.name.split(" ").firstOrNull() ?: "Usuario"}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = if (userProfile.role == "ADMIN") "Administrador" else "Transportista",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    },
                    actions = {
                        // Icono de notificaciones con badge
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = Color.Red,
                                        modifier = Modifier.offset(x = (-8).dp, y = 8.dp)
                                    ) {
                                        Text(
                                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = onNavigateToNotifications) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = Primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Avatar del usuario
                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userProfile.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF1A1A1A)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = Primary.copy(alpha = 0.1f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {
                // Contenido según el rol y pestaña seleccionada
                if (userProfile.role == "ADMIN") {
                    // Contenido para ADMIN
                    when (selectedTab) {
                        0 -> DashboardScreen(token = userProfile.token) // 🆕 CAMBIO AQUÍ
                        1 -> TripsListScreen(
                            token = userProfile.token,
                            onNavigateToCreateTrip = onNavigateToCreateTrip,
                            onNavigateToTripDetails = onNavigateToTripDetails
                        )
                        2 -> ExpensesScreen(
                            token = userProfile.token,
                            onNavigateToBudgetRequestDetails = onNavigateToBudgetRequestDetails
                        )
                        3 -> DriversListScreen(
                            token = userProfile.token,
                            onNavigateToCreateDriver = onNavigateToCreateDriver,
                            onNavigateToDriverDetails = onNavigateToDriverDetails
                        )
                    }
                } else {
                    // Contenido para USER (Transportista)
                    when (selectedTab) {
                        0 -> TransportistaHomeScreen(
                            token = userProfile.token,
                            driverId = userProfile.token,
                            onNavigateToTripDetails = onNavigateToTripDetails,
                            onNavigateToRegisterExpense = onNavigateToRegisterExpense,
                            onNavigateToRequestBudget = onNavigateToRequestBudget
                        )
                        1 -> TransportistaTripsScreen(
                            token = userProfile.token,
                            onNavigateToTripDetails = onNavigateToTripDetails
                        )
                        2 -> TransportistaExpensesScreen(
                            token = userProfile.token,
                            onNavigateToExpenseDetails = onNavigateToExpenseDetails
                        )
                    }
                }
            }
        }

        // Card emergente de nueva notificación
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        ) {
            NotificationPopupCard(
                notification = newNotification,
                onDismiss = { notificationViewModel.clearNewNotification() },
                onClick = {
                    newNotification?.let { notif ->
                        // Limpiar notificación
                        notificationViewModel.clearNewNotification()

                        // Navegar según el tipo
                        when (notif.type) {
                            NotificationType.TRIP_ASSIGNED -> {
                                onNavigateToTripDetails(notif.relatedId)
                            }
                            NotificationType.BUDGET_REQUESTED,
                            NotificationType.BUDGET_APPROVED,
                            NotificationType.BUDGET_REJECTED -> {
                                onNavigateToBudgetRequestDetails(notif.relatedId)
                            }
                            NotificationType.GENERAL -> {
                            onNavigateToNotifications()
                        }
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector
) {
    companion object {
        val adminItems = listOf(
            BottomNavItem("Inicio", Icons.Default.Home),
            BottomNavItem("Viajes", Icons.Default.DirectionsCar),
            BottomNavItem("Presupuestos", Icons.Default.AttachMoney),
            BottomNavItem("Transportistas", Icons.Default.LocalShipping)
        )

        val userItems = listOf(
            BottomNavItem("Inicio", Icons.Default.Home),
            BottomNavItem("Mis Viajes", Icons.Default.LocalShipping),
            BottomNavItem("Gastos", Icons.Default.Receipt)
        )
    }
}