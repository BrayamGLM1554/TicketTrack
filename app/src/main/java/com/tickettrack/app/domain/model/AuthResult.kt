package com.tickettrack.app.domain.model

sealed class AuthResult {
    data class Success(
        val token: String,
        val name: String,
        val email: String,
        val role: String,
        val companyEmail: String,
        val companyName: String, // AGREGAR ESTO
        val profileImageUrl: String?
    ) : AuthResult()

    data class Error(val message: String) : AuthResult()
}