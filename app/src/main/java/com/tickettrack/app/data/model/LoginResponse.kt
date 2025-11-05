// data/model/LoginResponse.kt
package com.tickettrack.app.data.model

import com.google.gson.annotations.SerializedName


data class LoginResponse(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("token")
    val token: String,

    @SerializedName("loginAt")
    val loginAt: String,

    @SerializedName("expiresAt")
    val expiresAt: String,

    @SerializedName("claims")
    val claims: Claims,

    @SerializedName("profileFilePath")
    val profileFilePath: String? = null,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null
)

data class Claims(
    @SerializedName("role")
    val role: String,
    @SerializedName("companyEmail")
    val companyEmail: String?,
    @SerializedName("companyName")
    val companyName: String?
)