package com.tickettrack.app.ui.trips.create

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Pantalla de creación de viaje con formulario multi-step.
 *
 * Pasos:
 * 1. Información de Carga
 * 2. Origen y Destino
 * 3. Presupuesto y Transportista
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCreateScreen(
    onNavigateBack: () -> Unit,
    onTripCreated: () -> Unit,
    currentUserId: String,
    currentUserName: String,
    viewModel: TripCreateViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Inicializar con datos del usuario
    LaunchedEffect(Unit) {
        viewModel.initialize(currentUserId, currentUserName)
    }

    // Navegar de regreso cuando el viaje se crea exitosamente
    LaunchedEffect(state.creationSuccess) {
        if (state.creationSuccess) {
            kotlinx.coroutines.delay(2000)
            onTripCreated()
        }
    }

    // Mostrar error si existe
    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Error") },
            text = { Text(state.errorMessage ?: "") },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("Entendido")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nuevo Viaje",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Paso ${state.currentStep} de ${state.totalSteps}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.canGoBack()) {
                            viewModel.previousStep()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color(0xFF5AC5C5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de progreso
            LinearProgressIndicator(
                progress = { state.getProgress() },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF5AC5C5)
            )

            // Contenido del paso actual
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.currentStep) {
                    1 -> Step1CargoScreen(viewModel = viewModel, state = state)
                    2 -> Step2LocationsScreen(viewModel = viewModel, state = state)
                    3 -> Step3BudgetScreen(viewModel = viewModel, state = state)
                }

                // Overlay de éxito
                if (state.creationSuccess) {
                    SuccessOverlay()
                }
            }

            // Botones de navegación
            NavigationButtons(
                currentStep = state.currentStep,
                totalSteps = state.totalSteps,
                canProceed = state.canProceedToNextStep(),
                isCreating = state.isCreating,
                onBack = { viewModel.previousStep() },
                onNext = {
                    when (state.currentStep) {
                        1 -> {
                            viewModel.validateStep1()
                            if (state.isStep1Valid()) {
                                viewModel.nextStep()
                            }
                        }
                        2 -> {
                            viewModel.validateStep2()
                            if (state.isStep2Valid()) {
                                viewModel.nextStep()
                            }
                        }
                        3 -> {
                            viewModel.createTrip()
                        }
                    }
                }
            )
        }
    }
}

/**
 * Botones de navegación entre pasos.
 */
@Composable
private fun NavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    isCreating: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón Atrás (solo si no es el primer paso)
        if (currentStep > 1) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                enabled = !isCreating,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF5AC5C5)
                )
            ) {
                Text("Atrás")
            }
        }

        // Botón Siguiente/Crear
        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = canProceed && !isCreating,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5AC5C5),
                disabledContainerColor = Color.Gray
            )
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (currentStep == totalSteps) "Generar Viaje" else "Siguiente",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Overlay de éxito al crear el viaje.
 */
@Composable
private fun SuccessOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Éxito",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¡Viaje Creado!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "El viaje ha sido registrado exitosamente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}