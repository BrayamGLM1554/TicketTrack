package com.tickettrack.app.data.repository.driver

import android.util.Log
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.data.model.driver.Driver
import com.tickettrack.app.data.model.driver.toDomain
import com.tickettrack.app.data.remote.TransportApiService
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Repositorio para operaciones CRUD de Transportistas.
 *
 * Usa la API de Transport para obtener y gestionar transportistas.
 *
 * @param apiService Servicio de API para Transport
 * @param authRegisterService Servicio de API para registro (temporal)
 * @param tokenManager Gestor de tokens y datos del usuario
 */
class DriverRepository(
    private val apiService: TransportApiService,
    private val authRegisterService: TransportApiService,
    private val tokenManager: TokenManager
) {

    companion object {
        private const val TAG = "DriverRepository"
    }

    /**
     * Obtiene el nombre de la compañía desde el email del admin.
     * Hace una llamada a la API para obtener el nombre real.
     */
    private suspend fun getCompanyNameFromEmail(companyEmail: String): String? {
        return try {
            val token = tokenManager.getToken() ?: return null
            val response = apiService.getAllTransport("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val companies = response.body()!!
                val myCompany = companies.firstOrNull {
                    it.admin.companyEmail.equals(companyEmail, ignoreCase = true)
                }
                myCompany?.companyName
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting company name", e)
            null
        }
    }

    // ==========================================
    // GET - Obtener todos los transportistas
    // ==========================================

    /**
     * Obtiene todos los transportistas de la compañía del admin logueado.
     *
     * Lógica de filtrado:
     * 1. Obtiene todas las compañías desde la API
     * 2. Filtra por companyEmail del admin logueado
     * 3. Extrae solo los users con role "USER"
     * 4. Filtra por createdBy == companyEmail del admin
     */
    suspend fun getAllDrivers(): Result<List<Driver>> {
        return try {
            val token = tokenManager.getToken()
            val companyEmail = tokenManager.getCompanyEmail()

            if (token.isNullOrBlank()) {
                return Result.failure(Exception("Token no disponible. Inicia sesión nuevamente."))
            }

            if (companyEmail.isNullOrBlank()) {
                return Result.failure(Exception("Email de compañía no disponible."))
            }

            Log.d(TAG, "Fetching drivers for company: $companyEmail")

            val response = apiService.getAllTransport("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val companies = response.body()!!

                // Filtrar la compañía del admin logueado
                val myCompany = companies.firstOrNull {
                    it.admin.companyEmail.equals(companyEmail, ignoreCase = true)
                }

                if (myCompany == null) {
                    Log.w(TAG, "No se encontró la compañía del admin: $companyEmail")
                    return Result.success(emptyList())
                }

                // Filtrar users con role "USER" creados por este admin
                val drivers = myCompany.users
                    .filter { user ->
                        user.role == "USER" &&
                                user.createdBy.equals(companyEmail, ignoreCase = true)
                    }
                    .toDomain()

                Log.d(TAG, "Found ${drivers.size} drivers for company: ${myCompany.companyName}")

                return Result.success(drivers)

            } else {
                val error = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                return Result.failure(Exception(error))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching drivers", e)
            return Result.failure(e)
        }
    }

    // ==========================================
    // GET by ID - Obtener un transportista
    // ==========================================

    /**
     * Obtiene un transportista específico por su UID.
     */
    suspend fun getDriverById(uid: String): Result<Driver?> {
        return try {
            val token = tokenManager.getToken()

            if (token.isNullOrBlank()) {
                return Result.failure(Exception("Token no disponible. Inicia sesión nuevamente."))
            }

            Log.d(TAG, "Fetching driver by UID: $uid")

            val response = apiService.getTransportByUid(uid, "Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!

                // Verificar que sea un USER (no un ADMIN)
                if (user.role != "USER") {
                    Log.w(TAG, "UID $uid no es un transportista (role: ${user.role})")
                    return Result.success(null)
                }

                val driver = user.toDomain()
                Log.d(TAG, "Driver found: ${driver.fullName}")

                return Result.success(driver)

            } else if (response.code() == 404) {
                Log.d(TAG, "Driver not found: $uid")
                return Result.success(null)

            } else {
                val error = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                return Result.failure(Exception(error))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching driver by ID", e)
            return Result.failure(e)
        }
    }

    // ==========================================
    // POST - Registrar nuevo transportista
    // ==========================================

    /**
     * Registra un nuevo transportista en el sistema.
     *
     * NOTA: Usa el endpoint de AUTH hasta que esté disponible en API Gateway.
     *
     * @param fullName Nombre completo del transportista
     * @param email Email del transportista
     * @param phone Teléfono personal
     * @param licenseNumber Número de licencia
     * @param password Contraseña (generada o proporcionada)
     * @param licenseImageBase64 Imagen de la licencia en base64 (opcional)
     */
    suspend fun registerDriver(
        fullName: String,
        email: String,
        phone: String,
        licenseNumber: String,
        password: String,
        licenseImageBase64: String = ""
    ): Result<Driver> {
        return try {
            val token = tokenManager.getToken()
            val companyEmail = tokenManager.getCompanyEmail()

            if (token.isNullOrBlank()) {
                return Result.failure(Exception("Token no disponible. Inicia sesión nuevamente."))
            }

            if (companyEmail.isNullOrBlank()) {
                return Result.failure(Exception("Email de compañía no disponible."))
            }

            Log.d(TAG, "Registering new driver: $fullName")
            Log.d(TAG, "Company email: $companyEmail")

            // Obtener el nombre de la compañía desde la API de Transport
            val companyName = getCompanyNameFromEmail(companyEmail) ?: "Unknown"

            Log.d(TAG, "Using company name: $companyName")

            // Simular delay de red
            delay(1500)

            Log.d(TAG, "Sending request: FullName=$fullName, Email=$email, Phone=$phone, License=$licenseNumber, Company=$companyName")

            // Crear RequestBody para cada campo (multipart/form-data)
            val fullNameBody = fullName.toRequestBody("text/plain".toMediaTypeOrNull())
            val licenseNumberBody = licenseNumber.toRequestBody("text/plain".toMediaTypeOrNull())
            val phoneBody = phone.toRequestBody("text/plain".toMediaTypeOrNull())
            val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
            val passwordBody = password.toRequestBody("text/plain".toMediaTypeOrNull())
            val companyNameBody = companyName.toRequestBody("text/plain".toMediaTypeOrNull())

            // TODO: Implementar carga de imágenes cuando sea necesario
            // val licenseImagePart = ... (si tienes la imagen)
            // val profileImagePart = ... (si tienes la imagen)

            val response = authRegisterService.registerUser(
                fullName = fullNameBody,
                licenseNumber = licenseNumberBody,
                personalPhone = phoneBody,
                email = emailBody,
                password = passwordBody,
                companyName = companyNameBody,
                licenseImage = null, // TODO: Agregar cuando tengamos la imagen
                profileImage = null, // TODO: Agregar cuando tengamos la imagen
                token = "Bearer $token"
            )

            if (response.isSuccessful && response.body() != null) {
                val registerResponse = response.body()!!

                Log.d(TAG, "Driver registered successfully: ${registerResponse.uid}")

                // Crear objeto Driver con los datos registrados
                val driver = Driver(
                    id = registerResponse.uid,
                    fullName = fullName,
                    email = email,
                    licenseNumber = licenseNumber,
                    licenseCaducation = "",
                    personalPhoneEncrypted = phone,
                    createdAt = java.time.Instant.now().toString(),
                    createdBy = tokenManager.getCompanyEmail() ?: "",
                    companyName = companyName,
                    isActive = true,
                    unitNumber = "",
                    totalTrips = 0,
                    trips = emptyList()
                )

                return Result.success(driver)

            } else {
                val error = "Error ${response.code()}: ${response.message()}"
                Log.e(TAG, error)
                return Result.failure(Exception(error))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error registering driver", e)
            return Result.failure(e)
        }
    }

    // ==========================================
    // TODO: Métodos pendientes
    // ==========================================

    /**
     * Actualiza información de un transportista.
     * TODO: Implementar cuando esté disponible PUT /api/Transport/{uid}
     */
    suspend fun updateDriver(
        uid: String,
        fullName: String,
        phone: String,
        licenseNumber: String
    ): Result<Driver> {
        return Result.failure(Exception("Función no implementada aún"))
    }

    /**
     * Elimina un transportista del sistema.
     * TODO: Implementar cuando esté disponible DELETE /api/Transport/{uid}
     */
    suspend fun deleteDriver(uid: String): Result<Unit> {
        return Result.failure(Exception("Función no implementada aún"))
    }
}