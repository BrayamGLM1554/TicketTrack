package com.tickettrack.app.ui.transportista

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestBudgetIncreaseScreen(
    token: String,
    tripId: String,
    driverId: String,
    currentBudget: Double,
    onNavigateBack: () -> Unit,
    viewModel: RequestBudgetViewModel = koinViewModel()
) {
    // requestedAmount ahora representa el MONTO DEL AUMENTO
    var requestedAmount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val minReasonLength = 20
    var reasonError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    // Calcular el nuevo presupuesto total en tiempo real
    val requestedBudget = remember(requestedAmount, currentBudget) {
        val requestedIncrease = requestedAmount.toDoubleOrNull() ?: 0.0
        currentBudget + requestedIncrease
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Solicitar Aumento",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de presupuesto actual
            CurrentBudgetCard(currentBudget = currentBudget)

            // Card de solicitud
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Detalles de la Solicitud",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    // Campo de monto solicitado (ahora es el AUMENTO)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Monto de Aumento Solicitado", // ¡CAMBIO AQUÍ!
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )

                        OutlinedTextField(
                            value = requestedAmount,
                            onValueChange = { newValue ->
                                // Solo permitir números y un punto decimal
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    requestedAmount = newValue
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: 1000") }, // ¡CAMBIO AQUÍ!
                            leadingIcon = {
                                Text(
                                    text = "$",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                cursorColor = Primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Mostrar nuevo presupuesto total y el monto de aumento
                        val requestedIncrease = requestedAmount.toDoubleOrNull() ?: 0.0
                        if (requestedIncrease > 0) { // ¡CAMBIO EN LA CONDICIÓN!
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Monto de Aumento:", // ¡CAMBIO AQUÍ!
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "+$${String.format("%.0f", requestedIncrease)}", // ¡USAMOS EL VALOR INGRESADO!
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp)) // Añadir espaciado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nuevo Presupuesto Total:", // ¡NUEVA LÍNEA!
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text(
                                    text = "$${String.format("%.0f", requestedBudget)}", // ¡NUEVA LÍNEA!
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Primary
                                )
                            }
                        }
                    }

                    // Campo de razón
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Motivo de la Solicitud *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )

                        OutlinedTextField(
                            value = reason,
                            onValueChange = {
                                reason = it
                                if (it.length >= minReasonLength) {
                                    reasonError = null
                                } else if (it.isNotBlank()) {
                                    reasonError = "La razón debe tener al menos $minReasonLength caracteres."
                                }
                            },
                            isError = reasonError != null, // <-- Usar el estado de error
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            placeholder = {
                                Text(
                                    text = "Explica por qué necesitas este aumento de presupuesto...",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            },
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                cursorColor = Primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (reasonError != null) {
                            Text(
                                text = reasonError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        // ... (contador de caracteres)
                        Text(
                            text = "${reason.length}/500 caracteres",
                            fontSize = 12.sp,
                            color = if (reason.length > 500) MaterialTheme.colorScheme.error else TextSecondary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            // Guía de sugerencias
            SuggestionsCard()

            // Mostrar error si existe
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = uiState.error ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFFC62828),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Botón de enviar
            Button(
                onClick = {
                    val requestedIncreaseCheck = requestedAmount.toDoubleOrNull() // Ahora es el aumento

                    //Re-validar antes de enviar
                    if (reason.length < minReasonLength) {
                        reasonError = "La razón debe tener al menos $minReasonLength caracteres."
                        return@Button
                    }

                    // ¡CAMBIO EN LA VALIDACIÓN! Comprobar que el aumento sea > 0
                    if (requestedIncreaseCheck != null && requestedIncreaseCheck > 0) {
                        viewModel.submitBudgetRequest(
                            token = token,
                            tripId = tripId,
                            driverId = driverId,
                            requestedBudget = requestedBudget, // ¡ENVIAMOS EL TOTAL CALCULADO!
                            reason = reason
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading &&
                        requestedAmount.toDoubleOrNull()?.let { it > 0 } == true && // ¡CAMBIO EN LA CONDICIÓN!
                        reason.isNotBlank() &&
                        reason.length >= minReasonLength &&
                        reason.length <= 500,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Enviar Solicitud",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "¡Solicitud Enviada!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Tu solicitud de aumento de presupuesto ha sido enviada al administrador para su revisión. Recibirás una notificación cuando sea procesada.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Text("Entendido")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CurrentBudgetCard(currentBudget: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Presupuesto Actual",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.0f", currentBudget)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(28.dp),
                color = Primary.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF9E6)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFFFA000),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Consejos para tu solicitud",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            SuggestionItem(
                text = "Explica gastos específicos que requieres cubrir"
            )
            SuggestionItem(
                text = "Menciona si son imprevistos o necesidades adicionales del viaje"
            )
            SuggestionItem(
                text = "Sé claro y conciso en tu justificación"
            )
        }
    }
}

@Composable
fun SuggestionItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            fontSize = 14.sp,
            color = Color(0xFFFFA000),
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF1A1A1A),
            lineHeight = 18.sp
        )
    }
}