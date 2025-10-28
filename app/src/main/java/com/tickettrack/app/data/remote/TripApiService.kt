package com.tickettrack.app.data.remote

import com.tickettrack.app.data.model.trip.TransportistaResponse
import com.tickettrack.app.data.model.trip.TripRequest
import com.tickettrack.app.data.model.trip.TripResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz que define los endpoints de la API de Viajes.
 * Retrofit implementa automáticamente esta interfaz.
 */
interface TripApiService {

    /**
     * Obtiene lista de transportistas disponibles.
     * GET /trips/api/transportistas
     */
    @GET("trips/api/transportistas")
    suspend fun getTransportistas(): Response<List<TransportistaResponse>>

    /**
     * Crea un nuevo viaje.
     * POST /trips/api
     */
    @POST("trips/api")
    suspend fun createTrip(@Body request: TripRequest): Response<TripResponse>

    /**
     * Obtiene un viaje por su ID.
     * GET /trips/api/{id}
     */
    @GET("trips/api/{id}")
    suspend fun getTripById(@Path("id") id: String): Response<TripResponse>

    /**
     * Lista todos los viajes de la compañía.
     * GET /trips/api
     */
    @GET("trips/api")
    suspend fun getAllTrips(): Response<List<TripResponse>>
}