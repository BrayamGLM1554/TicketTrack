package com.tickettrack.app.ui.main

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
import com.tickettrack.app.domain.model.UserProfile
import com.tickettrack.app.ui.drivers.DriversListScreen
import com.tickettrack.app.ui.expenses.ExpensesScreen
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import com.tickettrack.app.ui.transportista.TransportistaExpensesScreen
import com.tickettrack.app.ui.transportista.TransportistaHomeScreen
import com.tickettrack.app.ui.transportista.TransportistaTripsScreen
import com.tickettrack.app.ui.trips.TripsListScreen
/*
import com.tickettrack.app.ui.transportista.TransportistaHomeScreen
import com.tickettrack.app.ui.transportista.TransportistaTripsScreen
import com.tickettrack.app.ui.transportista.TransportistaExpensesScreen*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userProfile: UserProfile,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateDriver: () -> Unit,
    onNavigateToDriverDetails: (String) -> Unit,
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToTripDetails: (String) -> Unit,
    onNavigateToBudgetRequestDetails: (String) -> Unit,
    onNavigateToExpenseDetails: (String) -> Unit,
    onNavigateToRegisterExpense: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // Determinar items del bottom nav según el rol
    val bottomNavItems = if (userProfile.role == "ADMIN") {
        BottomNavItem.adminItems
    } else {
        BottomNavItem.userItems
    }

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
                    // Icono de notificaciones
                    IconButton(onClick = { /* TODO: Abrir notificaciones */ }) {
                        Badge(
                            containerColor = Color.Red,
                            modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                        ) {
                            Text(
                                text = "3",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Primary
                        )
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
                    0 -> DashboardContent()
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
                    0 -> // En el NavHost de MainScreen, cuando muestres TransportistaHomeScreen:
                        TransportistaHomeScreen(
                            token = userProfile.token,
                            driverId = userProfile.token, // o userProfile.id
                            onNavigateToTripDetails = onNavigateToTripDetails,
                            onNavigateToRegisterExpense = onNavigateToRegisterExpense)
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
}

@Composable
fun DashboardContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Dashboard,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Dashboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Próximamente",
                fontSize = 14.sp,
                color = TextSecondary
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