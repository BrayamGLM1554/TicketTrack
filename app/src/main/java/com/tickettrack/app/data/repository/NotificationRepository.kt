package com.tickettrack.app.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.tickettrack.app.data.local.SessionManager
import com.tickettrack.app.domain.model.AppNotification
import com.tickettrack.app.domain.model.NotificationType
import com.tickettrack.app.domain.model.UserProfile
import com.tickettrack.app.utils.JwtDecoder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NotificationRepository(
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager
) {
    private val notificationsCollection = firestore.collection("notifications")

    // Cliente HTTP para llamar a Vercel
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // ✅ URL de tu endpoint de Vercel
    private val vercelEndpoint = "https://notification-service-neon.vercel.app/send-push"

    companion object {
        private const val TAG = "NotificationRepo"
        private const val COLLECTION_NOTIFICATIONS = "notifications"
        private const val COLLECTION_TRIPS = "trips"
        private const val COLLECTION_BUDGET_REQUESTS = "budget_requests"
    }

    // Set para rastrear IDs de viajes ya procesados
    private val processedTripIds = mutableSetOf<String>()

    // Set para rastrear IDs de peticiones ya procesadas
    private val processedBudgetIds = mutableSetOf<String>()

    /**
     * Listener de notificaciones en tiempo real para el usuario actual
     */
    fun observeNotifications(token: String): Flow<List<AppNotification>> = callbackFlow {
        val uid = JwtDecoder.extractUid(token)
        if (uid == null) {
            Log.e(TAG, "No se pudo extraer UID del token")
            close()
            return@callbackFlow
        }

        Log.d(TAG, "📱 Observando notificaciones para UID: $uid")

        val listener = firestore.collection(COLLECTION_NOTIFICATIONS)
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error al escuchar notificaciones", error)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        AppNotification.fromFirestore(doc.id, doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al parsear notificación ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "✅ ${notifications.size} notificaciones cargadas")
                trySend(notifications)
            }

        awaitClose {
            Log.d(TAG, "🔒 Cerrando listener de notificaciones")
            listener.remove()
        }
    }

    /**
     * Listener de nuevos viajes asignados (para USER) - SOLO PENDING
     */
    fun observeNewTripsForUser(token: String): Flow<AppNotification?> = callbackFlow {
        val uid = JwtDecoder.extractUid(token)
        if (uid == null) {
            Log.e(TAG, "❌ UID es null, no se puede observar viajes")
            close()
            return@callbackFlow
        }

        Log.d(TAG, "🚚 Observando NUEVOS viajes PENDING para UID: $uid")

        val listener = firestore.collection(COLLECTION_TRIPS)
            .whereEqualTo("TransportistaUid", uid)
            .whereEqualTo("Status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error al escuchar viajes", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val data = change.document.data
                        val tripId = data["Id"] as? String ?: change.document.id

                        // Evitar duplicados
                        if (processedTripIds.contains(tripId)) {
                            Log.d(TAG, "⏭️ Viaje $tripId ya procesado, ignorando")
                            return@forEach
                        }

                        processedTripIds.add(tripId)

                        val tripName = data["TripName"] as? String ?: "Sin nombre"
                        val origin = data["Origin"] as? String ?: ""
                        val destination = data["Destination"] as? String ?: ""

                        Log.d(TAG, "🆕 Nuevo viaje PENDING detectado: $tripName (ID: $tripId)")

                        // Crear notificación
                        val notification = AppNotification(
                            userId = uid,
                            type = NotificationType.TRIP_ASSIGNED,
                            title = "Nuevo viaje asignado",
                            message = "Se te ha asignado el viaje: $tripName",
                            relatedId = tripId,
                            createdAt = Timestamp.now(),
                            metadata = mapOf(
                                "tripName" to tripName,
                                "origin" to origin,
                                "destination" to destination
                            )
                        )

                        // Guardar en Firestore
                        saveNotification(notification)
                        trySend(notification)
                    }
                }
            }

        awaitClose {
            Log.d(TAG, "🔒 Cerrando listener de viajes")
            listener.remove()
        }
    }

    // En NotificationRepository
    suspend fun getUserProfile(): UserProfile? {
        return sessionManager.getSession()
    }
    /**
     * Listener de nuevas peticiones de presupuesto (para ADMIN)
     */
    fun observeNewBudgetRequestsForAdmin(userProfile: UserProfile, firestore: FirebaseFirestore): Flow<AppNotification?> = callbackFlow {
        val uid = JwtDecoder.extractUid(userProfile.token)
        if (uid.isNullOrEmpty()) {
            Log.e(TAG, "❌ UID es null o vacío para ADMIN")
            close()
            return@callbackFlow
        }

        val companyName = userProfile.companyName
        if (companyName.isNullOrEmpty()) {
            Log.e(TAG, "❌ Admin no tiene companyName configurado")
            close()
            return@callbackFlow
        }

        Log.d(TAG, "🏢 Observando peticiones de presupuesto PENDING para ADMIN companyName: $companyName")

        val listener = firestore.collection(COLLECTION_BUDGET_REQUESTS)
            .whereEqualTo("status", "pending")
            .whereEqualTo("companyName", companyName) // ahora seguro
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error al escuchar peticiones", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val requestId = doc.id

                        // Evitar duplicados
                        if (processedBudgetIds.contains(requestId)) return@forEach
                        processedBudgetIds.add(requestId)

                        val data = doc.data
                        val driverName = data["driverName"] as? String ?: "Transportista"
                        val increaseAmount = (data["increaseAmount"] as? Number)?.toDouble() ?: 0.0
                        val tripName = data["tripName"] as? String ?: "Sin nombre"

                        val notification = AppNotification(
                            userId = uid,
                            type = NotificationType.BUDGET_REQUESTED,
                            title = "Nueva petición de presupuesto",
                            message = "$driverName solicita $${String.format("%.2f", increaseAmount)} para $tripName",
                            relatedId = requestId,
                            createdAt = Timestamp.now(),
                            metadata = mapOf(
                                "driverName" to driverName,
                                "amount" to increaseAmount.toString(),
                                "tripName" to tripName
                            )
                        )

                        saveNotification(notification)
                        trySend(notification)
                    }
                }
            }

        awaitClose {
            Log.d(TAG, "🔒 Cerrando listener de peticiones de presupuesto")
            listener.remove()
        }
    }
    /**
     * Listener de cambios en peticiones de presupuesto (para USER)
     */
    fun observeBudgetRequestStatusForUser(token: String): Flow<AppNotification?> = callbackFlow {
        val uid = JwtDecoder.extractUid(token)
        if (uid == null) {
            Log.e(TAG, "❌ UID es null para USER")
            close()
            return@callbackFlow
        }

        Log.d(TAG, "👀 Observando cambios de status en peticiones de UID: $uid")

        // Set para rastrear peticiones ya notificadas
        val notifiedStatusChanges = mutableSetOf<String>()

        val listener = firestore.collection(COLLECTION_BUDGET_REQUESTS)
            .whereEqualTo("createdBy", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error al escuchar cambios de status", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                        val data = change.document.data
                        val status = data["status"] as? String ?: return@forEach
                        val requestId = data["id"] as? String ?: change.document.id

                        // Crear una clave única para esta notificación
                        val notificationKey = "$requestId-$status"

                        if (notifiedStatusChanges.contains(notificationKey)) {
                            Log.d(TAG, "⏭️ Cambio de status ya notificado: $notificationKey")
                            return@forEach
                        }

                        if (status == "approved" || status == "rejected") {
                            notifiedStatusChanges.add(notificationKey)

                            val tripName = data["tripName"] as? String ?: "Sin nombre"
                            val increaseAmount =
                                (data["increaseAmount"] as? Number)?.toDouble() ?: 0.0
                            val rejectionReason = data["rejectionReason"] as? String

                            Log.d(TAG, "✅ Petición $requestId cambió a: $status")

                            val notification = AppNotification(
                                userId = uid,
                                type = if (status == "approved") NotificationType.BUDGET_APPROVED else NotificationType.BUDGET_REJECTED,
                                title = if (status == "approved") "Petición aprobada" else "Petición rechazada",
                                message = if (status == "approved") {
                                    "Tu petición de $${
                                        String.format(
                                            "%.2f",
                                            increaseAmount
                                        )
                                    } ha sido aprobada"
                                } else {
                                    "Tu petición fue rechazada${rejectionReason?.let { ": $it" } ?: ""}"
                                },
                                relatedId = requestId,
                                createdAt = Timestamp.now(),
                                metadata = mapOf(
                                    "status" to status,
                                    "tripName" to tripName,
                                    "amount" to increaseAmount.toString()
                                ).plus(rejectionReason?.let { mapOf("reason" to it) } ?: emptyMap())
                            )

                            saveNotification(notification)
                            trySend(notification)
                        }
                    }
                }
            }

        awaitClose {
            Log.d(TAG, "🔒 Cerrando listener de cambios de status")
            listener.remove()
        }
    }

    /**
     * Guarda una notificación en Firestore
     */
    private fun saveNotification(notification: AppNotification) {
        firestore.collection(COLLECTION_NOTIFICATIONS)
            .add(notification.toMap())
            .addOnSuccessListener {
                Log.d(TAG, "💾 Notificación guardada: ${notification.title}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al guardar notificación", e)
            }
    }

    /**
     * Marca una notificación como leída
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true)
                .await()
            Log.d(TAG, "✅ Notificación $notificationId marcada como leída")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al marcar como leída", e)
            Result.failure(e)
        }
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    suspend fun markAllAsRead(token: String): Result<Unit> {
        val uid =
            JwtDecoder.extractUid(token) ?: return Result.failure(Exception("UID no encontrado"))

        return try {
            val snapshot = firestore.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            Log.d(TAG, "✅ ${snapshot.size()} notificaciones marcadas como leídas")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al marcar todas como leídas", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina una notificación
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .await()
            Log.d(TAG, "🗑️ Notificación $notificationId eliminada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar notificación", e)
            Result.failure(e)
        }
    }

    suspend fun createNotificationAndSendPush(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedId: String
    ) {
        try {
            // 1. Crear notificación en Firestore
            val notificationData = hashMapOf(
                "userId" to userId,
                "title" to title,
                "message" to message,
                "type" to type.name,
                "relatedId" to relatedId,
                "isRead" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )

            notificationsCollection.add(notificationData).await()
            Log.d("NotificationRepo", "✅ Notificación creada en Firestore")

            // 2. Enviar push notification via Vercel (en segundo plano)
            sendPushViaVercel(userId, title, message, type.name, relatedId)

        } catch (e: Exception) {
            Log.e("NotificationRepo", "❌ Error al crear notificación: ${e.message}")
        }
    }

    private fun sendPushViaVercel(
        userId: String,
        title: String,
        message: String,
        type: String,
        relatedId: String
    ) {
        // Ejecutar en thread separado
        Thread {
            try {
                Log.d("NotificationRepo", "📤 Enviando push a Vercel...")

                val json = JSONObject().apply {
                    put("userId", userId)
                    put("title", title)
                    put("message", message)
                    put("type", type)
                    put("relatedId", relatedId)
                }

                val body = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(vercelEndpoint)
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    Log.d("NotificationRepo", "✅ Push enviada via Vercel: $responseBody")
                } else {
                    Log.e("NotificationRepo", "❌ Error Vercel (${response.code}): $responseBody")
                }

                response.close()

            } catch (e: Exception) {
                Log.e("NotificationRepo", "❌ Error al llamar Vercel: ${e.message}", e)
            }
        }.start()
    }
}