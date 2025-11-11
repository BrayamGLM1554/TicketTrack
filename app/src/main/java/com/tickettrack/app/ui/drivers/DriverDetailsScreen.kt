package com.tickettrack.app.ui.drivers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.domain.model.Driver
import com.tickettrack.app.domain.model.UpdateDriverRequest
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDetailsScreen(
    token: String,
    driverUid: String,
    isAdmin: Boolean,
    onBackPressed: () -> Unit,
    viewModel: DriversViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(driverUid) {
        viewModel.loadDriverDetails(token, driverUid)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelectedDriver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Transportista") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (uiState.selectedDriver != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                uiState.isLoadingDetails -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                uiState.detailsError != null -> {
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
                                text = uiState.detailsError ?: "Error desconocido",
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadDriverDetails(token, driverUid) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                uiState.selectedDriver != null -> {
                    DriverDetailsContent(
                        driver = uiState.selectedDriver!!,
                        isAdmin = isAdmin
                    )
                }
            }
        }
    }

    if (showEditDialog && uiState.selectedDriver != null) {
        EditDriverDialog(
            driver = uiState.selectedDriver!!,
            isAdmin = isAdmin,
            isUpdating = uiState.isUpdating,
            updateError = uiState.updateError,
            onDismiss = {
                showEditDialog = false
                viewModel.clearUpdateState()
            },
            onConfirm = { request ->
                viewModel.updateDriver(token, driverUid, request)
            }
        )

        LaunchedEffect(uiState.updateSuccess) {
            if (uiState.updateSuccess) {
                showEditDialog = false
                viewModel.clearUpdateState()
            }
        }
    }
}

@Composable
fun DriverDetailsContent(
    driver: Driver,
    isAdmin: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header con avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = driver.fullName.firstOrNull()?.toString()?.uppercase() ?: "T",
                        color = Primary,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = driver.fullName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = driver.role,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Información Personal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Información Personal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(16.dp))

                DetailInfoItem(
                    icon = Icons.Default.Email,
                    label = "Correo electrónico",
                    value = driver.email
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                DetailInfoItem(
                    icon = Icons.Default.Phone,
                    label = "Teléfono",
                    value = driver.personalPhoneEncrypted
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                DetailInfoItem(
                    icon = Icons.Default.Badge,
                    label = "Número de licencia",
                    value = driver.licenseNumber
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Información de la Empresa
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Información de la Empresa",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(16.dp))

                DetailInfoItem(
                    icon = Icons.Default.Business,
                    label = "Compañía",
                    value = driver.companyName
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                DetailInfoItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha de registro",
                    value = formatDate(driver.createdAt)
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                DetailInfoItem(
                    icon = Icons.Default.PersonAdd,
                    label = "Creado por",
                    value = driver.createdBy
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DetailInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EditDriverDialog(
    driver: Driver,
    isAdmin: Boolean,
    isUpdating: Boolean,
    updateError: String?,
    onDismiss: () -> Unit,
    onConfirm: (UpdateDriverRequest) -> Unit
) {
    var fullName by remember { mutableStateOf(driver.fullName) }
    var phone by remember { mutableStateOf(driver.personalPhoneEncrypted) }
    var licenseNumber by remember { mutableStateOf(driver.licenseNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Transportista",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Nombre completo
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Teléfono
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )

                // Licencia (solo admin)
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = { licenseNumber = it },
                        label = { Text("Número de licencia") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Primary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            focusedLabelColor = Primary
                        )
                    )
                }

                // Error
                if (updateError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = updateError,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        UpdateDriverRequest(
                            fullName = if (fullName != driver.fullName) fullName else null,
                            personalPhoneEncrypted = if (phone != driver.personalPhoneEncrypted) phone else null,
                            licenseNumber = if (isAdmin && licenseNumber != driver.licenseNumber) licenseNumber else null
                        )
                    )
                },
                enabled = !isUpdating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating
            ) {
                Text("Cancelar")
            }
        }
    )
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}