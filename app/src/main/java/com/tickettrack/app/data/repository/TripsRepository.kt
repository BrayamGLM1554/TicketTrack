package com.tickettrack.app.data.repository

import com.tickettrack.app.data.remote.api.TripsApi
import com.tickettrack.app.domain.model.CreateTripRequest
import com.tickettrack.app.domain.model.TransportistaAvailable
import com.tickettrack.app.domain.model.Trip
import com.tickettrack.app.domain.model.TripStatus
import com.tickettrack.app.domain.model.UpdateTripStatusRequest
import com.tickettrack.app.domain.repository.ITripsRepository

class TripsRepository(
    private val tripsApi: TripsApi
) : ITripsRepository {

    override suspend fun getTrips(token: String): Result<List<Trip>> {
        return try {
            val response = tripsApi.getTrips("Bearer $token")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getAssignedTrips(token: String): Result<List<Trip>> {
        return try {
            val trips = tripsApi.getAssignedTrips("Bearer $token")
            Result.success(trips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAvailableTransportistas(
        token: String
    ): Result<List<TransportistaAvailable>> {
        return try {
            val response = tripsApi.getAvailableTransportistas("Bearer $token")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTrip(
        token: String,
        request: CreateTripRequest
    ): Result<Trip> {
        return try {
            val response = tripsApi.createTrip("Bearer $token", request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTripById(
        token: String,
        tripId: String
    ): Result<Trip> {
        return try {
            val response = tripsApi.getTripById("Bearer $token", tripId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTripStatus(
        token: String,
        tripId: String,
        newStatus: TripStatus
    ): Result<Trip> {
        return try {
            val request = UpdateTripStatusRequest(newStatus = newStatus.name)
            // El PATCH solo devuelve confirmación, no el Trip completo
            tripsApi.updateTripStatus("Bearer $token", tripId, request)

            // Después del update exitoso, obtenemos el Trip actualizado
            val updatedTrip = tripsApi.getTripById("Bearer $token", tripId)
            Result.success(updatedTrip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}