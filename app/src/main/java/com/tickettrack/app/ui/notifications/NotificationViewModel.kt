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
import com.google.firebase.Timestamp // Importación necesaria para el ordenamiento

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
                    // El listener principal actualiza la lista y el contador
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
                        // 1. Mostrar Popup
                        _newNotification.value = notification

                        // 2. Forzar actualización inmediata de la lista y el contador (FIX)
                        // Esto garantiza que el badge y la lista se actualicen inmediatamente sin duplicación en Firestore.
                        forceUpdateNotificationList(notification)
                    }
            }

            // Escuchar cambios de estado en peticiones de presupuesto
            viewModelScope.launch {
                notificationRepository.observeBudgetRequestStatusForUser(token)
                    .filterNotNull()
                    .collect { notification ->
                        // 1. Mostrar Popup
                        _newNotification.value = notification

                        // 2. Forzar actualización inmediata de la lista y el contador (FIX)
                        forceUpdateNotificationList(notification)
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
                        // El listener del Admin ya guarda en Firestore, así que el listener principal lo recoge
                    }
                } else {
                    Log.e(TAG, "❌ No se pudo obtener UserProfile para ADMIN")
                }
            }
        }
    }

    /**
     * Función auxiliar para forzar la actualización de la lista sin duplicar.
     * Añade la notificación localmente. El listener principal la consolidará más tarde.
     */
    private fun forceUpdateNotificationList(notification: AppNotification) {
        val currentList = _notifications.value.toMutableList()

        // Evitar agregar si ya está presente por algún motivo (ej. latencia de Firestore)
        if (currentList.none { it.relatedId == notification.relatedId && it.type == notification.type }) {

            // Creamos una copia para asegurar que sea inmutable y la marcamos como no leída
            val notificationToAdd = notification.copy(isRead = false)
            currentList.add(notificationToAdd)

            // Ordenar por fecha de creación (más reciente primero)
            val sortedList = currentList.sortedByDescending {
                it.createdAt.toDate()
            }

            _notifications.value = sortedList
            _unreadCount.value = sortedList.count { !it.isRead }
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