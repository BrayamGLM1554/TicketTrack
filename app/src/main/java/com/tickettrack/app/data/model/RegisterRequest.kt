package com.tickettrack.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("CompanyName")
    val companyName: String,

    @SerialName("RFC")
    val rfc: String,

    @SerialName("OfficePhone")
    val officePhone: String,

    @SerialName("CompanyEmail")
    val companyEmail: String,

    @SerialName("NameOfManager")
    val nameOfManager: String,

    @SerialName("CURP")
    val curp: String,

    @SerialName("WorkPhone")
    val workPhone: String,

    @SerialName("PersonalEmail")
    val personalEmail: String,

    @SerialName("Password")
    val password: String
)