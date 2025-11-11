package com.tickettrack.app.domain.model

sealed class GoogleAuthResult {
    data class Success(
        val token: String,
        val uid: String,
        val name: String,
        val email: String,
        val needsCompanyData: Boolean, // Si es true, navegar a completar datos
        val role: String,
        val companyEmail: String,
        val profileImageUrl: String?
    ) : GoogleAuthResult()

    data class Error(val message: String) : GoogleAuthResult()
}