package com.tickettrack.app.data.repository

import com.tickettrack.app.data.remote.api.AuthApi
import com.tickettrack.app.data.remote.api.DriversApi
import com.tickettrack.app.domain.model.CreateDriverRequest
import com.tickettrack.app.domain.model.CreateDriverResponse
import com.tickettrack.app.domain.model.DriverDetailsResponse
import com.tickettrack.app.domain.model.DriversResponse
import com.tickettrack.app.domain.model.UpdateDriverRequest
import com.tickettrack.app.domain.model.UpdateDriverResponse
import com.tickettrack.app.domain.repository.IDriversRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class DriversRepository(
    private val driversApi: DriversApi,
    private val authApi: AuthApi // Agregamos AuthApi para eliminación
) : IDriversRepository {

    override suspend fun getDrivers(
        token: String,
        page: Int,
        limit: Int,
        search: String?
    ): Result<DriversResponse> {
        return try {
            val response = driversApi.getDrivers(
                token = "Bearer $token",
                page = page,
                limit = limit,
                search = search
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDriver(
        token: String,
        request: CreateDriverRequest
    ): Result<CreateDriverResponse> {
        return try {
            val response = driversApi.createDriver(
                token = "Bearer $token",
                fullName = request.fullName.toRequestBody("text/plain".toMediaTypeOrNull()),
                email = request.email.toRequestBody("text/plain".toMediaTypeOrNull()),
                password = request.password.toRequestBody("text/plain".toMediaTypeOrNull()),
                phone = request.personalPhoneEncrypted.toRequestBody("text/plain".toMediaTypeOrNull()),
                licenseNumber = request.licenseNumber.toRequestBody("text/plain".toMediaTypeOrNull()),
                companyName = request.companyName.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDriverDetails(
        token: String,
        uid: String
    ): Result<DriverDetailsResponse> {
        return try {
            val response = driversApi.getDriverDetails(
                token = "Bearer $token",
                uid = uid
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDriver(
        token: String,
        uid: String,
        request: UpdateDriverRequest
    ): Result<UpdateDriverResponse> {
        return try {
            val response = driversApi.updateDriver(
                token = "Bearer $token",
                uid = uid,
                fullName = request.fullName?.toRequestBody("text/plain".toMediaTypeOrNull()),
                phone = request.personalPhoneEncrypted?.toRequestBody("text/plain".toMediaTypeOrNull()),
                licenseNumber = request.licenseNumber?.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un transportista usando AuthApi
     * DELETE https://tickettrakedauth.runasp.net/auth/api/users/{userId}
     */
    override suspend fun deleteDriver(
        token: String,
        uid: String
    ): Result<Unit> {
        return try {
            val response = authApi.deleteUser(
                userId = uid,
                token = "Bearer $token"
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "No autorizado. Inicia sesión nuevamente."
                    403 -> "No tienes permisos para eliminar este transportista."
                    404 -> "Transportista no encontrado."
                    else -> "Error al eliminar: ${response.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}