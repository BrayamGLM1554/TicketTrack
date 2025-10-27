package com.tickettrack.app.ui.expenses.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tickettrack.app.ui.expenses.budget.components.BudgetRequestCard

/**
 * Pantalla de lista de solicitudes de presupuesto
 * HU20 - Ver y gestionar solicitudes de aumento de presupuesto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetRequestListScreen(
    userEmail: String,
    userName: String,
    userRole: String,
    companyEmail: String,
    onNavigateBack: () -> Unit,
    viewModel: BudgetRequestViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isConsignatario = userRole == "admin" || userRole == "consignatario"

    // Cargar solicitudes al iniciar
    LaunchedEffect(Unit) {
        if (isConsignatario) {
            viewModel.loadPendingRequests(companyEmail)
        } else {
            viewModel.loadUserRequests(userEmail)
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

    // Diálogo de revisión (aprobar/rechazar)
    state.requestToReview?.let { request ->
        AlertDialog(
            onDismissRequest = { viewModel.hideReviewDialog() },
            title = {
                Text(
                    if (state.isApproving) "Aprobar Solicitud" else "Rechazar Solicitud"
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Resumen de la solicitud
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Viaje: ${request.tripNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Aumento: ${request.getFormattedIncrease()} (${request.getFormattedPercentage()})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Solicitado por: ${request.requestedByName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    // Campo de nota
                    OutlinedTextField(
                        value = state.reviewNote,
                        onValueChange = { viewModel.onReviewNoteChanged(it) },
                        label = {
                            Text(
                                if (state.isApproving) "Comentario (opcional)" else "Motivo del rechazo *"
                            )
                        },
                        isError = state.reviewNoteError != null,
                        supportingText = state.reviewNoteError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (state.isApproving) {
                            viewModel.approveRequest(
                                requestId = request.id,
                                reviewNote = state.reviewNote,
                                reviewedBy = userEmail,
                                reviewedByName = userName,
                                companyEmail = companyEmail
                            )
                        } else {
                            viewModel.rejectRequest(
                                requestId = request.id,
                                reviewNote = state.reviewNote,
                                reviewedBy = userEmail,
                                reviewedByName = userName,
                                companyEmail = companyEmail
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isApproving) {
                            Color(0xFF4CAF50)
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    ),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (state.isApproving) "Aprobar" else "Rechazar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideReviewDialog() },
                    enabled = !state.isLoading
                ) {
                    Text("Cancelar")
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
                            if (isConsignatario) "Solicitudes de Presupuesto" else "Mis Solicitudes"
                        )
                        if (isConsignatario && state.pendingCount > 0) {
                            Text(
                                text = "${state.pendingCount} pendiente${if (state.pendingCount > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5AC5C5),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(state.isRefreshing),
            onRefresh = {
                viewModel.refreshRequests(
                    if (isConsignatario) companyEmail else userEmail,
                    isConsignatario
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF5AC5C5))
                }
            } else if (state.requests.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.RequestPage,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = if (isConsignatario) {
                                "No hay solicitudes pendientes"
                            } else {
                                "No has hecho solicitudes"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Encabezado con información
                    if (isConsignatario) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF5AC5C5).copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF5AC5C5)
                                    )
                                    Column {
                                        Text(
                                            text = "Revisa y gestiona las solicitudes",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Toca una solicitud para ver detalles y aprobar/rechazar",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Lista de solicitudes
                    items(state.requests) { request ->
                        BudgetRequestCard(
                            request = request,
                            isExpanded = state.expandedRequestId == request.id,
                            onToggleExpand = { viewModel.toggleRequestExpansion(request.id) },
                            onApprove = { viewModel.showApproveDialog(request) },
                            onReject = { viewModel.showRejectDialog(request) },
                            canReview = isConsignatario && request.isPending()
                        )
                    }
                }
            }
        }
    }
}