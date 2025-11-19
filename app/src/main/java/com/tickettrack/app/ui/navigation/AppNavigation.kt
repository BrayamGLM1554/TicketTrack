package com.tickettrack.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tickettrack.app.data.local.SessionManager
import com.tickettrack.app.domain.model.UserProfile
import com.tickettrack.app.ui.auth.GoogleAuthViewModel
import com.tickettrack.app.ui.auth.LoginScreen
import com.tickettrack.app.ui.auth.LoginViewModel
import com.tickettrack.app.ui.auth.RegisterScreen
import com.tickettrack.app.ui.auth.RegisterWithGoogleScreen
import com.tickettrack.app.ui.drivers.CreateDriverScreen
import com.tickettrack.app.ui.drivers.DriverDetailsScreen
import com.tickettrack.app.ui.expenses.BudgetRequestDetailsScreen
import com.tickettrack.app.ui.expenses.ExpenseDetailsScreen
import com.tickettrack.app.ui.expenses.ExpensesScreen
import com.tickettrack.app.ui.main.MainScreen
import com.tickettrack.app.ui.notifications.NotificationViewModel
import com.tickettrack.app.ui.notifications.NotificationsScreen
import com.tickettrack.app.ui.profile.ProfileScreen
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.transportista.RegisterExpenseScreen
import com.tickettrack.app.ui.transportista.RequestBudgetIncreaseScreen
import com.tickettrack.app.ui.trips.CreateTripScreen
import com.tickettrack.app.ui.trips.TripDetailsScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sessionManager: SessionManager = koinInject()
    val scope = rememberCoroutineScope()

    var currentUserProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isCheckingSession by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(Screen.Login.route) }

    // Obtener ViewModels a nivel de navegación
    val loginViewModel: LoginViewModel = koinViewModel()
    val googleViewModel: GoogleAuthViewModel = koinViewModel()

    val notificationViewModel: NotificationViewModel = koinInject()

    // Verificar sesión persistente al iniciar
    LaunchedEffect(Unit) {
        val savedSession = sessionManager.getSession()
        if (savedSession != null) {
            currentUserProfile = savedSession
            startDestination = Screen.Main.route
        }
        isCheckingSession = false
    }

    // Mostrar splash mientras verifica sesión
    if (isCheckingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { userProfile ->
                    currentUserProfile = userProfile
                    // NUEVO: Guardar sesión
                    scope.launch {
                        sessionManager.saveSession(userProfile)
                    }
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToRegisterWithGoogle = { name, email ->
                    navController.navigate(Screen.RegisterWithGoogle.createRoute(name, email))
                },
                loginViewModel = loginViewModel,
                googleViewModel = googleViewModel
            )
        }

        // Registro normal
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        // Registro con Google (completar datos)
        composable(
            route = Screen.RegisterWithGoogle.route,
            arguments = listOf(
                navArgument("userName") { type = NavType.StringType },
                navArgument("userEmail") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val userEmail = backStackEntry.arguments?.getString("userEmail") ?: ""

            RegisterWithGoogleScreen(
                userName = userName,
                userEmail = userEmail,
                onRegistrationSuccess = { userProfile ->
                    currentUserProfile = userProfile
                    // NUEVO: Guardar sesión
                    scope.launch {
                        sessionManager.saveSession(userProfile)
                    }
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackPressed = {
                    navController.popBackStack()
                },
                viewModel = googleViewModel
            )
        }

        // Main Screen
        composable(Screen.Main.route) {
            currentUserProfile?.let { userProfile ->
                MainScreen(
                    userProfile = userProfile,
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToNotifications = { // NUEVO
                        navController.navigate(Screen.Notifications.route)
                    },
                    notificationViewModel = notificationViewModel,
                            onNavigateToCreateDriver = {
                        navController.navigate(Screen.CreateDriver.route)
                    },
                    onNavigateToDriverDetails = { driverUid ->
                        navController.navigate(Screen.DriverDetails.createRoute(driverUid))
                    },
                    onNavigateToCreateTrip = {
                        navController.navigate(Screen.CreateTrip.route)
                    },
                    onNavigateToTripDetails = { tripId ->
                        navController.navigate(Screen.TripDetails.createRoute(tripId))
                    },
                    onNavigateToBudgetRequestDetails = { requestId ->
                        navController.navigate(Screen.BudgetRequestDetails.createRoute(requestId))
                    },
                    onNavigateToExpenseDetails = { expenseId ->
                        navController.navigate(Screen.ExpenseDetails.createRoute(expenseId))
                    },
                    onNavigateToRegisterExpense = { tripId, driverId ->
                        navController.navigate(Screen.RegisterExpense.createRoute(tripId, driverId))
                    },
                    onNavigateToRequestBudget = { tripId, driverId, currentBudget ->
                        navController.navigate(Screen.RequestBudget.createRoute(tripId, driverId, currentBudget))
                    },
                    onLogout = {
                        handleLogout(
                            navController = navController,
                            loginViewModel = loginViewModel,
                            googleViewModel = googleViewModel,
                            sessionManager = sessionManager,
                            onLogoutComplete = {
                                currentUserProfile = null
                            }
                        )
                    }
                )
            }
        }

        // Profile
        composable(Screen.Profile.route) {
            currentUserProfile?.let { userProfile ->
                ProfileScreen(
                    userProfile = userProfile,
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onNavigateToNotifications = { // NUEVO
                        navController.navigate(Screen.Notifications.route)
                    },

                    onLogout = {
                        handleLogout(
                            navController = navController,
                            loginViewModel = loginViewModel,
                            googleViewModel = googleViewModel,
                            sessionManager = sessionManager,
                            onLogoutComplete = {
                                currentUserProfile = null
                            }
                        )
                    }
                )
            }
        }

        // NUEVO: Notificaciones
        composable(Screen.Notifications.route) {
            currentUserProfile?.let { userProfile ->
                NotificationsScreen(
                    token = userProfile.token,
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onNavigateToTripDetails = { tripId ->
                        navController.navigate(Screen.TripDetails.createRoute(tripId))
                    },
                    onNavigateToBudgetRequestDetails = { requestId ->
                        navController.navigate(Screen.BudgetRequestDetails.createRoute(requestId))
                    },
                    viewModel = notificationViewModel
                )
            }
        }

        // Create Driver
        composable(Screen.CreateDriver.route) {
            currentUserProfile?.let { userProfile ->
                CreateDriverScreen(
                    token = userProfile.token,
                    companyName = userProfile.companyName,
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onDriverCreated = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Driver Details
        composable(
            route = Screen.DriverDetails.route,
            arguments = listOf(
                navArgument("driverUid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val driverUid = backStackEntry.arguments?.getString("driverUid") ?: ""
            currentUserProfile?.let { userProfile ->
                DriverDetailsScreen(
                    token = userProfile.token,
                    driverUid = driverUid,
                    isAdmin = userProfile.role == "ADMIN",
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Create Trip
        composable(Screen.CreateTrip.route) {
            currentUserProfile?.let { userProfile ->
                CreateTripScreen(
                    token = userProfile.token,
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onTripCreated = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Trip Details
        composable(
            route = Screen.TripDetails.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            currentUserProfile?.let { userProfile ->
                TripDetailsScreen(
                    token = userProfile.token,
                    tripId = tripId,
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onNavigateToExpenseDetails = { expenseId ->
                        navController.navigate(Screen.ExpenseDetails.createRoute(expenseId))
                    }
                )
            }
        }

        // Expenses
        composable(Screen.Expenses.route) {
            currentUserProfile?.let { userProfile ->
                ExpensesScreen(
                    token = userProfile.token,
                    onNavigateToBudgetRequestDetails = { requestId ->
                        navController.navigate(Screen.BudgetRequestDetails.createRoute(requestId))
                    }
                )
            }
        }

        // Budget Request Details
        composable(
            route = Screen.BudgetRequestDetails.route,
            arguments = listOf(
                navArgument("requestId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            currentUserProfile?.let { userProfile ->
                BudgetRequestDetailsScreen(
                    token = userProfile.token,
                    requestId = requestId,
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onActionComplete = {
                        // Lógica adicional si es necesaria
                    }
                )
            }
        }

        // Expense Details
        composable(
            route = Screen.ExpenseDetails.route,
            arguments = listOf(
                navArgument("expenseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
            currentUserProfile?.let { userProfile ->
                ExpenseDetailsScreen(
                    token = userProfile.token,
                    expenseId = expenseId,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Register Expense
        composable(
            route = Screen.RegisterExpense.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType },
                navArgument("driverId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            val driverId = backStackEntry.arguments?.getString("driverId") ?: ""

            currentUserProfile?.let { userProfile ->
                RegisterExpenseScreen(
                    token = userProfile.token,
                    tripId = tripId,
                    driverId = driverId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Request Budget Increase
        composable(
            route = Screen.RequestBudget.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType },
                navArgument("driverId") { type = NavType.StringType },
                navArgument("currentBudget") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            val driverId = backStackEntry.arguments?.getString("driverId") ?: ""
            val currentBudget = backStackEntry.arguments?.getString("currentBudget")?.toDoubleOrNull() ?: 0.0

            currentUserProfile?.let { userProfile ->
                RequestBudgetIncreaseScreen(
                    token = userProfile.token,
                    tripId = tripId,
                    driverId = driverId,
                    currentBudget = currentBudget,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

// Función centralizada para manejar el logout
private fun handleLogout(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    googleViewModel: GoogleAuthViewModel,
    sessionManager: SessionManager, // NUEVO
    onLogoutComplete: () -> Unit
) {
    // 1. Limpiar estados de Google
    googleViewModel.resetStates()

    // 2. Limpiar estados de login normal
    loginViewModel.resetStates()

    // 3. NUEVO: Limpiar sesión persistente
    kotlinx.coroutines.GlobalScope.launch {
        sessionManager.clearSession()
    }

    // 4. Limpiar usuario actual
    onLogoutComplete()

    // 5. Navegar a login limpiando todo el stack
    navController.navigate(Screen.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}