package com.tickettrack.app.domain.model

import com.google.gson.annotations.SerializedName

// Modelo para la respuesta del PATCH status
data class UpdateTripStatusResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("previousStatus")
    val previousStatus: String,

    @SerializedName("newStatus")
    val newStatus: String,

    @SerializedName("message")
    val message: String
)