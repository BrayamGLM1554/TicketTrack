package com.tickettrack.app.ui.profile

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.tickettrack.app.data.local.SessionManager
import com.tickettrack.app.data.remote.api.AuthApi
import com.tickettrack.app.domain.model.UserProfile
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onBackPressed: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit,
    authApi: AuthApi = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }

    // Configurar Google Sign-In Client para logout
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("119196505193-gctg8tk4g77dk7nojf0sjfn4s24vva72.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Función para eliminar cuenta
    fun deleteAccount() {
        scope.launch {
            isDeleting = true
            deleteErrorMessage = null

            try {
                val response = authApi.deleteOwnAccount("Bearer ${userProfile.token}")

                if (response.isSuccessful) {
                    // Cerrar sesión de Google
                    googleSignInClient.signOut().addOnCompleteListener {
                        scope.launch {
                            // Limpiar sesión local
                            sessionManager.clearSession()
                            showDeleteConfirmDialog = false
                            showDeleteAccountDialog = false
                            onLogout()
                        }
                    }
                } else {
                    deleteErrorMessage = when (response.code()) {
                        400 -> "La solicitud no es válida. Por favor, intenta de nuevo."
                        401 -> "Tu sesión ha expirado. Por favor, inicia sesión nuevamente."
                        403 -> "No tienes permisos para realizar esta acción."
                        404 -> "No se pudo encontrar tu cuenta."
                        500 -> "Ocurrió un error en el servidor. Por favor, intenta más tarde."
                        else -> "No se pudo eliminar la cuenta. Intenta de nuevo."
                    }
                }
            } catch (e: java.net.UnknownHostException) {
                deleteErrorMessage = "No hay conexión a internet. Verifica tu conexión e intenta de nuevo."
            } catch (e: java.net.SocketTimeoutException) {
                deleteErrorMessage = "La conexión tardó demasiado. Por favor, intenta de nuevo."
            } catch (e: java.io.IOException) {
                deleteErrorMessage = "Error de conexión. Verifica tu internet e intenta nuevamente."
            } catch (e: Exception) {
                deleteErrorMessage = "Ocurrió un error inesperado. Por favor, intenta más tarde."
            } finally {
                isDeleting = false
            }
        }
    }

    // Diálogo de confirmación de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            showLogoutDialog = false
                            onLogout()
                        }
                    }
                ) {
                    Text("Cerrar Sesión", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Primer diálogo: Advertencia sobre eliminar cuenta
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteAccountDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Eliminar Cuenta",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Esta acción es irreversible y tendrá las siguientes consecuencias:",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• Se eliminará toda tu información personal")
                    Text("• Perderás acceso a todos tus datos")
                    Text("• No podrás recuperar tu cuenta")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "¿Estás seguro de que deseas continuar?",
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF5350)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        showDeleteConfirmDialog = true
                    },
                    enabled = !isDeleting
                ) {
                    Text("Continuar", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    enabled = !isDeleting
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Segundo diálogo: Confirmación final
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Confirmación Final",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Column {
                    Text(
                        "Esta es tu última oportunidad para cancelar.",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Una vez eliminada, tu cuenta no podrá ser recuperada bajo ninguna circunstancia.",
                        color = Color(0xFF666666)
                    )

                    if (deleteErrorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = deleteErrorMessage!!,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { deleteAccount() },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isDeleting) "Eliminando..." else "Eliminar Mi Cuenta")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        deleteErrorMessage = null
                    },
                    enabled = !isDeleting
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
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
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
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
                    if (!userProfile.profileImageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = userProfile.profileImageUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                                color = Primary,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userProfile.name,
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
                            text = userProfile.role,
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información del usuario
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

                    ProfileInfoItem(
                        icon = Icons.Default.Person,
                        label = "Nombre completo",
                        value = userProfile.name
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    ProfileInfoItem(
                        icon = Icons.Default.Email,
                        label = "Correo electrónico",
                        value = userProfile.email
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    ProfileInfoItem(
                        icon = Icons.Default.Business,
                        label = if (userProfile.role == "ADMIN") "Correo de empresa" else "Compañía",
                        value = if (userProfile.role == "ADMIN") userProfile.companyEmail else userProfile.companyName
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Opciones adicionales
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configuración",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileActionItem(
                        icon = Icons.Default.Lock,
                        text = "Cambiar contraseña",
                        onClick = { /* TODO */ }
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    ProfileActionItem(
                        icon = Icons.Default.Notifications,
                        text = "Notificaciones",
                        onClick = onNavigateToNotifications
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    ProfileActionItem(
                        icon = Icons.Default.Help,
                        text = "Ayuda y soporte",
                        onClick = { /* TODO */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de cerrar sesión
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF5350)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar Sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de eliminar cuenta
            OutlinedButton(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFD32F2F)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F))
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Eliminar Mi Cuenta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileInfoItem(
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
fun ProfileActionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}