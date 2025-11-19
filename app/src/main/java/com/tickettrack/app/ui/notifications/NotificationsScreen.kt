package com.tickettrack.app.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tickettrack.app.domain.model.AppNotification
import com.tickettrack.app.domain.model.NotificationType
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    token: String,
    onBackPressed: () -> Unit,
    onNavigateToTripDetails: (String) -> Unit,
    onNavigateToBudgetRequestDetails: (String) -> Unit,
    viewModel: NotificationViewModel
) {


    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showOnlyUnread by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    // Filtrar notificaciones según el toggle
    val displayedNotifications = if (showOnlyUnread) {
        notifications.filter { !it.isRead }
    } else {
        notifications
    }

    LaunchedEffect(Unit) {
        if (unreadCount > 0) {
            viewModel.markAllAsRead(token)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Notificaciones")
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount sin leer",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Toggle para mostrar solo no leídas
                    IconButton(
                        onClick = { showOnlyUnread = !showOnlyUnread }
                    ) {
                        Icon(
                            imageVector = if (showOnlyUnread) Icons.Default.FilterList else Icons.Default.FilterListOff,
                            contentDescription = if (showOnlyUnread) "Mostrar todas" else "Solo no leídas",
                            tint = if (showOnlyUnread) Primary else Color.Gray
                        )
                    }

                    // Marcar todas como leídas
                    if (unreadCount > 0) {
                        IconButton(
                            onClick = { viewModel.markAllAsRead(token) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Marcar todas como leídas",
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )
            } else if (displayedNotifications.isEmpty()) {
                EmptyNotificationsState(showOnlyUnread = showOnlyUnread)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = displayedNotifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                // Marcar como leída
                                if (!notification.isRead) {
                                    viewModel.markAsRead(notification.id)
                                }

                                // Navegar según el tipo
                                when (notification.type) {
                                    NotificationType.TRIP_ASSIGNED -> {
                                        onNavigateToTripDetails(notification.relatedId)
                                    }
                                    NotificationType.BUDGET_REQUESTED,
                                    NotificationType.BUDGET_APPROVED,
                                    NotificationType.BUDGET_REJECTED -> {
                                        onNavigateToBudgetRequestDetails(notification.relatedId)
                                    }
                                    NotificationType.GENERAL -> {
                                        // Abrir simplemente la pantalla de notificaciones
                                        // o una vista genérica
                                        // Por ahora, no navegamos a ningún detalle
                                    }
                                }
                            },
                            onDelete = {
                                showDeleteDialog = notification.id
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar notificación") },
            text = { Text("¿Estás seguro de que deseas eliminar esta notificación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNotification(showDeleteDialog!!)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Primary.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono según tipo de notificación
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(notification.createdAt.toDate()),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // Botón de eliminar
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsState(showOnlyUnread: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (showOnlyUnread) "No tienes notificaciones sin leer" else "No tienes notificaciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (showOnlyUnread) "Cambia el filtro para ver todas" else "Te notificaremos cuando haya novedades",
                fontSize = 14.sp,
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

// Funciones auxiliares
fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.TRIP_ASSIGNED -> Icons.Default.LocalShipping
        NotificationType.BUDGET_REQUESTED -> Icons.Default.RequestQuote
        NotificationType.BUDGET_APPROVED -> Icons.Default.CheckCircle
        NotificationType.BUDGET_REJECTED -> Icons.Default.Cancel
        NotificationType.GENERAL -> Icons.Default.Notifications
    }
}

fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.TRIP_ASSIGNED -> Color(0xFF2196F3) // Azul
        NotificationType.BUDGET_REQUESTED -> Color(0xFFFF9800) // Naranja
        NotificationType.BUDGET_APPROVED -> Color(0xFF4CAF50) // Verde
        NotificationType.BUDGET_REJECTED -> Color(0xFFF44336) // Rojo
        NotificationType.GENERAL -> Color(0xFF9C27B0)
    }
}

fun formatTimestamp(date: Date): String {
    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 60000 -> "Ahora"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> "${diff / 86400000}d"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
}