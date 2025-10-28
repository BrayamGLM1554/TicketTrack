package com.tickettrack.app.domain.model.trip

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Representa un cambio de estado en el historial.
 */
data class StatusChange(
    val status: TripStatus = TripStatus.PENDING,
    val changedAt: String = "",               // ISO 8601
    val changedBy: String = "",               // UID
    val changedByName: String = ""
) {
    fun getFormattedDate(): String {
        return try {
            val instant = Instant.parse(changedAt)
            val formatter = DateTimeFormatter
                .ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (e: Exception) {
            changedAt
        }
    }
}