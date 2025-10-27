package com.tickettrack.app.data.model.trip

import com.tickettrack.app.domain.model.trip.TripStatus

/**
 * Representa un cambio de estado en el historial.
 */
data class StatusChange(
    val status: TripStatus = TripStatus.PENDING,
    val changedAt: String = "",               // ISO 8601
    val changedBy: String = "",               // UID
    val changedByName: String = ""
) {
    fun getStatusDisplayName(): String {
        return status.getDisplayName()
    }

    fun getStatusIcon(): String {
        return status.getIcon()
    }
}