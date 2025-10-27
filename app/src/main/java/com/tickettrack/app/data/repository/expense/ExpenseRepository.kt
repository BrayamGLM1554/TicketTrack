package com.tickettrack.app.data.repository.expense

import com.tickettrack.app.data.model.expense.*
import com.tickettrack.app.domain.model.expense.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Repositorio para gestión de gastos
 * NOTA: Actualmente usa datos simulados (mock)
 * TODO: Reemplazar con llamadas reales al API Gateway cuando esté disponible
 */
class ExpenseRepository {

    // Mock data - Gastos simulados
    private val mockExpenses = mutableListOf<ExpenseResponse>(
        ExpenseResponse(
            id = "exp_001",
            tripId = "trip_001",
            amount = 850.0,
            currency = "MXN",
            category = "fuel",
            description = "Gasolina Pemex - Querétaro",
            date = Instant.now().minus(2, ChronoUnit.DAYS).toString(),
            ticketImagePath = "tickets/trip_001/exp_001_ticket.jpg",
            ticketPublicUrl = "https://storage.googleapis.com/ticket001.jpg",
            ticketModerationStatus = "approved",
            moderationNote = "",
            moderatedAt = Instant.now().minus(2, ChronoUnit.DAYS).toString(),
            moderatedBy = "ai-moderator",
            location = LocationDTO(
                address = "Gasolinera Pemex, Querétaro",
                coordinates = CoordinatesDTO(20.5888, -100.3899)
            ),
            createdBy = "driver1@tickettrack.com",
            createdByName = "Juan Pérez",
            createdAt = Instant.now().minus(2, ChronoUnit.DAYS).toString(),
            updatedAt = Instant.now().minus(2, ChronoUnit.DAYS).toString()
        ),
        ExpenseResponse(
            id = "exp_002",
            tripId = "trip_001",
            amount = 350.0,
            currency = "MXN",
            category = "food",
            description = "Comida restaurante La Parilla",
            date = Instant.now().minus(1, ChronoUnit.DAYS).toString(),
            ticketImagePath = "tickets/trip_001/exp_002_ticket.jpg",
            ticketPublicUrl = "https://storage.googleapis.com/ticket002.jpg",
            ticketModerationStatus = "approved",
            moderationNote = "",
            moderatedAt = Instant.now().minus(1, ChronoUnit.DAYS).toString(),
            moderatedBy = "ai-moderator",
            createdBy = "driver1@tickettrack.com",
            createdByName = "Juan Pérez",
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS).toString(),
            updatedAt = Instant.now().minus(1, ChronoUnit.DAYS).toString()
        ),
        ExpenseResponse(
            id = "exp_003",
            tripId = "trip_001",
            amount = 150.0,
            currency = "MXN",
            category = "toll",
            description = "Caseta México-Querétaro",
            date = Instant.now().minus(1, ChronoUnit.DAYS).toString(),
            ticketImagePath = "tickets/trip_001/exp_003_ticket.jpg",
            ticketPublicUrl = "https://storage.googleapis.com/ticket003.jpg",
            ticketModerationStatus = "approved",
            moderationNote = "",
            moderatedAt = Instant.now().minus(1, ChronoUnit.DAYS).toString(),
            moderatedBy = "ai-moderator",
            createdBy = "driver1@tickettrack.com",
            createdByName = "Juan Pérez",
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS).toString(),
            updatedAt = Instant.now().minus(1, ChronoUnit.DAYS).toString()
        ),
        ExpenseResponse(
            id = "exp_004",
            tripId = "trip_002",
            amount = 1200.0,
            currency = "MXN",
            category = "maintenance",
            description = "Cambio de aceite y filtros",
            date = Instant.now().minus(5, ChronoUnit.DAYS).toString(),
            ticketImagePath = "tickets/trip_002/exp_004_ticket.jpg",
            ticketPublicUrl = "https://storage.googleapis.com/ticket004.jpg",
            ticketModerationStatus = "approved",
            moderationNote = "",
            moderatedAt = Instant.now().minus(5, ChronoUnit.DAYS).toString(),
            moderatedBy = "ai-moderator",
            createdBy = "driver2@tickettrack.com",
            createdByName = "María González",
            createdAt = Instant.now().minus(5, ChronoUnit.DAYS).toString(),
            updatedAt = Instant.now().minus(5, ChronoUnit.DAYS).toString()
        )
    )

    /**
     * Crea un nuevo gasto
     * TODO: Implementar POST /api/trips/{tripId}/expenses
     */
    suspend fun createExpense(request: ExpenseRequest): Result<Expense> {
        return try {
            // Simular latencia de red
            delay(2000)

            // Simular moderación de imagen (siempre aprobada en mock)
            delay(1000)

            // TODO: Reemplazar con llamada real a la API
            // val response = httpClient.post("/api/trips/${request.tripId}/expenses") {
            //     contentType(ContentType.Application.Json)
            //     setBody(request)
            // }

            val newExpense = ExpenseResponse(
                id = "exp_${System.currentTimeMillis()}",
                tripId = request.tripId,
                amount = request.amount,
                currency = request.currency,
                category = request.category,
                description = request.description,
                date = request.date,
                ticketImagePath = "tickets/${request.tripId}/ticket_${System.currentTimeMillis()}.jpg",
                ticketPublicUrl = "https://storage.googleapis.com/mock_ticket.jpg",
                ticketModerationStatus = "approved", // Simulado
                moderationNote = "",
                moderatedAt = Instant.now().toString(),
                moderatedBy = "ai-moderator",
                location = request.location,
                createdBy = request.createdBy,
                createdByName = request.createdByName,
                createdAt = Instant.now().toString(),
                updatedAt = Instant.now().toString()
            )

            mockExpenses.add(newExpense)
            Result.success(newExpense.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los gastos de un viaje
     * TODO: Implementar GET /api/trips/{tripId}/expenses
     */
    suspend fun getExpensesByTrip(tripId: String): Result<List<Expense>> {
        return try {
            delay(1000)

            // TODO: Reemplazar con llamada real
            // val response = httpClient.get("/api/trips/$tripId/expenses")

            val expenses = mockExpenses
                .filter { it.tripId == tripId }
                .map { it.toDomain() }
                .sortedByDescending { it.date }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los gastos del usuario (todos sus viajes)
     * TODO: Implementar GET /api/expenses?userId={userId}
     */
    suspend fun getExpensesByUser(userId: String): Result<List<Expense>> {
        return try {
            delay(1000)

            // TODO: Reemplazar con llamada real
            // val response = httpClient.get("/api/expenses?userId=$userId")

            val expenses = mockExpenses
                .filter { it.createdBy == userId }
                .map { it.toDomain() }
                .sortedByDescending { it.date }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene gastos filtrados por categoría
     * TODO: Implementar GET /api/expenses?category={category}
     */
    suspend fun getExpensesByCategory(
        userId: String,
        category: ExpenseCategory
    ): Result<List<Expense>> {
        return try {
            delay(800)

            val expenses = mockExpenses
                .filter { it.createdBy == userId && it.category == category.apiValue }
                .map { it.toDomain() }
                .sortedByDescending { it.date }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene estadísticas de gastos
     * TODO: Implementar GET /api/expenses/stats?userId={userId}&period={period}
     */
    suspend fun getExpenseStats(
        userId: String,
        period: TimePeriod
    ): Result<ExpenseStats> {
        return try {
            delay(1200)

            // TODO: Reemplazar con llamada real
            // val response = httpClient.get("/api/expenses/stats?userId=$userId&period=${period.name.lowercase()}")

            val userExpenses = mockExpenses
                .filter { it.createdBy == userId }
                .map { it.toDomain() }

            // Calcular estadísticas mock
            val total = userExpenses.sumOf { it.amount }
            val count = userExpenses.size
            val average = if (count > 0) total / count else 0.0

            val byCategory = userExpenses
                .groupBy { it.category }
                .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }

            val byDate = userExpenses
                .groupBy { it.date.toString().substring(0, 10) }
                .map { (date, expenses) ->
                    DateExpense(
                        date = date,
                        amount = expenses.sumOf { it.amount },
                        count = expenses.size
                    )
                }
                .sortedBy { it.date }

            val stats = ExpenseStats(
                totalAmount = total,
                expenseCount = count,
                averageAmount = average,
                expensesByCategory = byCategory,
                expensesByDate = byDate
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un gasto
     * TODO: Implementar DELETE /api/expenses/{expenseId}
     */
    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            delay(500)

            // TODO: Reemplazar con llamada real
            // httpClient.delete("/api/expenses/$expenseId")

            mockExpenses.removeIf { it.id == expenseId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}