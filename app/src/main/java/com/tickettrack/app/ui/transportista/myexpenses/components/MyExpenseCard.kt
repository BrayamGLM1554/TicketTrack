package com.tickettrack.app.ui.transportista.myexpenses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.domain.model.expense.Expense
import com.tickettrack.app.domain.model.expense.ExpenseCategory
import com.tickettrack.app.domain.model.expense.ModerationStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Card individual para mostrar un gasto en la lista.
 *
 * Muestra:
 * - Icono de categoría con color
 * - Descripción del gasto
 * - Categoría
 * - Fecha
 * - Monto
 * - Estado de moderación
 * - Viaje asociado
 */
@Composable
fun MyExpenseCard(expense: Expense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de categoría
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(expense.category.getColor()).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = expense.category.getIcon(),
                    contentDescription = null,
                    tint = Color(expense.category.getColor()),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del gasto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Categoría
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(expense.category.getColor()).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = expense.category.displayName,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = Color(expense.category.getColor()),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Badge de moderación
                    ModerationBadge(status = expense.ticketModerationStatus)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Fecha
                Text(
                    text = formatDate(expense.date.toString()),
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // ID del viaje (pequeño)
                Text(
                    text = "Viaje: ${expense.tripId}",
                    fontSize = 10.sp,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Monto
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${String.format("%.2f", expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF5AC5C5)
                )
                Text(
                    text = expense.currency,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ModerationBadge(status: ModerationStatus) {
    val (icon, color, text) = when (status) {
        ModerationStatus.APPROVED -> Triple(
            "✓",
            Color(0xFF4CAF50),
            "Aprobado"
        )
        ModerationStatus.PENDING -> Triple(
            "⏳",
            Color(0xFFFF9800),
            "Pendiente"
        )
        ModerationStatus.REJECTED -> Triple(
            "✗",
            Color(0xFFF44336),
            "Rechazado"
        )
        ModerationStatus.FLAGGED -> Triple(
            "⚠",
            Color(0xFFF44336),
            "Alerta"
        )
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==========================================
// UTILIDADES
// ==========================================

private fun formatDate(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val formatter = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        isoDate
    }
}

// Extensiones para íconos y colores
private fun ExpenseCategory.getIcon(): ImageVector {
    return when (this) {
        ExpenseCategory.FUEL -> Icons.Default.LocalGasStation
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TOLL -> Icons.Default.Toll
        ExpenseCategory.MAINTENANCE -> Icons.Default.Build
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
    }
}

private fun ExpenseCategory.getColor(): Long {
    return when (this) {
        ExpenseCategory.FUEL -> 0xFF2196F3
        ExpenseCategory.FOOD -> 0xFFFF9800
        ExpenseCategory.TOLL -> 0xFF9C27B0
        ExpenseCategory.MAINTENANCE -> 0xFFF44336
        ExpenseCategory.OTHER -> 0xFF607D8B
    }
}