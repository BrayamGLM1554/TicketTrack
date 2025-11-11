package com.tickettrack.app.data.remote.dto

data class RegisterRequest(
    val CompanyName: String,
    val Rfc: String,
    val OfficePhone: String,
    val CompanyEmail: String,
    val NameOfManager: String,
    val Curp: String,
    val WorkPhone: String,
    val PersonalEmail: String,
    val Password: String
)