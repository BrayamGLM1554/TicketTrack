package com.tickettrack.app.ui.trips.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paso 3: Presupuesto y Transportista
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3BudgetScreen(
    viewModel: TripCreateViewModel,
    state: TripCreateState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título del paso
        Text(
            text = "Presupuesto y Asignación",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Asigna el presupuesto y selecciona el transportista",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Presupuesto
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Presupuesto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5AC5C5)
                    )
                }

                OutlinedTextField(
                    value = state.budget,
                    onValueChange = viewModel::onBudgetChanged,
                    label = { Text("Presupuesto Inicial (MXN)*") },
                    placeholder = { Text("Ej: 25000") },
                    isError = state.budgetError != null,
                    supportingText = {
                        state.budgetError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Text(
                            text = "$",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5AC5C5),
                        focusedLabelColor = Color(0xFF5AC5C5),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                // Nota sobre presupuesto
                Text(
                    text = "Este será el presupuesto inicial del viaje. Podrás aumentarlo más tarde si es necesario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Transportista
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🚛",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Transportista",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5AC5C5)
                    )
                }

                if (state.isLoadingDrivers) {
                    // Loading
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF5AC5C5)
                        )
                    }
                } else if (state.availableDrivers.isEmpty()) {
                    // Sin transportistas
                    Text(
                        text = "No hay transportistas disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                } else {
                    // Dropdown de transportistas
                    var expanded by remember { mutableStateOf(false) }
                    val selectedDriver = state.availableDrivers.find { it.id == state.selectedDriverId }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedDriver?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Seleccionar Transportista*") },
                            placeholder = { Text("Selecciona un transportista disponible") },
                            isError = state.selectedDriverError != null,
                            supportingText = {
                                state.selectedDriverError?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5AC5C5),
                                focusedLabelColor = Color(0xFF5AC5C5),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            state.availableDrivers.forEach { driver ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = driver.name,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${driver.brand} ${driver.model} - ${driver.plates}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "${driver.completedTrips} viajes completados",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF4CAF50)
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.onDriverSelected(driver.id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Información del transportista seleccionado
                    if (selectedDriver != null) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Información del Transportista",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                DriverInfoRow("Nombre", selectedDriver.name)
                                DriverInfoRow("Unidad", selectedDriver.assignedUnit)
                                DriverInfoRow("Placas", selectedDriver.plates)
                                DriverInfoRow("Vehículo", "${selectedDriver.brand} ${selectedDriver.model}")
                                DriverInfoRow(
                                    "Viajes Completados",
                                    "${selectedDriver.completedTrips}",
                                    valueColor = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Resumen del viaje
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📋 Resumen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                SummaryRow("Carga", state.cargoName)
                SummaryRow("Tipo", state.cargoType)
                SummaryRow("Peso", "${state.weight} kg")
                SummaryRow("Origen", "${state.originCity}, ${state.originState}")
                SummaryRow("Destino", "${state.destinationCity}, ${state.destinationState}")
                SummaryRow("Presupuesto", "$${state.budget} MXN")

                val selectedDriver = state.availableDrivers.find { it.id == state.selectedDriverId }
                if (selectedDriver != null) {
                    SummaryRow("Transportista", selectedDriver.name)
                }
            }
        }

        // Espaciador para los botones
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun DriverInfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2E7D32)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1B5E20)
        )
    }
}