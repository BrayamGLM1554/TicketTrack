package com.tickettrack.app.domain.model

import com.google.gson.annotations.SerializedName

enum class ExpenseCategory {
    @SerializedName("Fuel") FUEL,
    @SerializedName("Food") FOOD,
    @SerializedName("Toll") TOLL,
    @SerializedName("Maintenance") MAINTENANCE,
    @SerializedName("Parking") PARKING,
    @SerializedName("Other") OTHER
}