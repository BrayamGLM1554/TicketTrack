package com.tickettrack.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de datos para la petición de registro al API Gateway.
 *
 * Los nombres de campos coinciden con el formato esperado por el backend.
 */
@Serializable
data class RegisterRequest(
    @SerialName("CompanyName")
    val companyName: String,

    @SerialName("Rfc")
    val rfc: String,

    @SerialName("OfficePhone")
    val officePhone: String,

    @SerialName("CompanyEmail")
    val companyEmail: String,

    @SerialName("NameOfManager")
    val nameOfManager: String,

    @SerialName("Curp")
    val curp: String,

    @SerialName("WorkPhone")
    val workPhone: String,

    @SerialName("PersonalEmail")
    val personalEmail: String,

    @SerialName("Password")
    val password: String
)