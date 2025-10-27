package com.tickettrack.app.data.model.trip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de respuesta para datos de un transportista.
 *
 * Se usa en:
 * - Dropdown de selección al crear viaje
 * - Detalle del viaje (sección transportista asignado)
 */
@Serializable
data class DriverResponse(
    @SerialName("id")
    val id: String = "",

    @SerialName("name")
    val name: String = "",

    @SerialName("photoUrl")
    val photoUrl: String? = null,

    @SerialName("completedTrips")
    val completedTrips: Int = 0,

    @SerialName("assignedUnit")
    val assignedUnit: String = "",        // TG-0347

    @SerialName("plates")
    val plates: String = "",              // NL-TB-4782

    @SerialName("brand")
    val brand: String = "",               // Kenworth

    @SerialName("model")
    val model: String = "",               // T680

    @SerialName("truckPhotoUrl")
    val truckPhotoUrl: String? = null,

    @SerialName("isAvailable")
    val isAvailable: Boolean = true
)

/**
 * Lista de transportistas disponibles.
 */
@Serializable
data class DriverListResponse(
    @SerialName("drivers")
    val drivers: List<DriverResponse> = emptyList()
)