package com.tickettrack.app.ui.auth.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.ui.auth.RegisterViewModel
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary

@Composable
fun RegisterStep2(viewModel: RegisterViewModel) {
    var managerName by remember { mutableStateOf("") }
    var curp by remember { mutableStateOf("") }
    var workPhone by remember { mutableStateOf("") }
    var personalEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Validaciones de contraseña en tiempo real
    val hasMinLength = password.length >= 8
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
    ) {
        // Título
        Text(
            text = "Datos del encargado",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Información del responsable que usará la cuenta",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Nombre del encargado
        OutlinedTextField(
            value = managerName,
            onValueChange = { managerName = it },
            label = { Text("Nombre del encargado") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CURP
        OutlinedTextField(
            value = curp,
            onValueChange = {
                if (it.length <= 18) {
                    curp = it.uppercase()
                }
            },
            label = { Text("CURP") },
            supportingText = { Text("18 caracteres") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Teléfono de trabajo
        OutlinedTextField(
            value = workPhone,
            onValueChange = {
                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                    workPhone = it
                }
            },
            label = { Text("Teléfono personal") },
            supportingText = { Text("10 dígitos") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Correo empresarial personal
        OutlinedTextField(
            value = personalEmail,
            onValueChange = { personalEmail = it },
            label = { Text("Correo empresarial personal") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Crear contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary
            )
        )

        // Requisitos de contraseña
        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "La contraseña debe contener:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PasswordRequirement(
                        text = "Mínimo 8 caracteres",
                        isMet = hasMinLength
                    )
                    PasswordRequirement(
                        text = "Una letra mayúscula (A-Z)",
                        isMet = hasUpperCase
                    )
                    PasswordRequirement(
                        text = "Una letra minúscula (a-z)",
                        isMet = hasLowerCase
                    )
                    PasswordRequirement(
                        text = "Un número (0-9)",
                        isMet = hasDigit
                    )
                    PasswordRequirement(
                        text = "Un caracter especial (!@#$%^&*)",
                        isMet = hasSpecialChar
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirmar contraseña
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón Atrás
            OutlinedButton(
                onClick = { viewModel.previousStep() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Primary
                )
            ) {
                Text("Atrás", fontSize = 16.sp)
            }

            // Botón Registrar
            Button(
                onClick = {
                    viewModel.register(
                        nameOfManager = managerName,
                        curp = curp,
                        workPhone = workPhone,
                        personalEmail = personalEmail,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Registrar", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PasswordRequirement(
    text: String,
    isMet: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF4CAF50) else Color(0xFFBDBDBD),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isMet) Color(0xFF4CAF50) else Color(0xFF666666)
        )
    }
}