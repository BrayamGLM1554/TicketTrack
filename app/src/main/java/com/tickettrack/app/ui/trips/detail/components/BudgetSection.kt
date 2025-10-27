package com.tickettrack.app.ui.trips.detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.data.model.trip.Budget
import com.tickettrack.app.data.model.trip.BudgetIncrease

/**
 * Sección de presupuesto con historial expandible.
 */
@Composable
fun BudgetSection(
    budget: Budget,
    totalExpenses: Double,
    remainingBudget: Double,
    isHistoryExpanded: Boolean,
    canIncreaseBudget: Boolean,
    onToggleHistory: () -> Unit,
    onIncreaseBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Presupuesto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (canIncreaseBudget) {
                    IconButton(
                        onClick = onIncreaseBudget,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar Presupuesto",
                            tint = Color(0xFF5AC5C5)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información del presupuesto
            BudgetRow("Inicial", budget.getInitialFormatted())
            Spacer(modifier = Modifier.height(8.dp))
            BudgetRow("Actual", budget.getCurrentFormatted(), Color(0xFF5AC5C5))
            Spacer(modifier = Modifier.height(8.dp))
            BudgetRow("Gastado", "$%.2f ${budget.currency}".format(totalExpenses))
            Spacer(modifier = Modifier.height(8.dp))
            BudgetRow(
                "Restante",
                "$%.2f ${budget.currency}".format(remainingBudget),
                if (remainingBudget < 0) Color(0xFFF44336) else Color(0xFF4CAF50)
            )

            // Aumentos
            if (budget.hasIncreases()) {
                Spacer(modifier = Modifier.height(8.dp))
                BudgetRow("Aumentos", "${budget.getIncreaseCount()}", Color(0xFFFF9800))
            }

            // Historial expandible
            if (budget.hasIncreases()) {
                Spacer(modifier = Modifier.height(12.dp))

                Divider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleHistory)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial de Aumentos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5AC5C5)
                    )

                    Icon(
                        imageVector = if (isHistoryExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isHistoryExpanded) "Ocultar" else "Mostrar",
                        tint = Color(0xFF5AC5C5)
                    )
                }

                AnimatedVisibility(visible = isHistoryExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        budget.history.forEach { increase ->
                            BudgetIncreaseCard(increase = increase)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun BudgetIncreaseCard(increase: BudgetIncrease) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header con aumento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = increase.getIncreaseFormatted(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )

                // Badge de urgencia
                Surface(
                    color = Color(increase.urgency.getColor()),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = increase.urgency.getDisplayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Motivo
            Text(
                text = increase.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Detalles
            Text(
                text = "Anterior: $%.2f → Nuevo: $%.2f".format(
                    increase.previousAmount,
                    increase.newAmount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Text(
                text = "Solicitado por: ${increase.requestedByName}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}