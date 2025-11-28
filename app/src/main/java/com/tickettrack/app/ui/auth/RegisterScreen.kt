package com.tickettrack.app.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tickettrack.app.domain.model.UserProfile
import com.tickettrack.app.ui.auth.components.RegisterStep1
import com.tickettrack.app.ui.auth.components.RegisterStep2
import com.tickettrack.app.ui.theme.Primary
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (UserProfile) -> Unit, // CAMBIO: Ahora recibe UserProfile
    onBackPressed: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    val context = LocalContext.current

    // Observar estados
    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is RegisterState.Success -> {
                // ¡Registro exitoso e inicio de sesión automático!
                Toast.makeText(
                    context,
                    "¡Registro exitoso! Bienvenido a TicketTrack",
                    Toast.LENGTH_SHORT
                ).show()
                onRegisterSuccess(state.userProfile) // Navegar directamente a la app
            }
            is RegisterState.SuccessButLoginFailed -> {
                // Registro exitoso pero falló el login automático
                Toast.makeText(
                    context,
                    "Registro exitoso. Por favor, inicia sesión manualmente",
                    Toast.LENGTH_LONG
                ).show()
                onBackPressed() // Llevar al usuario de vuelta al login
            }
            is RegisterState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.clearError() // ARREGLADO: era registerViewModel, debe ser viewModel
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == 2) {
                            viewModel.previousStep()
                        } else {
                            onBackPressed()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Step Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (currentStep >= 1) Primary else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (currentStep >= 2) Primary else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }

                // Contenido según el paso
                when (currentStep) {
                    1 -> RegisterStep1(viewModel)
                    2 -> RegisterStep2(viewModel)
                }
            }

            // Loading Overlay
            if (registerState is RegisterState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        }
    }
}