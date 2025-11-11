package com.tickettrack.app.data.remote.dto

data class LoginResponse(
    val name: String?,
    val email: String?,
    val token: String?,
    val loginAt: String?,
    val expiresAt: String?,
    val claims: Claims?,
    val companyName: String?, // Agregar aquí también
    val profileFilePath: String?,
    val profileImageUrl: String?,
    val message: String?
)

data class Claims(
    val role: String?,
    val companyEmail: String?,
    val companyName: String? // AGREGAR ESTO
)