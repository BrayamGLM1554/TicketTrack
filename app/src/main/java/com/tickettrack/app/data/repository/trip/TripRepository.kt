package com.tickettrack.app.data.repository.trip

import android.util.Log
import com.tickettrack.app.data.model.trip.*
import com.tickettrack.app.data.remote.TripApiService
import com.tickettrack.app.domain.model.trip.*
import kotlinx.coroutines.delay

/**
 * Repositorio para operaciones CRUD de viajes.
 *
 * Estrategia híbrida:
 * - Métodos con API disponible: Usan Retrofit + convierten a Domain
 * - Métodos sin API: Mock temporal con cache local
 *
 * @param apiService Servicio de API para llamadas HTTP
 */
class TripRepository(
    private val apiService: TripApiService
) {

    companion object {
        private const val TAG = "TripRepository"

        // Cache local para funcionalidades sin API
        private val tripCache = mutableMapOf<String, Trip>()
    }

    // ==========================================
    // MÉTODOS CON API REAL
    // ==========================================

    /**
     * Crea un nuevo viaje usando la API real.
     * Convierte: Domain → API Request → API Response → Domain
     */
    suspend fun createTrip(trip: Trip): Result<Trip> {
        return try {
            Log.d(TAG, "Creating trip via API: ${trip.cargoName}")

            // 1. Convertir Domain → API Request
            val apiRequest = trip.toApiRequest()

            // 2. Llamar a la API
            val response = apiService.createTrip(apiRequest)

            if (response.isSuccessful && response.body() != null) {
                // 3. Convertir API Response → Domain
                val tripDomain = response.body()!!.toDomain()

                // 4. Guardar en cache local
                tripCache[tripDomain.id] = tripDomain

                Log.d(TAG, "Trip created successfully: ${tripDomain.id}")
                Result.success(tripDomain)
            } else {
                val error = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error creating trip", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los viajes de la compañía usando la API.
     */
    suspend fun getTripsByAdmin(adminId: String): Result<List<Trip>> {
        return try {
            Log.d(TAG, "Fetching trips from API")

            val response = apiService.getAllTrips()

            if (response.isSuccessful && response.body() != null) {
                // Convertir lista de API → Domain
                val trips = response.body()!!.toDomain()

                // Actualizar cache
                trips.forEach { tripCache[it.id] = it }

                Log.d(TAG, "Found ${trips.size} trips from API")
                Result.success(trips)
            } else {
                val error = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trips from API", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene un viaje por su ID usando la API.
     */
    suspend fun getTripById(tripId: String): Result<Trip?> {
        return try {
            Log.d(TAG, "Fetching trip from API: $tripId")

            val response = apiService.getTripById(tripId)

            if (response.isSuccessful && response.body() != null) {
                val trip = response.body()!!.toDomain()
                tripCache[trip.id] = trip

                Log.d(TAG, "Trip found via API: ${trip.cargoName}")
                Result.success(trip)
            } else if (response.code() == 404) {
                Log.d(TAG, "Trip not found: $tripId")
                Result.success(null)
            } else {
                val error = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trip from API", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene lista de transportistas disponibles desde la API.
     */
    suspend fun getTransportistas(): Result<List<TransportistaResponse>> {
        return try {
            Log.d(TAG, "Fetching transportistas from API")

            val response = apiService.getTransportistas()

            if (response.isSuccessful && response.body() != null) {
                val transportistas = response.body()!!
                Log.d(TAG, "Found ${transportistas.size} transportistas")
                Result.success(transportistas)
            } else {
                val error = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching transportistas", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // MÉTODOS SIN API (Mock Temporal)
    // ==========================================

    /**
     * Obtiene viajes asignados a un transportista.
     * ⚠️ MOCK: Filtra del cache local.
     * TODO: Implementar cuando exista endpoint GET /trips/api/driver/{driverId}
     */
    suspend fun getTripsByDriver(driverId: String): Result<List<Trip>> {
        return try {
            Log.d(TAG, "[MOCK] Fetching trips for driver: $driverId")
            delay(500)

            val trips = tripCache.values.filter { it.assignedDriverId == driverId }

            Log.d(TAG, "[MOCK] Found ${trips.size} trips for driver")
            Result.success(trips)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching driver trips", e)
            Result.failure(e)
        }
    }

    /**
     * Aumenta el presupuesto de un viaje.
     * ⚠️ MOCK: Actualiza solo en cache local.
     * TODO: Implementar cuando exista endpoint PUT /trips/api/{id}/budget
     */
    suspend fun increaseBudget(
        tripId: String,
        newAmount: Double,
        reason: String,
        urgency: Urgency,
        requestedBy: String,
        requestedByName: String
    ): Result<Trip> {
        return try {
            Log.d(TAG, "[MOCK] Increasing budget for trip: $tripId")
            delay(1000)

            val trip = tripCache[tripId]
                ?: return Result.failure(Exception("Viaje no encontrado"))

            val now = java.time.Instant.now().toString()

            // Crear nueva entrada en historial
            val budgetIncrease = BudgetIncrease(
                previousAmount = trip.budget.current,
                newAmount = newAmount,
                increase = newAmount - trip.budget.current,
                reason = reason,
                requestedBy = requestedBy,
                requestedByName = requestedByName,
                approvedBy = requestedBy,
                approvedByName = requestedByName,
                requestedAt = now,
                approvedAt = now,
                urgency = urgency,
                supportingExpenses = emptyList()
            )

            // Actualizar presupuesto
            val updatedBudget = trip.budget.copy(
                current = newAmount,
                history = trip.budget.history + budgetIncrease
            )

            // Actualizar viaje
            val updatedTrip = trip.copy(
                budget = updatedBudget,
                remainingBudget = newAmount - trip.totalExpenses,
                budgetIncreaseCount = trip.budgetIncreaseCount + 1,
                updatedAt = now
            )

            tripCache[tripId] = updatedTrip

            Log.d(TAG, "[MOCK] Budget increased successfully")
            Result.success(updatedTrip)

        } catch (e: Exception) {
            Log.e(TAG, "Error increasing budget", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el estado de un viaje.
     * ⚠️ MOCK: Actualiza solo en cache local.
     * TODO: Implementar cuando exista endpoint PUT /trips/api/{id}/status
     */
    suspend fun updateTripStatus(
        tripId: String,
        newStatus: TripStatus,
        changedBy: String,
        changedByName: String
    ): Result<Trip> {
        return try {
            Log.d(TAG, "[MOCK] Updating trip status: $tripId -> $newStatus")
            delay(1000)

            val trip = tripCache[tripId]
                ?: return Result.failure(Exception("Viaje no encontrado"))

            val now = java.time.Instant.now().toString()

            val statusChange = StatusChange(
                status = newStatus,
                changedAt = now,
                changedBy = changedBy,
                changedByName = changedByName
            )

            val updatedTrip = trip.copy(
                status = newStatus,
                statusHistory = trip.statusHistory + statusChange,
                updatedAt = now,
                completedAt = if (newStatus == TripStatus.COMPLETED) now else trip.completedAt
            )

            tripCache[tripId] = updatedTrip

            Log.d(TAG, "[MOCK] Trip status updated successfully")
            Result.success(updatedTrip)

        } catch (e: Exception) {
            Log.e(TAG, "Error updating trip status", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un viaje del cache.
     * ⚠️ MOCK: Solo local.
     * TODO: Implementar endpoint DELETE /trips/api/{id} si es necesario
     */
    suspend fun deleteTrip(tripId: String): Result<Unit> {
        return try {
            Log.d(TAG, "[MOCK] Deleting trip: $tripId")
            delay(500)

            tripCache.remove(tripId)

            Log.d(TAG, "[MOCK] Trip deleted")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting trip", e)
            Result.failure(e)
        }
    }

    /**
     * Limpia el cache local.
     * Solo para testing.
     */
    fun clearCache() {
        tripCache.clear()
        Log.d(TAG, "Cache cleared")
    }
}