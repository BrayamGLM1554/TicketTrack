package com.tickettrack.app.domain.repository

import com.tickettrack.app.domain.model.AuthResult

interface IAuthRepository {
    suspend fun login(email: String, password: String): AuthResult

    suspend fun register(
        companyName: String,
        rfc: String,
        officePhone: String,
        companyEmail: String,
        nameOfManager: String,
        curp: String,
        workPhone: String,
        personalEmail: String,
        password: String
    ): AuthResult
}