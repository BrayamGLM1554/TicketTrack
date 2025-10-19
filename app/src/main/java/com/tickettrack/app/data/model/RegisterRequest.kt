package com.tickettrack.app.data.model

/**
 * Modelo de datos para la petición de registro de usuario.
 *
 * Este modelo representa la estructura que se enviará al API Gateway
 * cuando se complete el proceso de registro.
 */
data class RegisterRequest(
    val company: CompanyData,
    val accountOwner: AccountOwnerData
)

data class CompanyData(
    val name: String,
    val rfc: String,
    val phone: String,
    val email: String
)

data class AccountOwnerData(
    val name: String,
    val curp: String,
    val phone: String,
    val email: String
)