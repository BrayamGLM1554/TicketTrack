//package com.tickettrack.app.ui.expenses
//
///**
// * Constantes de navegación para el módulo de gastos
// */
//object ExpenseRoutes {
//    const val EXPENSE_LIST = "expenses/list"
//    const val EXPENSE_LIST_BY_TRIP = "expenses/list/{tripId}"
//    const val EXPENSE_REGISTER = "expenses/register/{tripId}"
//    const val EXPENSE_CHARTS = "expenses/charts"
//    const val BUDGET_REQUESTS = "expenses/budget-requests"
//
//    /**
//     * Crea la ruta para lista de gastos de un viaje
//     */
//    fun expenseListByTrip(tripId: String) = "expenses/list/$tripId"
//
//    /**
//     * Crea la ruta para registrar gasto en un viaje
//     */
//    fun expenseRegister(tripId: String) = "expenses/register/$tripId"
//}
//
///**
// * Ejemplo de integración con NavHost
// *
// * TODO: Agregar estas rutas al NavHost principal de la app
// *
// * @Composable
// * fun ExpenseNavGraph(
// *     navController: NavController,
// *     tokenManager: TokenManager
// * ) {
// *     val userId = tokenManager.getUserEmail() ?: ""
// *     val userName = tokenManager.getUserName() ?: ""
// *     val userRole = tokenManager.getUserRole() ?: ""
// *     val companyEmail = tokenManager.getCompanyEmail() ?: ""
// *
// *     // Lista de gastos (pantalla principal desde BottomNavBar)
// *     composable(ExpenseRoutes.EXPENSE_LIST) {
// *         ExpenseListScreen(
// *             userId = userId,
// *             userRole = userRole,
// *             onNavigateToRegister = {
// *                 // El transportista necesita estar en un viaje para registrar gasto
// *                 // Mostrar diálogo o navegar a lista de viajes
// *             },
// *             onNavigateToCharts = {
// *                 navController.navigate(ExpenseRoutes.EXPENSE_CHARTS)
// *             },
// *             onNavigateToBudgetRequests = {
// *                 navController.navigate(ExpenseRoutes.BUDGET_REQUESTS)
// *             }
// *         )
// *     }
// *
// *     // Lista de gastos de un viaje específico
// *     composable(
// *         route = ExpenseRoutes.EXPENSE_LIST_BY_TRIP,
// *         arguments = listOf(
// *             navArgument("tripId") { type = NavType.StringType }
// *         )
// *     ) { backStackEntry ->
// *         val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
// *
// *         ExpenseListScreen(
// *             userId = userId,
// *             userRole = userRole,
// *             tripId = tripId,
// *             onNavigateToRegister = {
// *                 navController.navigate(ExpenseRoutes.expenseRegister(tripId))
// *             },
// *             onNavigateToCharts = {
// *                 navController.navigate(ExpenseRoutes.EXPENSE_CHARTS)
// *             },
// *             onNavigateToBudgetRequests = {
// *                 navController.navigate(ExpenseRoutes.BUDGET_REQUESTS)
// *             }
// *         )
// *     }
// *
// *     // Registrar gasto
// *     composable(
// *         route = ExpenseRoutes.EXPENSE_REGISTER,
// *         arguments = listOf(
// *             navArgument("tripId") { type = NavType.StringType }
// *         )
// *     ) { backStackEntry ->
// *         val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
// *
// *         ExpenseRegisterScreen(
// *             tripId = tripId,
// *             userId = userId,
// *             userName = userName,
// *             onNavigateBack = { navController.popBackStack() },
// *             onExpenseRegistered = {
// *                 // Volver a la lista de gastos del viaje
// *                 navController.popBackStack()
// *             }
// *         )
// *     }
// *
// *     // Gráficas de gastos
// *     composable(ExpenseRoutes.EXPENSE_CHARTS) {
// *         ExpenseChartsScreen(
// *             userId = userId,
// *             onNavigateBack = { navController.popBackStack() }
// *         )
// *     }
// *
// *     // Solicitudes de presupuesto
// *     composable(ExpenseRoutes.BUDGET_REQUESTS) {
// *         BudgetRequestListScreen(
// *             userEmail = userId,
// *             userName = userName,
// *             userRole = userRole,
// *             companyEmail = companyEmail,
// *             onNavigateBack = { navController.popBackStack() }
// *         )
// *     }
// * }
// */