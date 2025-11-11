package com.tickettrack.app.data.remote.api

import com.tickettrack.app.domain.model.CreateTripRequest
import com.tickettrack.app.domain.model.TransportistaAvailable
import com.tickettrack.app.domain.model.Trip
import com.tickettrack.app.domain.model.UpdateTripStatusRequest
import com.tickettrack.app.domain.model.UpdateTripStatusResponse
import retrofit2.http.*

interface TripsApi {

    @GET("trips/api")
    suspend fun getTrips(
        @Header("Authorization") token: String
    ): List<Trip>

    // Nuevo endpoint para usuarios USER - viajes asignados
    @GET("trips/api/assigned")
    suspend fun getAssignedTrips(
        @Header("Authorization") token: String
    ): List<Trip>

    @GET("trips/api/transportistas")
    suspend fun getAvailableTransportistas(
        @Header("Authorization") token: String
    ): List<TransportistaAvailable>

    @POST("trips/api")
    suspend fun createTrip(
        @Header("Authorization") token: String,
        @Body request: CreateTripRequest
    ): Trip

    @GET("trips/api/{tripId}")
    suspend fun getTripById(
        @Header("Authorization") token: String,
        @Path("tripId") tripId: String
    ): Trip

    @PATCH("trips/api/{tripId}/status")
    suspend fun updateTripStatus(
        @Header("Authorization") token: String,
        @Path("tripId") tripId: String,
        @Body request: UpdateTripStatusRequest
    ): UpdateTripStatusResponse
}