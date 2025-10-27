package com.tickettrack.app.ui.expenses.budget.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tickettrack.app.domain.model.expense.BudgetRequest
import com.tickettrack.app.domain.model.expense.BudgetUrgency
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card expandible para mostrar solicitud de presupuesto
 * HU20 - Cards con información compacta y expandible
 */
@Composable
fun BudgetRequestCard(
    request: BudgetRequest,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    canReview: Boolean,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val urgencyColor = getUrgencyColor(request.urgency)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                request.status.name == "APPROVED" -> Color(0xFFE8F5E9)
                request.status.name == "REJECTED" -> Color(0xFFFFEBEE)
                else -> Color.White
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vista compacta (siempre visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Número de viaje y urgencia
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge de urgencia
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(urgencyColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = request.urgency.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = urgencyColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Número de viaje
                        Text(
                            text = request.tripNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Transportista
                    Text(
                        text = request.requestedByName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    // Fecha
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = dateFormatter.format(Date.from(request.createdAt)),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Monto solicitado
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = request.getFormattedIncrease(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor
                    )
                    Text(
                        text = request.getFormattedPercentage(),
                        style = MaterialTheme.typography.bodySmall,
                        color = urgencyColor
                    )
                }
            }

            // Indicador de estado
            when {
                request.status.name == "APPROVED" -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Aprobada",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                request.status.name == "REJECTED" -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Rechazada",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Vista expandida (detalles)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider()

                    // Presupuesto actual vs propuesto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Presupuesto Actual",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "$${"%.2f".format(request.currentBudget)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Presupuesto Propuesto",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "$${"%.2f".format(request.requestedBudget)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5AC5C5)
                            )
                        }
                    }

                    // Justificación
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Justificación",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = request.reason,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Nota de revisión (si existe)
                    if (!request.reviewNote.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (request.status.name == "APPROVED") {
                                    Color(0xFFE8F5E9)
                                } else {
                                    Color(0xFFFFEBEE)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Comentario de revisión",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Text(
                                    text = request.reviewNote,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (request.reviewedByName != null) {
                                    Text(
                                        text = "Por: ${request.reviewedByName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Botones de acción (solo si está pendiente y el usuario puede revisar)
                    if (request.isPending() && canReview) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rechazar")
                            }

                            Button(
                                onClick = onApprove,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aprobar")
                            }
                        }
                    }
                }
            }

            // Indicador de expandir/colapsar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = Color.Gray
                )
            }
        }
    }
}

/**
 * Obtiene el color según la urgencia
 */
private fun getUrgencyColor(urgency: BudgetUrgency): Color {
    return when (urgency) {
        BudgetUrgency.LOW -> Color(0xFF4CAF50)
        BudgetUrgency.MEDIUM -> Color(0xFFFF9800)
        BudgetUrgency.HIGH -> Color(0xFFFF5722)
        BudgetUrgency.CRITICAL -> Color(0xFFF44336)
    }
}