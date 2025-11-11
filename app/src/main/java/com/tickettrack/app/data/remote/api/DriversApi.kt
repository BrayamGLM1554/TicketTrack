package com.tickettrack.app.data.remote.api

import com.tickettrack.app.domain.model.CreateDriverResponse
import com.tickettrack.app.domain.model.DriverDetailsResponse
import com.tickettrack.app.domain.model.DriversResponse
import com.tickettrack.app.domain.model.UpdateDriverResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface DriversApi {

    @GET("api/drivers")
    suspend fun getDrivers(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): DriversResponse

    @GET("api/drivers/{uid}")
    suspend fun getDriverDetails(
        @Header("Authorization") token: String,
        @Path("uid") uid: String
    ): DriverDetailsResponse

    @Multipart
    @PUT("api/drivers/{uid}")
    suspend fun updateDriver(
        @Header("Authorization") token: String,
        @Path("uid") uid: String,
        @Part("FullName") fullName: RequestBody?,
        @Part("PersonalPhoneEncrypted") phone: RequestBody?,
        @Part("LicenseNumber") licenseNumber: RequestBody?
    ): UpdateDriverResponse

    @Multipart
    @POST("api/drivers")
    suspend fun createDriver(
        @Header("Authorization") token: String,
        @Part("FullName") fullName: RequestBody,
        @Part("Email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("PersonalPhoneEncrypted") phone: RequestBody,
        @Part("LicenseNumber") licenseNumber: RequestBody,
        @Part("CompanyName") companyName: RequestBody
    ): CreateDriverResponse
}