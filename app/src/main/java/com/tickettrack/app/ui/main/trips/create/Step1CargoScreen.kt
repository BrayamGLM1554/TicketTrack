package com.tickettrack.app.ui.main.trips.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paso 1: Información de la Carga
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1CargoScreen(
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
            text = "Información de la Carga",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Proporciona los detalles de la carga a transportar",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre de la Carga
        OutlinedTextField(
            value = state.cargoName,
            onValueChange = viewModel::onCargoNameChanged,
            label = { Text("Nombre de la Carga*") },
            placeholder = { Text("Ej: Entrega de Maquinaria Pesada") },
            isError = state.cargoNameError != null,
            supportingText = {
                state.cargoNameError?.let {
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

        // Tipo de Carga (Dropdown)
        var expanded by remember { mutableStateOf(false) }
        val cargoTypes = listOf(
            "Maquinaria",
            "Alimentos",
            "Materiales de Construcción",
            "Productos Químicos",
            "Textiles",
            "Electrónicos",
            "Automóviles",
            "Medicamentos",
            "Otros"
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = state.cargoType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Carga*") },
                placeholder = { Text("Selecciona el tipo") },
                isError = state.cargoTypeError != null,
                supportingText = {
                    state.cargoTypeError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5AC5C5),
                    focusedLabelColor = Color(0xFF5AC5C5)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                cargoTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            viewModel.onCargoTypeChanged(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Peso (kg)
        OutlinedTextField(
            value = state.weight,
            onValueChange = viewModel::onWeightChanged,
            label = { Text("Peso (kg)*") },
            placeholder = { Text("Ej: 5000") },
            isError = state.weightError != null,
            supportingText = {
                state.weightError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5AC5C5),
                focusedLabelColor = Color(0xFF5AC5C5)
            )
        )

        // Descripción
        OutlinedTextField(
            value = state.description,
            onValueChange = viewModel::onDescriptionChanged,
            label = { Text("Descripción*") },
            placeholder = { Text("Describe la carga con detalle") },
            isError = state.descriptionError != null,
            supportingText = {
                state.descriptionError?.let {
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

        // Requisitos Especiales (Opcional)
        OutlinedTextField(
            value = state.specialRequirements,
            onValueChange = viewModel::onSpecialRequirementsChanged,
            label = { Text("Requisitos Especiales (Opcional)") },
            placeholder = { Text("Ej: Carga frágil, refrigeración") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5AC5C5),
                focusedLabelColor = Color(0xFF5AC5C5)
            )
        )

        // Nota informativa
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "💡",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Los campos marcados con * son obligatorios",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1976D2)
                )
            }
        }

        // Espaciador para los botones
        Spacer(modifier = Modifier.height(80.dp))
    }
}