package com.tickettrack.app.domain.repository

import com.tickettrack.app.domain.model.*

interface IDriversRepository {

    suspend fun getDrivers(
        token: String,
        page: Int,
        limit: Int,
        search: String?
    ): Result<DriversResponse>

    suspend fun createDriver(
        token: String,
        request: CreateDriverRequest
    ): Result<CreateDriverResponse>

    suspend fun getDriverDetails(
        token: String,
        uid: String
    ): Result<DriverDetailsResponse>

    suspend fun updateDriver(
        token: String,
        uid: String,
        request: UpdateDriverRequest
    ): Result<UpdateDriverResponse>

    /**
     * Elimina un transportista por su UID usando AuthApi
     * @param token Token de autorización Bearer
     * @param uid UUID del transportista a eliminar
     */
    suspend fun deleteDriver(
        token: String,
        uid: String
    ): Result<Unit>
}