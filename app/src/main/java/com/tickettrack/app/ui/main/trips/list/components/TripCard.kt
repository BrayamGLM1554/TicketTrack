package com.tickettrack.app.ui.main.trips.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.TripStatus

/**
 * Card reutilizable para mostrar un viaje en la lista.
 *
 * Siguiendo principios de Compose:
 * - Componente sin estado (stateless)
 * - Reutilizable
 * - Recibe datos y callbacks como parámetros
 */
@Composable
fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Nombre del viaje + Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.cargoName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Badge de estado
                StatusBadge(status = trip.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información del viaje
            TripInfoRow(label = "Origen", value = trip.origin.getShortAddress())

            Spacer(modifier = Modifier.height(4.dp))

            TripInfoRow(label = "Destino", value = trip.destination.getShortAddress())

            Spacer(modifier = Modifier.height(4.dp))

            TripInfoRow(label = "Presupuesto", value = trip.budget.getCurrentFormatted())

            Spacer(modifier = Modifier.height(4.dp))

            TripInfoRow(
                label = "Usado",
                value = trip.getTotalExpensesFormatted(),
                valueColor = if (trip.isOverBudget()) Color(0xFFF44336) else Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Ver detalles
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF5AC5C5)
                )
            ) {
                Text(
                    text = "Ver detalles",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Badge para mostrar el estado del viaje.
 */
@Composable
private fun StatusBadge(
    status: TripStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        TripStatus.PENDING -> Color(0xFFFFF3E0)
        TripStatus.IN_PROGRESS -> Color(0xFFE3F2FD)
        TripStatus.COMPLETED -> Color(0xFFE8F5E9)
        TripStatus.CANCELLED -> Color(0xFFFFEBEE)
    }

    val textColor = when (status) {
        TripStatus.PENDING -> Color(0xFFF57C00)
        TripStatus.IN_PROGRESS -> Color(0xFF1976D2)
        TripStatus.COMPLETED -> Color(0xFF388E3C)
        TripStatus.CANCELLED -> Color(0xFFD32F2F)
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status.getIcon(),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = status.getDisplayName(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Fila de información del viaje.
 */
@Composable
private fun TripInfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontSize = 14.sp
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            fontSize = 14.sp
        )
    }
}