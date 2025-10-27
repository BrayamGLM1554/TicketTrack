package com.tickettrack.app.ui.expenses.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tickettrack.app.domain.model.expense.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para registrar un nuevo gasto
 * HU16 - Registrar Gasto
 * HU17 - Moderación de Imágenes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseRegisterScreen(
    tripId: String,
    userId: String,
    userName: String,
    onNavigateBack: () -> Unit,
    onExpenseRegistered: () -> Unit,
    viewModel: ExpenseRegisterViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri)
    }

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.selectedDateMillis ?: System.currentTimeMillis()
    )

    // Efecto para navegar cuando el registro sea exitoso
    LaunchedEffect(state.registrationSuccess) {
        if (state.registrationSuccess) {
            onExpenseRegistered()
        }
    }

    // Mostrar error si existe
    state.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Date Picker Dialog
    if (state.showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { viewModel.hideDatePicker() },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.onDateSelected(millis)
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDatePicker() }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Category Picker Dialog
    if (state.showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCategoryPicker() },
            title = { Text("Seleccionar Categoría") },
            text = {
                Column {
                    ExpenseCategory.getAllDisplayNames().forEach { category ->
                        TextButton(
                            onClick = { viewModel.onCategoryChanged(category) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Gasto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5AC5C5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Monto
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.onAmountChanged(it) },
                label = { Text("Monto *") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                },
                isError = state.amountError != null,
                supportingText = state.amountError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Categoría
            OutlinedTextField(
                value = state.selectedCategory,
                onValueChange = {},
                label = { Text("Categoría *") },
                leadingIcon = {
                    Icon(Icons.Default.Category, contentDescription = null)
                },
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                },
                isError = state.categoryError != null,
                supportingText = state.categoryError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showCategoryPicker() },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Descripción
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Descripción *") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                isError = state.descriptionError != null,
                supportingText = state.descriptionError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                minLines = 2
            )

            // Fecha
            val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
            val dateText = state.selectedDateMillis?.let { millis ->
                dateFormatter.format(Date(millis))
            } ?: ""

            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                label = { Text("Fecha *") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                },
                isError = state.dateError != null,
                supportingText = state.dateError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showDatePicker() },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Imagen del Ticket
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = if (state.imageError != null) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ticket / Evidencia *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (state.ticketImageUri != null) {
                        // Mostrar imagen seleccionada
                        AsyncImage(
                            model = state.ticketImageUri,
                            contentDescription = "Ticket",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Fit
                        )

                        // Indicador de moderación
                        if (state.isModerating) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Moderando imagen...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        } else if (state.moderationStatus == "approved") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Imagen aprobada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }

                        // Botón para cambiar imagen
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5AC5C5)
                            )
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cambiar Imagen")
                        }
                    } else {
                        // Botón para seleccionar imagen
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5AC5C5)
                            )
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar Imagen")
                        }
                    }

                    // Error de imagen
                    state.imageError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Registrar
            Button(
                onClick = {
                    viewModel.registerExpense(
                        tripId = tripId,
                        userId = userId,
                        userName = userName
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading && state.hasCompleteData() && state.moderationStatus == "approved",
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5AC5C5)
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrando...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Gasto")
                }
            }
        }
    }
}