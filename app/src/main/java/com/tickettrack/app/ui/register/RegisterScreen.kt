package com.tickettrack.app.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Pantalla 1: Registro de datos de la empresa.
 *
 * Esta pantalla captura los datos de la empresa y valida antes
 * de permitir avanzar a la segunda pantalla.
 */
@Composable
fun RegisterScreen(
    onNavigateToEmployeeScreen: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Botón de regreso
        IconButton(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Regresar",
                tint = Color(0xFF187083)
            )
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Ingresa tus datos",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo: Nombre de la empresa
            OutlinedTextField(
                value = state.companyName,
                onValueChange = viewModel::onCompanyNameChanged,
                label = { Text("Nombre de la empresa") },
                placeholder = { Text("Ingrese el nombre") },
                isError = state.companyNameError != null,
                supportingText = {
                    state.companyNameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5AC5C5),
                    focusedLabelColor = Color(0xFF5AC5C5),
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    errorTextColor = Color.Black
                )
            )

            // Campo: RFC
            OutlinedTextField(
                value = state.companyRfc,
                onValueChange = viewModel::onCompanyRfcChanged,
                label = { Text("RFC") },
                placeholder = { Text("RFC") },
                isError = state.companyRfcError != null,
                supportingText = {
                    state.companyRfcError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5AC5C5),
                    focusedLabelColor = Color(0xFF5AC5C5),
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    errorTextColor = Color.Black
                )
            )

            // Campo: Teléfono oficina
            OutlinedTextField(
                value = state.companyPhone,
                onValueChange = viewModel::onCompanyPhoneChanged,
                label = { Text("Teléfono oficina") },
                placeholder = { Text("222-2222-222") },
                isError = state.companyPhoneError != null,
                supportingText = {
                    state.companyPhoneError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5AC5C5),
                    focusedLabelColor = Color(0xFF5AC5C5),
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    errorTextColor = Color.Black
                )
            )

            // Campo: Correo empresarial principal
            OutlinedTextField(
                value = state.companyEmail,
                onValueChange = viewModel::onCompanyEmailChanged,
                label = { Text("Correo empresarial principal") },
                placeholder = { Text("example.enterprise@enterprise.com") },
                isError = state.companyEmailError != null,
                supportingText = {
                    state.companyEmailError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5AC5C5),
                    focusedLabelColor = Color(0xFF5AC5C5),
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black,
                    errorTextColor = Color.Black
                )
            )

            // Botón Siguiente
            Button(
                onClick = {
                    if (viewModel.validateCompanyData()) {
                        onNavigateToEmployeeScreen()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF187083)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Siguiente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))


        }
    }
}