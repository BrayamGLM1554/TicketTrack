package com.tickettrack.app.data.remote.dto

data class UpdateAdminRequest(
    val CompanyName: String,
    val Rfc: String,
    val OfficePhone: String,
    val NameOfManager: String,
    val Curp: String,
    val WorkPhone: String,
    val PersonalEmail: String,
    val ProfileImage: String? = null // Base64 si necesitas subir imagen
)

data class UpdateAdminResponse(
    val message: String?,
    val success: Boolean?,
    val admin: AdminData?
)

data class AdminData(
    val uid: String?,
    val companyName: String?,
    val companyEmail: String?,
    val nameOfManager: String?,
    val profileImageUrl: String?
)

// Para el login con Google
data class GoogleAuthRequest(
    val idToken: String, // Token de Google
    val email: String,
    val name: String,
    val photoUrl: String?
)

data class GoogleAuthResponse(
    val token: String?,
    val uid: String?,
    val email: String?,
    val name: String?,
    val isNewUser: Boolean, // Para saber si necesita completar datos
    val needsCompanyData: Boolean, // Flag que indica si necesita el PUT
    val claims: Claims?,
    val profileImageUrl: String?,
    val message: String?
)