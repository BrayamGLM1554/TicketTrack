package com.tickettrack.app.ui.main.drivers

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import com.tickettrack.app.data.model.Driver

class DriverViewModel : ViewModel() {

    private val _transportistas = mutableStateListOf<Driver>()
    val transportistas: List<Driver> get() = _transportistas

    init {
        // Datos de ejemplo que coinciden con la imagen
        _transportistas.addAll(
            listOf(
                Driver(
                    fullName = "Juan Pérez",
                    email = "juan.perez@correo.com",
                    licenseNumber = "Unidad 045",
                    licenseCaducation = "2026-05-30",
                    personalPhoneEncrypted = "+52 555-123-4567",
                    createdAt = "2025-10-23",
                    createdBy = "Admin001",
                    companyName = "Transporte MX",
                    isActive = true,
                    unitNumber = "045",
                    totalTrips = 24
                ),
                Driver(
                    fullName = "María López",
                    email = "maria.lopez@correo.com",
                    licenseNumber = "Unidad 023",
                    licenseCaducation = "2025-12-15",
                    personalPhoneEncrypted = "+52 555-234-5678",
                    createdAt = "2025-10-20",
                    createdBy = "Admin001",
                    companyName = "Transporte MX",
                    isActive = true,
                    unitNumber = "023",
                    totalTrips = 18
                ),
                Driver(
                    fullName = "Carlos Gómez",
                    email = "carlos.gomez@correo.com",
                    licenseNumber = "Unidad 067",
                    licenseCaducation = "2026-03-20",
                    personalPhoneEncrypted = "+52 555-345-6789",
                    createdAt = "2025-10-18",
                    createdBy = "Admin001",
                    companyName = "Transporte MX",
                    isActive = false,
                    unitNumber = "067",
                    totalTrips = 31
                ),
                Driver(
                    fullName = "Ana Martínez",
                    email = "ana.martinez@correo.com",
                    licenseNumber = "Unidad 089",
                    licenseCaducation = "2026-07-10",
                    personalPhoneEncrypted = "+52 555-456-7890",
                    createdAt = "2025-10-15",
                    createdBy = "Admin001",
                    companyName = "Transporte MX",
                    isActive = true,
                    unitNumber = "089",
                    totalTrips = 15
                )
            )
        )
    }
}