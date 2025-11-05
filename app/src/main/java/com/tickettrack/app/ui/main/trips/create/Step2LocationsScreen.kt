package com.tickettrack.app.ui.main.trips.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paso 2: Origen y Destino
 */
@Composable
fun Step2LocationsScreen(
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
            text = "Ubicaciones",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Especifica el origen y destino del viaje",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ==================== ORIGEN ====================
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
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        text = "📍",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Origen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5AC5C5)
                    )
                }

                OutlinedTextField(
                    value = state.originAddress,
                    onValueChange = viewModel::onOriginAddressChanged,
                    label = { Text("Dirección*") },
                    placeholder = { Text("Ej: Av. Insurgentes 1234") },
                    isError = state.originAddressError != null,
                    supportingText = {
                        state.originAddressError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5AC5C5),
                        focusedLabelColor = Color(0xFF5AC5C5),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.originCity,
                        onValueChange = viewModel::onOriginCityChanged,
                        label = { Text("Ciudad*") },
                        placeholder = { Text("CDMX") },
                        isError = state.originCityError != null,
                        supportingText = {
                            state.originCityError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5AC5C5),
                            focusedLabelColor = Color(0xFF5AC5C5),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = state.originState,
                        onValueChange = viewModel::onOriginStateChanged,
                        label = { Text("Estado*") },
                        placeholder = { Text("CDMX") },
                        isError = state.originStateError != null,
                        supportingText = {
                            state.originStateError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5AC5C5),
                            focusedLabelColor = Color(0xFF5AC5C5),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                }

                OutlinedTextField(
                    value = state.originZipCode,
                    onValueChange = viewModel::onOriginZipCodeChanged,
                    label = { Text("Código Postal*") },
                    placeholder = { Text("03100") },
                    isError = state.originZipCodeError != null,
                    supportingText = {
                        state.originZipCodeError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5AC5C5),
                        focusedLabelColor = Color(0xFF5AC5C5),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }
        }

        // ==================== DESTINO ====================
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
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        text = "🏁",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Destino",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5AC5C5)
                    )
                }

                OutlinedTextField(
                    value = state.destinationAddress,
                    onValueChange = viewModel::onDestinationAddressChanged,
                    label = { Text("Dirección*") },
                    placeholder = { Text("Ej: Carretera Federal 57 Km 45") },
                    isError = state.destinationAddressError != null,
                    supportingText = {
                        state.destinationAddressError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5AC5C5),
                        focusedLabelColor = Color(0xFF5AC5C5),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.destinationCity,
                        onValueChange = viewModel::onDestinationCityChanged,
                        label = { Text("Ciudad*") },
                        placeholder = { Text("Querétaro") },
                        isError = state.destinationCityError != null,
                        supportingText = {
                            state.destinationCityError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5AC5C5),
                            focusedLabelColor = Color(0xFF5AC5C5),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = state.destinationState,
                        onValueChange = viewModel::onDestinationStateChanged,
                        label = { Text("Estado*") },
                        placeholder = { Text("Querétaro") },
                        isError = state.destinationStateError != null,
                        supportingText = {
                            state.destinationStateError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5AC5C5),
                            focusedLabelColor = Color(0xFF5AC5C5),
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                }

                OutlinedTextField(
                    value = state.destinationZipCode,
                    onValueChange = viewModel::onDestinationZipCodeChanged,
                    label = { Text("Código Postal*") },
                    placeholder = { Text("76000") },
                    isError = state.destinationZipCodeError != null,
                    supportingText = {
                        state.destinationZipCodeError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5AC5C5),
                        focusedLabelColor = Color(0xFF5AC5C5),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }
        }

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
                    text = "🌍",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Las coordenadas se obtendrán automáticamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1976D2)
                )
            }
        }

        // Espaciador para los botones
        Spacer(modifier = Modifier.height(80.dp))
    }
}