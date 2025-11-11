package com.tickettrack.app.domain.model

data class UserProfile(
    val name: String,
    val email: String,
    val token: String,
    val role: String,
    val companyEmail: String,
    val companyName: String, // AGREGAR ESTO
    val profileImageUrl: String?
)