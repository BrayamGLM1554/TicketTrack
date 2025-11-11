package com.tickettrack.app.domain.repository

import com.tickettrack.app.domain.model.CreateTripRequest
import com.tickettrack.app.domain.model.TransportistaAvailable
import com.tickettrack.app.domain.model.Trip
import com.tickettrack.app.domain.model.TripStatus

interface ITripsRepository {
    suspend fun getTrips(token: String): Result<List<Trip>>
    suspend fun getAssignedTrips(token: String): Result<List<Trip>>
    suspend fun getAvailableTransportistas(token: String): Result<List<TransportistaAvailable>>

    suspend fun createTrip(token: String, request: CreateTripRequest): Result<Trip>

    suspend fun getTripById(token: String, tripId: String): Result<Trip>

    suspend fun updateTripStatus(
        token: String,
        tripId: String,
        newStatus: TripStatus
    ): Result<Trip>
}