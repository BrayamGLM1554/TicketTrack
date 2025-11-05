package com.tickettrack.app.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Pantalla 1: Registro de datos de la empresa.
 */
@Composable
fun RegisterScreen(
    onNavigateToEmployeeScreen: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header personalizado
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color(0xFF187083)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Crea tu cuenta",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                        Text(
                            text = "Información de la empresa",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            }
        }

        // Contenido con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card con formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Datos de la Empresa",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF187083)
                    )

                    InputFieldWithIcon(
                        value = state.companyName,
                        onValueChange = viewModel::onCompanyNameChanged,
                        label = "Nombre de la empresa",
                        icon = Icons.Default.Business,
                        placeholder = "Ingrese el nombre",
                        isError = state.companyNameError != null,
                        errorMessage = state.companyNameError,
                        isRequired = true
                    )

                    InputFieldWithIcon(
                        value = state.companyRfc,
                        onValueChange = viewModel::onCompanyRfcChanged,
                        label = "RFC",
                        icon = Icons.Default.Badge,
                        placeholder = "RFC de la empresa",
                        isError = state.companyRfcError != null,
                        errorMessage = state.companyRfcError,
                        isRequired = true
                    )

                    InputFieldWithIcon(
                        value = state.companyPhone,
                        onValueChange = viewModel::onCompanyPhoneChanged,
                        label = "Teléfono oficina",
                        icon = Icons.Default.Phone,
                        placeholder = "222-2222-222",
                        keyboardType = KeyboardType.Phone,
                        isError = state.companyPhoneError != null,
                        errorMessage = state.companyPhoneError,
                        isRequired = true
                    )

                    InputFieldWithIcon(
                        value = state.companyEmail,
                        onValueChange = viewModel::onCompanyEmailChanged,
                        label = "Correo empresarial principal",
                        icon = Icons.Default.Email,
                        placeholder = "empresa@ejemplo.com",
                        keyboardType = KeyboardType.Email,
                        isError = state.companyEmailError != null,
                        errorMessage = state.companyEmailError,
                        isRequired = true
                    )
                }
            }

            // Info adicional
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "El correo empresarial será usado para acceder al sistema",
                        fontSize = 12.sp,
                        color = Color(0xFF1565C0)
                    )
                }
            }

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
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Siguiente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InputFieldWithIcon(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null,
    isRequired: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C3E50)
            )
            if (isRequired) {
                Text(
                    text = " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Red
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(placeholder, color = Color.Gray, fontSize = 14.sp)
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error else Color(0xFF187083)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            supportingText = {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF187083),
                unfocusedBorderColor = Color(0xFFBDBDBD),
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLeadingIconColor = Color(0xFF187083),
                unfocusedLeadingIconColor = Color.Gray
            ),
            shape = MaterialTheme.shapes.small,
            singleLine = true
        )
    }
}