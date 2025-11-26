package com.tickettrack.app.domain.model

import androidx.compose.runtime.saveable.Saver

data class UserProfile(
    val name: String,
    val email: String,
    val token: String,
    val role: String,
    val companyEmail: String,
    val companyName: String,
    val profileImageUrl: String?,
) {
    companion object {
        val Saver: Saver<UserProfile?, String> = Saver(
            save = { profile ->
                profile?.let {
                    // Usar un separador que NO aparezca en los datos
                    "${it.name}|||${it.email}|||${it.token}|||${it.role}|||${it.companyEmail}|||${it.companyName}|||${it.profileImageUrl.orEmpty()}"
                }
            },
            restore = { savedString ->
                savedString?.split("|||")?.let { parts ->
                    if (parts.size == 7) {
                        UserProfile(
                            name = parts[0],
                            email = parts[1],
                            token = parts[2],
                            role = parts[3],
                            companyEmail = parts[4],
                            companyName = parts[5],
                            profileImageUrl = parts[6].ifEmpty { null }
                        )
                    } else null
                }
            }
        )
    }
}