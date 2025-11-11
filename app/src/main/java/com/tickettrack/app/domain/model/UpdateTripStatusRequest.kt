package com.tickettrack.app.domain.model

import com.google.gson.annotations.SerializedName

data class UpdateTripStatusRequest(
    @SerializedName("NewStatus") val newStatus: String
)