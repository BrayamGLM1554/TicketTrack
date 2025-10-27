package com.tickettrack.app.data.repository.trip

import android.util.Log
import com.tickettrack.app.data.model.trip.*
import com.tickettrack.app.domain.model.trip.*
import kotlinx.coroutines.delay
import java.time.Instant

/**
 * Repositorio para operaciones CRUD de viajes.
 *
 * Siguiendo Clean Architecture y SOLID:
 * - Single Responsibility: Solo maneja acceso a datos de viajes
 * - Dependency Inversion: Puede ser reemplazado por una interfaz
 * - Preparado para integración con Firebase/API Gateway
 *
 * Estado actual: Simulación con datos de prueba
 * TODO: Implementar llamadas reales a Firebase cuando el microservicio esté listo
 */
class TripRepository {

    companion object {
        private const val TAG = "TripRepository"

        // Simulación de datos para desarrollo
        private val mockTrips = mutableListOf<TripResponse>()
    }

    /**
     * Crea un nuevo viaje.
     *
     * @param request Datos del viaje a crear
     * @return Viaje creado con ID generado
     */
    suspend fun createTrip(request: TripRequest): Result<TripResponse> {
        return try {
            Log.d(TAG, "Creating trip: ${request.cargoName}")

            // Simular delay de red
            delay(1500)

            // TODO: Reemplazar con llamada real a Firebase
            // val tripRef = firestore.collection("trips").document()
            // tripRef.set(request).await()

            // Generar ID simulado
            val tripId = "trip_${System.currentTimeMillis()}"
            val now = Instant.now().toString()

            // Crear respuesta simulada
            val response = TripResponse(
                id = tripId,
                cargoName = request.cargoName,
                origin = request.origin,
                destination = request.destination,
                cargo = request.cargo,
                budget = request.budget,
                assignedDriverId = request.assignedDriverId,
                createdByAdminId = request.createdByAdminId,
                status = "pending",
                statusHistory = listOf(
                    StatusChangeData(
                        status = "pending",
                        changedAt = now,
                        changedBy = request.createdByAdminId,
                        changedByName = "Admin"
                    )
                ),
                totalExpenses = 0.0,
                remainingBudget = request.budget.initial,
                expenseCount = 0,
                budgetIncreaseCount = 0,
                createdAt = now,
                updatedAt = now,
                completedAt = null
            )

            // Guardar en lista simulada
            mockTrips.add(response)

            Log.d(TAG, "Trip created successfully: $tripId")
            Result.success(response)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating trip", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los viajes de un admin.
     *
     * @param adminId UID del admin
     * @return Lista de viajes
     */
    suspend fun getTripsByAdmin(adminId: String): Result<List<TripResponse>> {
        return try {
            Log.d(TAG, "Fetching trips for admin: $adminId")

            // Simular delay de red
            delay(1000)

            // TODO: Reemplazar con llamada real a Firebase
            // val snapshot = firestore.collection("trips")
            //     .whereEqualTo("createdByAdminId", adminId)
            //     .get()
            //     .await()

            // Filtrar viajes del admin
            val trips = mockTrips.filter { it.createdByAdminId == adminId }

            Log.d(TAG, "Found ${trips.size} trips")
            Result.success(trips)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trips", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los viajes asignados a un transportista.
     *
     * @param driverId UID del transportista
     * @return Lista de viajes asignados
     */
    suspend fun getTripsByDriver(driverId: String): Result<List<TripResponse>> {
        return try {
            Log.d(TAG, "Fetching trips for driver: $driverId")

            // Simular delay de red
            delay(1000)

            // TODO: Reemplazar con llamada real a Firebase
            // val snapshot = firestore.collection("trips")
            //     .whereEqualTo("assignedDriverId", driverId)
            //     .get()
            //     .await()

            // Filtrar viajes del transportista
            val trips = mockTrips.filter { it.assignedDriverId == driverId }

            Log.d(TAG, "Found ${trips.size} trips for driver")
            Result.success(trips)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching driver trips", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene un viaje por su ID.
     *
     * @param tripId ID del viaje
     * @return Viaje encontrado o null
     */
    suspend fun getTripById(tripId: String): Result<TripResponse?> {
        return try {
            Log.d(TAG, "Fetching trip: $tripId")

            // Simular delay de red
            delay(500)

            // TODO: Reemplazar con llamada real a Firebase
            // val doc = firestore.collection("trips").document(tripId).get().await()
            // val trip = doc.toObject(TripResponse::class.java)

            val trip = mockTrips.find { it.id == tripId }

            if (trip != null) {
                Log.d(TAG, "Trip found: ${trip.cargoName}")
            } else {
                Log.d(TAG, "Trip not found")
            }

            Result.success(trip)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trip", e)
            Result.failure(e)
        }
    }

    /**
     * Aumenta el presupuesto de un viaje.
     *
     * @param request Datos del aumento
     * @return Viaje actualizado
     */
    suspend fun increaseBudget(request: IncreaseBudgetRequest): Result<TripResponse> {
        return try {
            Log.d(TAG, "Increasing budget for trip: ${request.tripId}")

            // Simular delay de red
            delay(1000)

            // TODO: Reemplazar con llamada real a Firebase
            // val tripRef = firestore.collection("trips").document(request.tripId)
            // Actualizar budget.current, budget.history, budgetIncreaseCount, remainingBudget

            // Buscar viaje en lista simulada
            val tripIndex = mockTrips.indexOfFirst { it.id == request.tripId }
            if (tripIndex == -1) {
                throw Exception("Viaje no encontrado")
            }

            val trip = mockTrips[tripIndex]
            val now = Instant.now().toString()

            // Crear nuevo entry en historial
            val newHistoryEntry = BudgetIncreaseData(
                previousAmount = request.previousAmount,
                newAmount = request.newAmount,
                increase = request.increase,
                reason = request.reason,
                requestedBy = request.requestedBy,
                requestedByName = request.requestedByName,
                approvedBy = request.approvedBy,
                approvedByName = request.approvedByName,
                requestedAt = now,
                approvedAt = now,
                urgency = request.urgency,
                supportingExpenses = request.supportingExpenses
            )

            // Actualizar presupuesto
            val updatedBudget = trip.budget.copy(
                current = request.newAmount,
                history = trip.budget.history + newHistoryEntry
            )

            // Actualizar viaje
            val updatedTrip = trip.copy(
                budget = updatedBudget,
                remainingBudget = request.newAmount - trip.totalExpenses,
                budgetIncreaseCount = trip.budgetIncreaseCount + 1,
                updatedAt = now
            )

            // Actualizar en lista simulada
            mockTrips[tripIndex] = updatedTrip

            Log.d(TAG, "Budget increased successfully")
            Result.success(updatedTrip)

        } catch (e: Exception) {
            Log.e(TAG, "Error increasing budget", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el estado de un viaje.
     *
     * @param request Datos del cambio de estado
     * @return Viaje actualizado
     */
    suspend fun updateTripStatus(request: UpdateTripStatusRequest): Result<TripResponse> {
        return try {
            Log.d(TAG, "Updating trip status: ${request.tripId} -> ${request.newStatus}")

            // Simular delay de red
            delay(1000)

            // TODO: Reemplazar con llamada real a Firebase
            // Actualizar status, statusHistory, y si es completed, registrar completedAt

            val tripIndex = mockTrips.indexOfFirst { it.id == request.tripId }
            if (tripIndex == -1) {
                throw Exception("Viaje no encontrado")
            }

            val trip = mockTrips[tripIndex]
            val now = Instant.now().toString()

            // Crear nuevo entry en historial de estados
            val newStatusEntry = StatusChangeData(
                status = request.newStatus,
                changedAt = now,
                changedBy = request.changedBy,
                changedByName = request.changedByName
            )

            // Actualizar viaje
            val updatedTrip = trip.copy(
                status = request.newStatus,
                statusHistory = trip.statusHistory + newStatusEntry,
                updatedAt = now,
                completedAt = if (request.newStatus == "completed") now else trip.completedAt
            )

            // Actualizar en lista simulada
            mockTrips[tripIndex] = updatedTrip

            Log.d(TAG, "Trip status updated successfully")
            Result.success(updatedTrip)

        } catch (e: Exception) {
            Log.e(TAG, "Error updating trip status", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un viaje.
     * Solo para desarrollo/testing.
     */
    suspend fun deleteTrip(tripId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting trip: $tripId")

            delay(500)

            // TODO: Implementar en Firebase si es necesario
            mockTrips.removeIf { it.id == tripId }

            Log.d(TAG, "Trip deleted successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting trip", e)
            Result.failure(e)
        }
    }

    /**
     * Limpia todos los datos simulados.
     * Solo para testing.
     */
    fun clearMockData() {
        mockTrips.clear()
        Log.d(TAG, "Mock data cleared")
    }
}