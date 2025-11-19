package com.tickettrack.app.ui.notifications

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.tickettrack.app.data.repository.NotificationRepository
import com.tickettrack.app.domain.model.AppNotification
import com.tickettrack.app.utils.JwtDecoder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _newNotification = MutableStateFlow<AppNotification?>(null)
    val newNotification: StateFlow<AppNotification?> = _newNotification.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Inicia todos los listeners de notificaciones
     */
    fun startListening(token: String) {
        val role = JwtDecoder.extractRole(token) ?: return

        // Escuchar notificaciones existentes
        viewModelScope.launch {
            notificationRepository.observeNotifications(token)
                .collect { notifications ->
                    _notifications.value = notifications
                    _unreadCount.value = notifications.count { !it.isRead }
                }
        }

        // Listeners específicos por rol
        if (role == "USER") {
            // Escuchar nuevos viajes asignados
            viewModelScope.launch {
                notificationRepository.observeNewTripsForUser(token)
                    .filterNotNull()
                    .collect { notification ->
                        _newNotification.value = notification
                    }
            }

            // Escuchar cambios de estado en peticiones de presupuesto
            viewModelScope.launch {
                notificationRepository.observeBudgetRequestStatusForUser(token)
                    .filterNotNull()
                    .collect { notification ->
                        _newNotification.value = notification
                    }
            }
        }
        else if (role == "ADMIN") {
            viewModelScope.launch {
                val userProfile = notificationRepository.getUserProfile()
                if (userProfile != null) {
                    notificationRepository.observeNewBudgetRequestsForAdmin(
                        userProfile,
                        FirebaseFirestore.getInstance()
                    ).collect { notification ->
                        _newNotification.value = notification
                    }
                } else {
                    Log.e(TAG, "❌ No se pudo obtener UserProfile para ADMIN")
                }
            }
        }
    }

    /**
     * Marca una notificación como leída
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    fun markAllAsRead(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            notificationRepository.markAllAsRead(token)
            _isLoading.value = false
        }
    }

    /**
     * Elimina una notificación
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }

    /**
     * Limpia la notificación nueva (después de mostrarla)
     */
    fun clearNewNotification() {
        _newNotification.value = null
    }

    /**
     * Obtiene notificaciones no leídas
     */
    val unreadNotifications: StateFlow<List<AppNotification>> = notifications.map { list ->
        list.filter { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Obtiene notificaciones leídas
     */
    val readNotifications: StateFlow<List<AppNotification>> = notifications.map { list ->
        list.filter { it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveFcmToken(userId: String, token: String) {
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                Log.d("FCM", "✅ Token guardado correctamente")
            } catch (e: Exception) {
                Log.e("FCM", "❌ Error al guardar token: ${e.message}")
            }
        }
    }
}