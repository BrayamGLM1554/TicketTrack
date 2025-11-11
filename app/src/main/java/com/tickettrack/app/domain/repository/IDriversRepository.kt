package com.tickettrack.app.domain.repository

import com.tickettrack.app.domain.model.CreateDriverRequest
import com.tickettrack.app.domain.model.CreateDriverResponse
import com.tickettrack.app.domain.model.DriverDetailsResponse
import com.tickettrack.app.domain.model.DriversResponse
import com.tickettrack.app.domain.model.UpdateDriverRequest
import com.tickettrack.app.domain.model.UpdateDriverResponse

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
}