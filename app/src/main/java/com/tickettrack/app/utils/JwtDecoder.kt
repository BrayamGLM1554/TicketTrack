package com.tickettrack.app.utils

import android.util.Base64
import org.json.JSONObject

/**
 * Utilidad para decodificar JWT sin dependencias externas
 */
object JwtDecoder {

    /**
     * Extrae el UID (sub) del token JWT
     */
    fun extractUid(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val json = JSONObject(payload)

            json.optString("sub", null)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extrae el rol del token JWT
     */
    fun extractRole(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val json = JSONObject(payload)

            json.optString("role", null)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extrae el email del token JWT
     */
    fun extractEmail(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val json = JSONObject(payload)

            json.optString("email", null)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verifica si el token ha expirado
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val json = JSONObject(payload)

            val exp = json.optLong("exp", 0)
            val currentTime = System.currentTimeMillis() / 1000

            exp < currentTime
        } catch (e: Exception) {
            true
        }
    }
}