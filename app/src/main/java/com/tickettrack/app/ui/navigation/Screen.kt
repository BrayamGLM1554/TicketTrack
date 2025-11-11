package com.tickettrack.app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object RegisterWithGoogle : Screen("register_with_google/{userName}/{userEmail}") {
        fun createRoute(userName: String, userEmail: String) = "register_with_google/$userName/$userEmail"
    }
    object Main : Screen("main")
    object Profile : Screen("profile")
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
    // NUEVO: Ruta para registrar gastos
    object RegisterExpense : Screen("register_expense/{tripId}/{driverId}") {
        fun createRoute(tripId: String, driverId: String) = "register_expense/$tripId/$driverId"
    }
}