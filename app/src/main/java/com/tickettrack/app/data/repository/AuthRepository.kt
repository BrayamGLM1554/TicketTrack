package com.tickettrack.app.data.repository

import android.util.Log
import com.tickettrack.app.data.remote.api.AuthApi
import com.tickettrack.app.data.remote.dto.LoginRequest
import com.tickettrack.app.data.remote.dto.RegisterRequest
import com.tickettrack.app.domain.model.AuthResult
import com.tickettrack.app.domain.repository.IAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val authApi: AuthApi
) : IAuthRepository {

    override suspend fun login(email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.token != null && body.name != null) {
                        Log.d("AuthRepository", "Login exitoso para: ${body.name}")
                        AuthResult.Success(
                            token = body.token,
                            name = body.name,
                            email = body.email ?: email,
                            role = body.claims?.role ?: "USER",
                            companyEmail = body.claims?.companyEmail ?: "",
                            companyName = body.companyName ?: body.claims?.companyName ?: "", // USAR companyName del response
                            profileImageUrl = body.profileImageUrl
                        )
                    } else {
                        Log.e("AuthRepository", "Login fallido - No se recibió token o nombre")
                        AuthResult.Error("Credenciales incorrectas")
                    }
                } else {
                    Log.e("AuthRepository", "Login fallido - Código: ${response.code()}")
                    AuthResult.Error("Error de autenticación. Intenta más tarde.")
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Excepción en login: ${e.message}", e)
                AuthResult.Error("Error de conexión. Verifica tu internet.")
            }
        }
    }

    override suspend fun register(
        companyName: String,
        rfc: String,
        officePhone: String,
        companyEmail: String,
        nameOfManager: String,
        curp: String,
        workPhone: String,
        personalEmail: String,
        password: String
    ): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(
                    CompanyName = companyName,
                    Rfc = rfc,
                    OfficePhone = officePhone,
                    CompanyEmail = companyEmail,
                    NameOfManager = nameOfManager,
                    Curp = curp,
                    WorkPhone = workPhone,
                    PersonalEmail = personalEmail,
                    Password = password
                )

                val response = authApi.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d("AuthRepository", "Registro exitoso: ${body.message}")
                    AuthResult.Success(
                        token = "",
                        name = nameOfManager,
                        email = companyEmail,
                        role = "USER",
                        companyEmail = companyEmail,
                        companyName = companyName, // USAR companyName del registro
                        profileImageUrl = null
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthRepository", "Error en registro: $errorBody")

                    if (errorBody?.contains("EMAIL_EXISTS") == true ||
                        errorBody?.contains("already exists") == true) {
                        AuthResult.Error("Este correo ya está registrado. Intenta con otro.")
                    } else {
                        AuthResult.Error("Error al registrar. Intenta más tarde.")
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Excepción en registro: ${e.message}", e)
                AuthResult.Error("Error de conexión. Verifica tu internet.")
            }
        }
    }
}