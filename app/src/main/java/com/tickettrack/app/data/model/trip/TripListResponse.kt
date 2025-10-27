package com.tickettrack.app.data.model.trip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de respuesta para una lista de viajes.
 *
 * Se usa cuando el backend retorna múltiples viajes.
 * Por ejemplo: GET /trips?adminId=xxx
 */
@Serializable
data class TripListResponse(
    @SerialName("trips")
    val trips: List<TripResponse> = emptyList(),

    @SerialName("total")
    val total: Int = 0,

    @SerialName("page")
    val page: Int = 1,

    @SerialName("pageSize")
    val pageSize: Int = 20
)