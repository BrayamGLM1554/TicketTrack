package com.tickettrack.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tickettrack.app.domain.model.UserProfile
import com.tickettrack.app.utils.JwtDecoder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_COMPANY_EMAIL = stringPreferencesKey("company_email")
        private val KEY_COMPANY_NAME = stringPreferencesKey("company_name")
        private val KEY_PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
    }

    /**
     * Guarda la sesión del usuario
     */
    suspend fun saveSession(userProfile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = userProfile.token
            preferences[KEY_NAME] = userProfile.name
            preferences[KEY_EMAIL] = userProfile.email
            preferences[KEY_ROLE] = userProfile.role
            preferences[KEY_COMPANY_EMAIL] = userProfile.companyEmail
            preferences[KEY_COMPANY_NAME] = userProfile.companyName
            preferences[KEY_PROFILE_IMAGE_URL] = userProfile.profileImageUrl ?: ""
        }
    }

    suspend fun getUserId(): String? {
        val preferences = context.dataStore.data.first()
        val token = preferences[KEY_TOKEN] ?: return null
        return JwtDecoder.extractUid(token)
    }


    /**
     * Obtiene la sesión guardada (si existe y no ha expirado)
     */
    suspend fun getSession(): UserProfile? {
        val preferences = context.dataStore.data.first()
        val token = preferences[KEY_TOKEN] ?: return null

        // Verificar si el token ha expirado
        if (JwtDecoder.isTokenExpired(token)) {
            clearSession()
            return null
        }

        return UserProfile(
            token = token,
            name = preferences[KEY_NAME] ?: "",
            email = preferences[KEY_EMAIL] ?: "",
            role = preferences[KEY_ROLE] ?: "",
            companyEmail = preferences[KEY_COMPANY_EMAIL] ?: "",
            companyName = preferences[KEY_COMPANY_NAME] ?: "",
            profileImageUrl = preferences[KEY_PROFILE_IMAGE_URL]?.takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Flow que emite la sesión actual
     */
    val sessionFlow: Flow<UserProfile?> = context.dataStore.data.map { preferences ->
        val token = preferences[KEY_TOKEN] ?: return@map null

        if (JwtDecoder.isTokenExpired(token)) {
            return@map null
        }

        UserProfile(
            token = token,
            name = preferences[KEY_NAME] ?: "",
            email = preferences[KEY_EMAIL] ?: "",
            role = preferences[KEY_ROLE] ?: "",
            companyEmail = preferences[KEY_COMPANY_EMAIL] ?: "",
            companyName = preferences[KEY_COMPANY_NAME] ?: "",
            profileImageUrl = preferences[KEY_PROFILE_IMAGE_URL]?.takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Limpia la sesión (logout)
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Verifica si hay sesión activa
     */
    suspend fun hasActiveSession(): Boolean {
        return getSession() != null
    }
}