package com.tickettrack.app.ui.main.trips.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Paso 3: Presupuesto y Transportista
 */
@Composable
fun Step3BudgetScreen(
    viewModel: TripCreateViewModel,
    state: TripCreateState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título del paso
        Text(
            text = "Presupuesto y Transportista",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Asigna el presupuesto y selecciona el transportista",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de presupuesto
        OutlinedTextField(
            value = state.budget,
            onValueChange = viewModel::onBudgetChanged,
            label = { Text("Presupuesto Asignado (MXN)*") },
            placeholder = { Text("Ej: 50000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = state.budgetError != null,
            supportingText = {
                state.budgetError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5AC5C5),
                focusedLabelColor = Color(0xFF5AC5C5)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sección de transportistas
        Text(
            text = "Seleccionar Transportista*",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Loading de transportistas
        if (state.isLoadingDrivers) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(color = Color(0xFF5AC5C5))
                    Text(
                        text = "Cargando transportistas...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else if (state.availableDrivers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "No hay transportistas disponibles en este momento",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC62828)
                    )
                }
            }
        } else {
            // Lista de transportistas
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.availableDrivers) { driver ->
                    DriverCard(
                        driver = driver,
                        isSelected = state.selectedDriverId == driver.uid,
                        onClick = { viewModel.onDriverSelected(driver.uid) }
                    )
                }
            }
        }

        // Error de transportista
        state.selectedDriverError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Espaciador para los botones
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Tarjeta de transportista individual.
 */
@Composable
private fun DriverCard(
    driver: DriverResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFFE0F7FA)
            } else {
                Color.White
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, Color(0xFF5AC5C5))
        } else {
            BorderStroke(1.dp, Color(0xFFE0E0E0))
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de selección
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF5AC5C5)
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del transportista
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = driver.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF00796B) else Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = driver.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                if (driver.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📱 ${driver.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Badge de estado
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (driver.isActive) Color(0xFF4CAF50) else Color(0xFFFF5252)
            ) {
                Text(
                    text = if (driver.isActive) "Activo" else "Inactivo",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}