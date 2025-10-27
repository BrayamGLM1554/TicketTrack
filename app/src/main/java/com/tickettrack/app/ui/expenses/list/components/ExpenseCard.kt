package com.tickettrack.app.ui.expenses.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tickettrack.app.domain.model.expense.Expense
import com.tickettrack.app.domain.model.expense.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card para mostrar un gasto individual
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCard(
    expense: Expense,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ícono de categoría
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(expense.category).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.category),
                    contentDescription = null,
                    tint = getCategoryColor(expense.category),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Información del gasto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Categoría
                Text(
                    text = expense.category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getCategoryColor(expense.category)
                )

                // Descripción
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2
                )

                // Fecha
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = dateFormatter.format(Date.from(expense.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Estado del ticket
                if (expense.hasApprovedTicket()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Ticket aprobado",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Monto y opciones
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Monto
                Text(
                    text = expense.getFormattedAmount(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5AC5C5)
                )

                // Botón eliminar (solo para admin)
                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Obtiene el ícono según la categoría
 */
private fun getCategoryIcon(category: ExpenseCategory): ImageVector {
    return when (category) {
        ExpenseCategory.FUEL -> Icons.Default.LocalGasStation
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TOLL -> Icons.Default.Toll
        ExpenseCategory.MAINTENANCE -> Icons.Default.Build
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz
    }
}

/**
 * Obtiene el color según la categoría
 */
private fun getCategoryColor(category: ExpenseCategory): Color {
    return when (category) {
        ExpenseCategory.FUEL -> Color(0xFFF44336)
        ExpenseCategory.FOOD -> Color(0xFFFF9800)
        ExpenseCategory.TOLL -> Color(0xFF2196F3)
        ExpenseCategory.MAINTENANCE -> Color(0xFF9C27B0)
        ExpenseCategory.OTHER -> Color(0xFF607D8B)
    }
}