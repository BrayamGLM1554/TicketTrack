package com.tickettrack.app.ui.transportista.mytrips.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.TripStatus

/**
 * Card individual para mostrar un viaje en la lista.
 *
 * Muestra:
 * - Nombre del viaje
 * - Origen → Destino
 * - Estado actual
 * - Presupuesto y gastos
 * - Badges de alerta
 */
@Composable
fun MyTripCard(
    trip: Trip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Nombre y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.cargoName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatusBadge(status = trip.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ID del viaje
            Text(
                text = "ID: ${trip.id}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ruta: Origen → Destino
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trip.origin.city,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trip.destination.city,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(12.dp))

            // Información de presupuesto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetInfo(
                    label = "Presupuesto",
                    value = "$${String.format("%.0f", trip.budget.current)}",
                    color = Color(0xFF5AC5C5)
                )
                BudgetInfo(
                    label = "Gastado",
                    value = "$${String.format("%.0f", trip.totalExpenses)}",
                    color = Color(0xFFF44336)
                )
                BudgetInfo(
                    label = "Disponible",
                    value = "$${String.format("%.0f", trip.remainingBudget)}",
                    color = Color(0xFF4CAF50)
                )
            }

            // Barra de progreso del presupuesto
            if (trip.budget.current > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                val usagePercentage = (trip.totalExpenses / trip.budget.current).toFloat()

                LinearProgressIndicator(
                    progress = usagePercentage.coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = when {
                        usagePercentage >= 0.9f -> Color(0xFFF44336)
                        usagePercentage >= 0.7f -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    },
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }

            // Alertas y badges adicionales
            if (trip.getBudgetUsagePercentage() >= 90f || trip.budgetIncreaseCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (trip.getBudgetUsagePercentage() >= 90f) {
                        AlertChip(
                            text = "⚠️ Presupuesto bajo",
                            backgroundColor = Color(0xFFFFF3CD)
                        )
                    }

                    if (trip.budgetIncreaseCount > 0) {
                        AlertChip(
                            text = "✓ Presupuesto aumentado",
                            backgroundColor = Color(0xFFD1ECF1)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TripStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TripStatus.PENDING -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.2f),
            Color(0xFFFF9800),
            "Pendiente"
        )
        TripStatus.IN_PROGRESS -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.2f),
            Color(0xFF2196F3),
            "En Viaje"
        )
        TripStatus.COMPLETED -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.2f),
            Color(0xFF4CAF50),
            "Completado"
        )
        TripStatus.CANCELLED -> Triple(
            Color(0xFFF44336).copy(alpha = 0.2f),
            Color(0xFFF44336),
            "Cancelado"
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun BudgetInfo(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun AlertChip(
    text: String,
    backgroundColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}