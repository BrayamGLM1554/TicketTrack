package com.tickettrack.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tickettrack.app.MainActivity
import com.tickettrack.app.R
import com.tickettrack.app.data.local.SessionManager
import com.tickettrack.app.utils.JwtDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "ticket_track_notifications"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "📩 Mensaje recibido de: ${remoteMessage.from}")

        // Si tiene notificación, mostrarla
        remoteMessage.notification?.let {
            Log.d(TAG, "Título: ${it.title}")
            Log.d(TAG, "Mensaje: ${it.body}")
            showNotification(
                title = it.title ?: "TicketTrack",
                body = it.body ?: "",
                data = remoteMessage.data
            )
        }

        // Si solo tiene data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = getSystemService(NotificationManager::class.java)

        // Crear canal de notificaciones (Android 8+)
        createNotificationChannel(notificationManager)

        // Intent para abrir la app al hacer click
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pasar datos para navegación
            putExtra("notification_type", data["type"])
            putExtra("relatedId", data["relatedId"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construir notificación
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Necesitas crear este icono
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        // Mostrar notificación
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "✅ Notificación mostrada con ID: $notificationId")
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones de TicketTrack",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de viajes, presupuestos y gastos"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Aquí puedes procesar data adicional si es necesario
        val type = data["type"]
        val relatedId = data["relatedId"]
        Log.d(TAG, "Tipo: $type, ID relacionado: $relatedId")
    }

    private suspend fun saveTokenToFirestore(token: String) {
        val sessionManager = SessionManager(applicationContext)
        val session = sessionManager.getSession() ?: return

        val jwt = session.token ?: return

        val userId = JwtDecoder.extractUid(jwt) ?: return
        val role = session.role  // ← USAR EL ROLE GUARDADO EN DATASTORE

        val collection = if (role.uppercase() == "ADMIN") "admins" else "users"

        FirebaseFirestore.getInstance()
            .collection(collection)
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FCMService", "🔥 Token guardado en Firestore en $collection/$userId")
            }
            .addOnFailureListener {
                Log.e("FCMService", "❌ Error guardando token", it)
            }
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        CoroutineScope(Dispatchers.IO).launch {
            saveTokenToFirestore(token)
        }
    }




}