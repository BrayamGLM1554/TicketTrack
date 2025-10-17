package com.tickettrack.app.data.repository

import com.tickettrack.app.data.model.LoginRequest
import com.tickettrack.app.data.model.LoginResponse

class AuthRepository {

    suspend fun login(request: LoginRequest): LoginResponse {
        // Space reserved for future functionality
        // Example future implementation:
        // val response = httpClient.post("https://api-gateway-tickettrack/login") {
        //     contentType(ContentType.Application.Json)
        //     setBody(request)
        // }
        // return response.body()
        throw NotImplementedError("Login API call not yet implemented")
    }
}
