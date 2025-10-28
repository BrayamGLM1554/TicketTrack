package com.tickettrack.app.data.model.trip

import com.google.gson.annotations.SerializedName

/**
 * Response de transportista desde la API.
 */
data class TransportistaResponse(
    @SerializedName("uid")
    val uid: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("telefono")
    val telefono: String? = null,

    @SerializedName("estado")
    val estado: String
)