package com.tickettrack.app.ui.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.ui.auth.RegisterViewModel
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary

@Composable
fun RegisterStep1(viewModel: RegisterViewModel) {
    var companyName by remember { mutableStateOf(viewModel.companyName) }
    var rfc by remember { mutableStateOf(viewModel.rfc) }
    var officePhone by remember { mutableStateOf(viewModel.officePhone) }
    var companyEmail by remember { mutableStateOf(viewModel.companyEmail) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
    ) {
        // Título
        Text(
            text = "Datos de la empresa",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Proporciona los datos generales de tu empresa",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Nombre de la empresa
        OutlinedTextField(
            value = companyName,
            onValueChange = {
                companyName = it
                viewModel.companyName = it
            },
            label = { Text("Nombre de la empresa") },
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

        // RFC
        OutlinedTextField(
            value = rfc,
            onValueChange = {
                if (it.length <= 13) {
                    rfc = it.uppercase()
                    viewModel.rfc = it.uppercase()
                }
            },
            label = { Text("RFC") },
            supportingText = { Text("12-13 caracteres") },
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

        // Teléfono de oficina
        OutlinedTextField(
            value = officePhone,
            onValueChange = {
                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                    officePhone = it
                    viewModel.officePhone = it
                }
            },
            label = { Text("Teléfono de oficina") },
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

        // Correo empresarial principal
        OutlinedTextField(
            value = companyEmail,
            onValueChange = {
                companyEmail = it
                viewModel.companyEmail = it
            },
            label = { Text("Correo empresarial principal") },
            supportingText = { Text("Usado para iniciar sesión") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Siguiente
        Button(
            onClick = { viewModel.nextStep() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Siguiente", fontSize = 16.sp)
        }
    }
}