package com.tickettrack.app.data.model.driver

import com.google.gson.annotations.SerializedName

/**
 * Respuesta completa del endpoint GET /api/Transport
 * Contiene información de una compañía con su admin y users
 */
data class TransportCompanyResponse(
    @SerializedName("companyName")
    val companyName: String = "",

    @SerializedName("admin")
    val admin: AdminResponse = AdminResponse(),

    @SerializedName("users")
    val users: List<TransportUserResponse> = emptyList()
)

/**
 * Información del admin dentro de la respuesta
 */
data class AdminResponse(
    @SerializedName("uid")
    val uid: String = "",

    @SerializedName("companyEmail")
    val companyEmail: String = "",

    @SerializedName("companyName")
    val companyName: String = "",

    @SerializedName("personalEmail")
    val personalEmail: String = "",

    @SerializedName("role")
    val role: String = "",

    @SerializedName("createdAt")
    val createdAt: String = ""
)

/**
 * Información de un transportista (USER) dentro de la respuesta
 */
data class TransportUserResponse(
    @SerializedName("uid")
    val uid: String = "",

    @SerializedName("role")
    val role: String = "",

    @SerializedName("fullname")
    val fullname: String? = null,

    @SerializedName("email")
    val email: String = "",

    @SerializedName("personalPhone")
    val personalPhone: String? = null,

    @SerializedName("licenseNumber")
    val licenseNumber: String = "",

    @SerializedName("licenseFilePath")
    val licenseFilePath: String = "",

    @SerializedName("licensePublicUrl")
    val licensePublicUrl: String? = null,

    @SerializedName("companyName")
    val companyName: String = "",

    @SerializedName("createdBy")
    val createdBy: String = "",

    @SerializedName("createdAt")
    val createdAt: String = ""
)

/**
 * Request para registrar un nuevo transportista
 * POST /auth/api/register-user
 */
data class RegisterUserRequest(
    @SerializedName("FullName")
    val fullName: String,

    @SerializedName("LicenseNumber")
    val licenseNumber: String,

    @SerializedName("PersonalPhone")
    val personalPhone: String,

    @SerializedName("Email")
    val email: String,

    @SerializedName("Password")
    val password: String,

    @SerializedName("LicenseImageBase64")
    val licenseImageBase64: String = "", // Imagen en base64 (opcional por ahora)

    @SerializedName("CompanyName")
    val companyName: String
)

/**
 * Respuesta del registro de usuario
 */
data class RegisterUserResponse(
    @SerializedName("uid")
    val uid: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("message")
    val message: String = ""
)

// ==========================================
// EXTENSION FUNCTIONS: API → Domain
// ==========================================

/**
 * Convierte TransportUserResponse a Driver (modelo de dominio)
 */
fun TransportUserResponse.toDomain(): Driver {
    return Driver(
        id = this.uid,
        fullName = this.fullname ?: "Sin nombre",
        email = this.email,
        licenseNumber = this.licenseNumber,
        licenseCaducation = "", // No existe en API, dejarlo vacío
        personalPhoneEncrypted = this.personalPhone ?: "",
        createdAt = this.createdAt,
        createdBy = this.createdBy,
        companyName = this.companyName,
        isActive = this.role == "USER", // Si es USER, está activo
        unitNumber = "", // No existe en API, dejarlo vacío
        totalTrips = 0, // Se calculará después desde Trips
        trips = emptyList() // Se cargarán después desde Trips
    )
}

/**
 * Convierte una lista de TransportUserResponse a lista de Drivers
 */
fun List<TransportUserResponse>.toDomain(): List<Driver> {
    return this.map { it.toDomain() }
}