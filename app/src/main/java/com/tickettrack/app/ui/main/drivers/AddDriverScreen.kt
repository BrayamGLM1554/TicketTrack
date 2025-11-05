package com.tickettrack.app.ui.main.drivers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDriverScreen(
    viewModel: DriverViewModel,
    onBack: () -> Unit,
    onDriverAdded: () -> Unit
) {
    val state = viewModel.addDriverState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.onLicenseCaducationChanged(date.toString())
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar", color = Color(0xFF187083))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF187083)
                )
            )
        }
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
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF187083)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Alta de Transportista",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                        Text(
                            text = "Registrar nuevo conductor",
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
            // Sección: Información Personal
            SectionHeader(title = "Información Personal")

            InputFieldWithIcon(
                value = state.value.fullName,
                onValueChange = { viewModel.onFullNameChanged(it) },
                label = "Nombre completo",
                icon = Icons.Default.Person,
                placeholder = "Juan Pérez García",
                isError = state.value.fullNameError != null,
                errorMessage = state.value.fullNameError,
                isRequired = true
            )

            InputFieldWithIcon(
                value = state.value.email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = "Correo electrónico",
                icon = Icons.Default.Email,
                placeholder = "chofer@example.com",
                keyboardType = KeyboardType.Email,
                isError = state.value.emailError != null,
                errorMessage = state.value.emailError,
                isRequired = true
            )

            InputFieldWithIcon(
                value = state.value.phone,
                onValueChange = { viewModel.onPhoneChanged(it) },
                label = "Teléfono",
                icon = Icons.Default.Phone,
                placeholder = "+52 555-123-4567",
                keyboardType = KeyboardType.Phone,
                isError = state.value.phoneError != null,
                errorMessage = state.value.phoneError,
                isRequired = true
            )

            // Campo de contraseña
            Column {
                Row(
                    modifier = Modifier.padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contraseña",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2C3E50)
                    )
                    Text(
                        text = " (opcional)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                OutlinedTextField(
                    value = state.value.password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Se generará automáticamente si se deja vacío",
                            color = Color.Gray,
                            fontSize = 12.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (state.value.passwordError != null)
                                MaterialTheme.colorScheme.error else Color(0xFF187083)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (showPassword) "Ocultar contraseña"
                                else "Mostrar contraseña",
                                tint = Color(0xFF187083)
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = state.value.passwordError != null,
                    supportingText = {
                        if (state.value.passwordError != null) {
                            Text(
                                text = state.value.passwordError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF187083),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    shape = MaterialTheme.shapes.small
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sección: Información de Licencia
            SectionHeader(title = "Información de Licencia")

            InputFieldWithIcon(
                value = state.value.licenseNumber,
                onValueChange = { viewModel.onLicenseNumberChanged(it) },
                label = "Número de licencia",
                icon = Icons.Default.CreditCard,
                placeholder = "LIC123456",
                isError = state.value.licenseNumberError != null,
                errorMessage = state.value.licenseNumberError,
                isRequired = true
            )

            // Fecha de caducidad con DatePicker (OPCIONAL)
            Column {
                Row(
                    modifier = Modifier.padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fecha de caducidad",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2C3E50)
                    )
                    Text(
                        text = " (opcional)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                OutlinedTextField(
                    value = state.value.licenseCaducation,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    placeholder = { Text("Seleccionar fecha", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (state.value.licenseCaducationError != null)
                                MaterialTheme.colorScheme.error else Color(0xFF187083)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Seleccionar fecha",
                                tint = Color(0xFF187083)
                            )
                        }
                    },
                    isError = state.value.licenseCaducationError != null,
                    supportingText = {
                        if (state.value.licenseCaducationError != null) {
                            Text(
                                text = state.value.licenseCaducationError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF187083),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        disabledTextColor = Color.Black
                    ),
                    shape = MaterialTheme.shapes.small
                )
            }

            InputFieldWithIcon(
                value = state.value.unitNumber,
                onValueChange = { viewModel.onUnitNumberChanged(it) },
                label = "Número de unidad",
                icon = Icons.Default.DirectionsBus,
                placeholder = "U-045",
                isError = state.value.unitNumberError != null,
                errorMessage = state.value.unitNumberError,
                isRequired = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Estado activo (siempre true por defecto)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE0F2F7),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Estado del transportista",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2C3E50)
                        )
                        Text(
                            text = "Por defecto se registrará como ACTIVO",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Surface(
                        color = Color(0xFF187083),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "ACTIVO",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Mensaje de error general
            if (state.value.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.value.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Guardar
            Button(
                onClick = {
                    viewModel.addDriver(
                        onSuccess = onDriverAdded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.value.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF187083)
                )
            ) {
                if (state.value.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Registrar Transportista",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Espacio para el BottomNavBar
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF187083),
        modifier = Modifier.padding(vertical = 4.dp)
    )
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
            shape = MaterialTheme.shapes.small
        )
    }
}