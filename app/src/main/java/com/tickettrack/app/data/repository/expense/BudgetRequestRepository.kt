package com.tickettrack.app.data.repository.expense

import com.tickettrack.app.data.model.expense.BudgetRequestDTO
import com.tickettrack.app.data.model.expense.BudgetRequestResponse
import com.tickettrack.app.data.model.expense.ReviewBudgetRequestDTO
import com.tickettrack.app.domain.model.expense.BudgetRequest
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Repositorio para gestión de solicitudes de aumento de presupuesto
 * NOTA: Actualmente usa datos simulados (mock)
 * TODO: Reemplazar con llamadas reales al API Gateway
 */
class BudgetRequestRepository {

    // Mock data - Solicitudes simuladas
    private val mockRequests = mutableListOf<BudgetRequestResponse>(
        BudgetRequestResponse(
            id = "req_001",
            tripId = "trip_001",
            tripNumber = "VJ-2025-001",
            currentBudget = 5000.0,
            requestedBudget = 6500.0,
            increaseAmount = 1500.0,
            reason = "Se requiere combustible adicional debido a desvío por construcción en la carretera",
            urgency = "medium",
            requestedBy = "driver1@tickettrack.com",
            requestedByName = "Juan Pérez",
            status = "pending",
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS).toString(),
            updatedAt = Instant.now().minus(1, ChronoUnit.DAYS).toString()
        ),
        BudgetRequestResponse(
            id = "req_002",
            tripId = "trip_003",
            tripNumber = "VJ-2025-003",
            currentBudget = 8000.0,
            requestedBudget = 10000.0,
            increaseAmount = 2000.0,
            reason = "Mantenimiento urgente del vehículo - falla en el sistema de frenos",
            urgency = "high",
            requestedBy = "driver2@tickettrack.com",
            requestedByName = "María González",
            status = "pending",
            createdAt = Instant.now().minus(2, ChronoUnit.HOURS).toString(),
            updatedAt = Instant.now().minus(2, ChronoUnit.HOURS).toString()
        ),
        BudgetRequestResponse(
            id = "req_003",
            tripId = "trip_004",
            tripNumber = "VJ-2025-004",
            currentBudget = 4500.0,
            requestedBudget = 5000.0,
            increaseAmount = 500.0,
            reason = "Necesito presupuesto adicional para casetas de peaje no contempladas en la ruta original",
            urgency = "low",
            requestedBy = "driver3@tickettrack.com",
            requestedByName = "Carlos Ramírez",
            status = "approved",
            reviewedBy = "admin@tickettrack.com",
            reviewedByName = "Admin Principal",
            reviewNote = "Aprobado. El desvío era necesario.",
            reviewedAt = Instant.now().minus(1, ChronoUnit.DAYS).toString(),
            createdAt = Instant.now().minus(3, ChronoUnit.DAYS).toString(),
            updatedAt = Instant.now().minus(1, ChronoUnit.DAYS).toString()
        )
    )

    /**
     * Crea una nueva solicitud de aumento de presupuesto
     * TODO: Implementar POST /api/budget-requests
     */
    suspend fun createBudgetRequest(request: BudgetRequestDTO): Result<BudgetRequest> {
        return try {
            delay(1000)

            // TODO: Reemplazar con llamada real
            // val response = httpClient.post("/api/budget-requests") {
            //     contentType(ContentType.Application.Json)
            //     setBody(request)
            // }

            val newRequest = BudgetRequestResponse(
                id = "req_${System.currentTimeMillis()}",
                tripId = request.tripId,
                tripNumber = "VJ-2025-XXX", // En real vendría del backend
                currentBudget = request.currentBudget,
                requestedBudget = request.requestedBudget,
                increaseAmount = request.requestedBudget - request.currentBudget,
                reason = request.reason,
                urgency = request.urgency,
                requestedBy = request.requestedBy,
                requestedByName = request.requestedByName,
                status = "pending",
                createdAt = Instant.now().toString(),
                updatedAt = Instant.now().toString()
            )

            mockRequests.add(newRequest)
            Result.success(newRequest.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene solicitudes pendientes para un consignatario
     * TODO: Implementar GET /api/budget-requests?status=pending&companyEmail={email}
     */
    suspend fun getPendingRequests(companyEmail: String): Result<List<BudgetRequest>> {
        return try {
            delay(800)

            // TODO: Reemplazar con llamada real
            // val response = httpClient.get("/api/budget-requests?status=pending&companyEmail=$companyEmail")

            val requests = mockRequests
                .filter { it.status == "pending" }
                .map { it.toDomain() }
                .sortedByDescending { it.createdAt }

            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las solicitudes de un usuario (transportista)
     * TODO: Implementar GET /api/budget-requests?userId={userId}
     */
    suspend fun getRequestsByUser(userId: String): Result<List<BudgetRequest>> {
        return try {
            delay(800)

            // TODO: Reemplazar con llamada real
            // val response = httpClient.get("/api/budget-requests?userId=$userId")

            val requests = mockRequests
                .filter { it.requestedBy == userId }
                .map { it.toDomain() }
                .sortedByDescending { it.createdAt }

            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aprueba una solicitud de presupuesto
     * TODO: Implementar PATCH /api/budget-requests/{requestId}/approve
     */
    suspend fun approveRequest(
        requestId: String,
        reviewNote: String,
        reviewedBy: String,
        reviewedByName: String
    ): Result<BudgetRequest> {
        return try {
            delay(1000)

            // TODO: Reemplazar con llamada real
            // val response = httpClient.patch("/api/budget-requests/$requestId/approve") {
            //     contentType(ContentType.Application.Json)
            //     setBody(ReviewBudgetRequestDTO(...))
            // }

            val requestIndex = mockRequests.indexOfFirst { it.id == requestId }
            if (requestIndex == -1) {
                return Result.failure(Exception("Solicitud no encontrada"))
            }

            val updatedRequest = mockRequests[requestIndex].copy(
                status = "approved",
                reviewedBy = reviewedBy,
                reviewedByName = reviewedByName,
                reviewNote = reviewNote,
                reviewedAt = Instant.now().toString(),
                updatedAt = Instant.now().toString()
            )

            mockRequests[requestIndex] = updatedRequest
            Result.success(updatedRequest.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Rechaza una solicitud de presupuesto
     * TODO: Implementar PATCH /api/budget-requests/{requestId}/reject
     */
    suspend fun rejectRequest(
        requestId: String,
        reviewNote: String,
        reviewedBy: String,
        reviewedByName: String
    ): Result<BudgetRequest> {
        return try {
            delay(1000)

            // TODO: Reemplazar con llamada real
            // val response = httpClient.patch("/api/budget-requests/$requestId/reject") {
            //     contentType(ContentType.Application.Json)
            //     setBody(ReviewBudgetRequestDTO(...))
            // }

            val requestIndex = mockRequests.indexOfFirst { it.id == requestId }
            if (requestIndex == -1) {
                return Result.failure(Exception("Solicitud no encontrada"))
            }

            val updatedRequest = mockRequests[requestIndex].copy(
                status = "rejected",
                reviewedBy = reviewedBy,
                reviewedByName = reviewedByName,
                reviewNote = reviewNote,
                reviewedAt = Instant.now().toString(),
                updatedAt = Instant.now().toString()
            )

            mockRequests[requestIndex] = updatedRequest
            Result.success(updatedRequest.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cuenta solicitudes pendientes para badge
     * TODO: Implementar GET /api/budget-requests/count?status=pending
     */
    suspend fun getPendingRequestCount(companyEmail: String): Result<Int> {
        return try {
            delay(300)

            // TODO: Reemplazar con llamada real
            // val response = httpClient.get("/api/budget-requests/count?status=pending&companyEmail=$companyEmail")

            val count = mockRequests.count { it.status == "pending" }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}