package com.tickettrack.app.data.remote.api

import com.tickettrack.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {

    @POST("auth/api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/api/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>


}