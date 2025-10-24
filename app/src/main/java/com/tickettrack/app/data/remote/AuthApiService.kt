package com.tickettrack.app.data.remote

import com.tickettrack.app.data.model.LoginRequest
import com.tickettrack.app.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}