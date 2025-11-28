package com.tickettrack.app.ui.drivers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.domain.model.Driver
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriversListScreen(
    token: String,
    onNavigateToCreateDriver: () -> Unit,
    onNavigateToDriverDetails: (String) -> Unit,
    viewModel: DriversViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var driverToDelete by remember { mutableStateOf<Driver?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDrivers(token)
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && driverToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                driverToDelete = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Eliminar Transportista",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "¿Estás seguro de que deseas eliminar a:",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        driverToDelete!!.fullName,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        driverToDelete!!.email,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Esta acción no se puede deshacer.",
                        fontSize = 14.sp,
                        color = Color(0xFFEF5350)
                    )

                    if (uiState.deleteError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            uiState.deleteError!!,
                            color = Color(0xFFEF5350),
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDriver(token, driverToDelete!!.uid)
                    },
                    enabled = !uiState.isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF5350)
                    )
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (uiState.isDeleting) "Eliminando..." else "Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        driverToDelete = null
                        viewModel.clearDeleteState()
                    },
                    enabled = !uiState.isDeleting
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Mostrar éxito y cerrar diálogo
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            showDeleteDialog = false
            driverToDelete = null
            viewModel.clearDeleteState()
            viewModel.loadDrivers(token) // Recargar lista
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header con búsqueda
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Transportistas",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "${uiState.drivers.size} registrados",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }

                    FloatingActionButton(
                        onClick = onNavigateToCreateDriver,
                        containerColor = Primary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar transportista"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchDrivers(token, it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por nombre o email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.searchDrivers(token, "")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }
        }

        // Contenido
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDrivers(token) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            uiState.drivers.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "No se encontraron transportistas"
                            } else {
                                "No hay transportistas registrados"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Agrega tu primer transportista",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.drivers,
                        key = { it.uid }
                    ) { driver ->
                        SwipeToDeleteDriverCard(
                            driver = driver,
                            onDriverClick = onNavigateToDriverDetails,
                            onDeleteClick = {
                                driverToDelete = driver
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeToDeleteDriverCard(
    driver: Driver,
    onDriverClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val maxSwipeDistance = -200f // Mayor distancia para que el card se oculte más
    val deleteThreshold = -60f // Umbral más sensible

    // Animar el offset con suavidad
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        label = "swipe_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp) // Altura fija para el contenedor
    ) {
        // Fondo rojo de eliminación (mismo tamaño que el card)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEF5350))
                .clickable(
                    enabled = offsetX < -10f,
                    onClick = { onDeleteClick() }
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Solo mostrar el ícono cuando el swipe sea visible
            if (offsetX < -10f) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .size(28.dp)
                )
            }
        }

        // Card del driver (foreground) - Altura exacta
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Snap a posición cerrada o abierta
                            offsetX = if (offsetX < deleteThreshold) {
                                maxSwipeDistance
                            } else {
                                0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            // Limitar el swipe solo hacia la izquierda
                            offsetX = newOffset.coerceIn(maxSwipeDistance, 0f)
                        }
                    )
                }
                .clickable {
                    if (offsetX < -10f) {
                        // Si está abierto, cerrar
                        offsetX = 0f
                    } else {
                        // Si está cerrado, navegar
                        onDriverClick(driver.uid)
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = driver.fullName.firstOrNull()?.toString()?.uppercase() ?: "T",
                        color = Primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Información
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driver.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = driver.email,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lic. ${driver.licenseNumber}",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Indicador de estado
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }
    }
}