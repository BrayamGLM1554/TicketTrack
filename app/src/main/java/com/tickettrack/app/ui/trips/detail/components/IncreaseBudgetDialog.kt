package com.tickettrack.app.ui.trips.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tickettrack.app.data.model.trip.Urgency

/**
 * Diálogo para solicitar aumento de presupuesto.
 */
@Composable
fun IncreaseBudgetDialog(
    currentBudget: Double,
    newAmount: String,
    newAmountError: String?,
    reason: String,
    reasonError: String?,
    selectedUrgency: Urgency,
    isIncreasing: Boolean,
    onNewAmountChanged: (String) -> Unit,
    onReasonChanged: (String) -> Unit,
    onUrgencySelected: (Urgency) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Aumentar Presupuesto",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Presupuesto actual
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Presupuesto Actual:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = "$%.2f MXN".format(currentBudget),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }
                }

                // Nuevo monto
                OutlinedTextField(
                    value = newAmount,
                    onValueChange = onNewAmountChanged,
                    label = { Text("Nuevo Monto*") },
                    placeholder = { Text("Ingrese el nuevo monto") },
                    isError = newAmountError != null,
                    supportingText = {
                        newAmountError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    leadingIcon = {
                        Text(
                            text = "$",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5AC5C5),
                        focusedLabelColor = Color(0xFF5AC5C5)
                    )
                )

                // Motivo
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChanged,
                    label = { Text("Motivo del Aumento*") },
                    placeholder = { Text("Explique por qué necesita aumentar el presupuesto") },
                    isError = reasonError != null,
                    supportingText = {
                        reasonError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5AC5C5),
                        focusedLabelColor = Color(0xFF5AC5C5)
                    )
                )

                // Urgencia
                Text(
                    text = "Urgencia:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Urgency.entries.forEach { urgency ->
                        FilterChip(
                            selected = selectedUrgency == urgency,
                            onClick = { onUrgencySelected(urgency) },
                            label = { Text(urgency.getDisplayName()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(urgency.getColor()).copy(alpha = 0.2f),
                                selectedLabelColor = Color(urgency.getColor())
                            )
                        )
                    }
                }

                // Aumento calculado
                val increase = (newAmount.toDoubleOrNull() ?: 0.0) - currentBudget
                if (increase > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Aumento:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "+$%.2f MXN".format(increase),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isIncreasing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5AC5C5)
                )
            ) {
                if (isIncreasing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Solicitar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isIncreasing
            ) {
                Text("Cancelar")
            }
        }
    )
}