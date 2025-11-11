package com.tickettrack.app.domain.model

import com.google.gson.annotations.SerializedName

data class Driver(
    @SerializedName("Uid") val uid: String,
    @SerializedName("Role") val role: String,
    @SerializedName("FullName") val fullName: String,
    @SerializedName("Email") val email: String,
    @SerializedName("PersonalPhoneEncrypted") val personalPhoneEncrypted: String,
    @SerializedName("LicenseNumber") val licenseNumber: String,
    @SerializedName("LicenseFilePath") val licenseFilePath: String,
    @SerializedName("ProfileFilePath") val profileFilePath: String,
    @SerializedName("CompanyName") val companyName: String,
    @SerializedName("CreatedBy") val createdBy: String,
    @SerializedName("CreatedAt") val createdAt: String
)

data class DriversResponse(
    val success: Boolean,
    val data: List<Driver>,
    val pagination: Pagination
)

data class CreateDriverRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val personalPhoneEncrypted: String,
    val licenseNumber: String,
    val companyName: String
)

data class CreateDriverResponse(
    val success: Boolean,
    val message: String,
    val data: DriverCreated
)

data class DriverCreated(
    @SerializedName("Uid") val uid: String,
    @SerializedName("FullName") val fullName: String,
    @SerializedName("Email") val email: String,
    @SerializedName("CompanyName") val companyName: String
)

data class DriverDetailsResponse(
    val success: Boolean,
    val data: Driver
)

data class UpdateDriverRequest(
    val fullName: String?,
    val personalPhoneEncrypted: String?,
    val licenseNumber: String?
)

data class UpdateDriverResponse(
    val success: Boolean,
    val message: String,
    val data: DriverUpdated
)

data class DriverUpdated(
    @SerializedName("Uid") val uid: String,
    @SerializedName("FullName") val fullName: String
)