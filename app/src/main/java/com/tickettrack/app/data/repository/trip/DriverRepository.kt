package com.tickettrack.app.data.repository.trip

import android.util.Log
import com.tickettrack.app.data.model.trip.DriverListResponse
import com.tickettrack.app.data.model.trip.DriverResponse
import kotlinx.coroutines.delay

/**
 * Repositorio para operaciones con transportistas.
 *
 * Siguiendo Clean Architecture:
 * - Encapsula acceso a datos de transportistas
 * - Preparado para integración con Firebase/API
 * - Actualmente simula datos para desarrollo
 */
class DriverRepository {

    companion object {
        private const val TAG = "DriverRepository"
    }

    /**
     * Obtiene la lista de transportistas disponibles.
     *
     * @return Lista de transportistas
     */
    suspend fun getAvailableDrivers(): Result<DriverListResponse> {
        return try {
            Log.d(TAG, "Fetching available drivers")

            // Simular delay de red
            delay(800)

            // TODO: Reemplazar con llamada real a Firebase
            // val snapshot = firestore.collection("users")
            //     .whereEqualTo("role", "driver")
            //     .whereEqualTo("isAvailable", true)
            //     .get()
            //     .await()

            // Datos simulados de transportistas
            val mockDrivers = listOf(
                DriverResponse(
                    id = "driver_001",
                    name = "Roberto Alejandro Herrera Luna",
                    photoUrl = null,
                    completedTrips = 8,
                    assignedUnit = "TG-0347",
                    plates = "NL-TB-4782",
                    brand = "Kenworth",
                    model = "T680",
                    truckPhotoUrl = null,
                    isAvailable = true
                ),
                DriverResponse(
                    id = "driver_002",
                    name = "Juan Carlos Pérez García",
                    photoUrl = null,
                    completedTrips = 15,
                    assignedUnit = "TG-0241",
                    plates = "JL-XY-1234",
                    brand = "Freightliner",
                    model = "Cascadia",
                    truckPhotoUrl = null,
                    isAvailable = true
                ),
                DriverResponse(
                    id = "driver_003",
                    name = "María Fernanda López Ruiz",
                    photoUrl = null,
                    completedTrips = 22,
                    assignedUnit = "TG-0189",
                    plates = "MX-AB-5678",
                    brand = "Volvo",
                    model = "VNL",
                    truckPhotoUrl = null,
                    isAvailable = true
                ),
                DriverResponse(
                    id = "driver_004",
                    name = "José Luis Martínez Sánchez",
                    photoUrl = null,
                    completedTrips = 5,
                    assignedUnit = "TG-0512",
                    plates = "QR-CD-9012",
                    brand = "Peterbilt",
                    model = "579",
                    truckPhotoUrl = null,
                    isAvailable = true
                ),
                DriverResponse(
                    id = "driver_005",
                    name = "Ana Gabriela Ramírez Torres",
                    photoUrl = null,
                    completedTrips = 12,
                    assignedUnit = "TG-0298",
                    plates = "SL-EF-3456",
                    brand = "International",
                    model = "LT Series",
                    truckPhotoUrl = null,
                    isAvailable = true
                )
            )

            val response = DriverListResponse(drivers = mockDrivers)

            Log.d(TAG, "Found ${mockDrivers.size} available drivers")
            Result.success(response)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching drivers", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene un transportista por su ID.
     *
     * @param driverId ID del transportista
     * @return Transportista encontrado o null
     */
    suspend fun getDriverById(driverId: String): Result<DriverResponse?> {
        return try {
            Log.d(TAG, "Fetching driver: $driverId")

            delay(500)

            // TODO: Reemplazar con llamada real a Firebase
            // val doc = firestore.collection("users").document(driverId).get().await()

            // Buscar en drivers simulados
            val allDrivers = getAvailableDrivers().getOrNull()?.drivers
            val driver = allDrivers?.find { it.id == driverId }

            if (driver != null) {
                Log.d(TAG, "Driver found: ${driver.name}")
            } else {
                Log.d(TAG, "Driver not found")
            }

            Result.success(driver)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching driver", e)
            Result.failure(e)
        }
    }
}