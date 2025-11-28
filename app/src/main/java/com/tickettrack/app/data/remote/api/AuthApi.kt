package com.tickettrack.app.data.remote.api

import com.tickettrack.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {

    @POST("auth/api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/api/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @DELETE("auth/api/admin/delete-account")
    suspend fun deleteOwnAccount(
        @Header("Authorization") token: String
    ): Response<Unit>

    // Eliminar un transportista específico (solo para admin)
    @DELETE("auth/api/users/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

}