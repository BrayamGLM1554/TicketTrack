package com.tickettrack.app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object RegisterWithGoogle : Screen("register_google/{userName}/{userEmail}") {
        fun createRoute(userName: String, userEmail: String) =
            "register_google/$userName/$userEmail"
    }
    object Main : Screen("main")
    object Profile : Screen("profile")

    // NUEVO: Notificaciones
    object Notifications : Screen("notifications")

    // Admin routes
    object CreateDriver : Screen("create_driver")
    object DriverDetails : Screen("driver_details/{driverUid}") {
        fun createRoute(driverUid: String) = "driver_details/$driverUid"
    }
    object CreateTrip : Screen("create_trip")
    object TripDetails : Screen("trip_details/{tripId}") {
        fun createRoute(tripId: String) = "trip_details/$tripId"
    }
    object Expenses : Screen("expenses")
    object BudgetRequestDetails : Screen("budget_request_details/{requestId}") {
        fun createRoute(requestId: String) = "budget_request_details/$requestId"
    }
    object ExpenseDetails : Screen("expense_details/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_details/$expenseId"
    }

    // Transportista routes
    object RegisterExpense : Screen("register_expense/{tripId}/{driverId}") {
        fun createRoute(tripId: String, driverId: String) =
            "register_expense/$tripId/$driverId"
    }

    // Request Budget
    object RequestBudget : Screen("request_budget/{tripId}/{driverId}/{currentBudget}") {
        fun createRoute(tripId: String, driverId: String, currentBudget: Double) =
            "request_budget/$tripId/$driverId/$currentBudget"
    }
}