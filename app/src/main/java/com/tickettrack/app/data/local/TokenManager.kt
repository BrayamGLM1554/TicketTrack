package com.tickettrack.app.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ticket_track_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_COMPANY_EMAIL = "company_email"
        private const val KEY_COMPANY_NAME = "company_name"  // ✅ NUEVO
        private const val KEY_EXPIRES_AT = "expires_at"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    // ✅ MODIFICADO: companyEmail ahora es nullable
    fun saveUserData(
        name: String,
        email: String,
        role: String,
        companyEmail: String?,  // ✅ Ahora puede ser null
        expiresAt: String
    ) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            putString(KEY_COMPANY_EMAIL, companyEmail)
            putString(KEY_EXPIRES_AT, expiresAt)
            apply()
        }
    }

    // ✅ NUEVO: Método adicional para guardar companyName (para USER)
    fun saveCompanyName(companyName: String?) {
        prefs.edit().putString(KEY_COMPANY_NAME, companyName).apply()
    }

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)
    fun getCompanyEmail(): String? = prefs.getString(KEY_COMPANY_EMAIL, null)

    // ✅ NUEVO: Obtener companyName
    fun getCompanyName(): String? = prefs.getString(KEY_COMPANY_NAME, null)

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}