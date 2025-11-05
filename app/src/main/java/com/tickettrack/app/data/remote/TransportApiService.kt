package com.tickettrack.app.data.remote

import com.tickettrack.app.data.model.driver.RegisterUserRequest
import com.tickettrack.app.data.model.driver.RegisterUserResponse
import com.tickettrack.app.data.model.driver.TransportCompanyResponse
import com.tickettrack.app.data.model.driver.TransportUserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service para operaciones de Transportistas
 * Base URL: http://apigatewayticket.somee.com/
 */
interface TransportApiService {

    /**
     * GET /api/Transport
     * Obtiene todas las compañías con sus transportistas
     *
     * Requiere autenticación: Bearer token
     */
    @GET("api/Transport")
    suspend fun getAllTransport(
        @Header("Authorization") token: String
    ): Response<List<TransportCompanyResponse>>

    /**
     * GET /api/Transport/{uid}
     * Obtiene un transportista específico por su UID
     *
     * Requiere autenticación: Bearer token
     */
    @GET("api/Transport/{uid}")
    suspend fun getTransportByUid(
        @Path("uid") uid: String,
        @Header("Authorization") token: String
    ): Response<TransportUserResponse>

    /**
     * POST /auth/api/register-user
     * Registra un nuevo transportista (USER)
     *
     * IMPORTANTE: Este endpoint espera multipart/form-data, NO JSON
     * Base URL: https://tickettrakedauth.runasp.net
     */
    @Multipart
    @POST("auth/api/register-user")
    suspend fun registerUser(
        @Part("FullName") fullName: RequestBody,
        @Part("LicenseNumber") licenseNumber: RequestBody,
        @Part("PersonalPhone") personalPhone: RequestBody,
        @Part("Email") email: RequestBody,
        @Part("Password") password: RequestBody,
        @Part("CompanyName") companyName: RequestBody,
        @Part licenseImage: MultipartBody.Part?, // Imagen de licencia (opcional)
        @Part profileImage: MultipartBody.Part?, // Foto de perfil (opcional)
        @Header("Authorization") token: String
    ): Response<RegisterUserResponse>

    // ==========================================
    // TODO: Endpoints pendientes
    // ==========================================

    /**
     * PUT /api/Transport/{uid}
     * Actualiza información de un transportista
     *
     * TODO: Implementar cuando se defina el contrato de la API
     */
    // @PUT("api/Transport/{uid}")
    // suspend fun updateTransport(
    //     @Path("uid") uid: String,
    //     @Body request: UpdateTransportRequest,
    //     @Header("Authorization") token: String
    // ): Response<TransportUserResponse>

    /**
     * DELETE /api/Transport/{uid}
     * Elimina un transportista
     *
     * TODO: Implementar cuando se defina el contrato de la API
     */
    // @DELETE("api/Transport/{uid}")
    // suspend fun deleteTransport(
    //     @Path("uid") uid: String,
    //     @Header("Authorization") token: String
    // ): Response<Unit>
}