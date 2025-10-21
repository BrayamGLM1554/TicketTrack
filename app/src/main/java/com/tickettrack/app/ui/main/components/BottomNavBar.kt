package com.tickettrack.app.ui.main.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = selectedItem == "Inicio",
            onClick = { onItemSelected("Inicio") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.DirectionsBus, contentDescription = "Viajes") },
            label = { Text("Viajes") },
            selected = selectedItem == "Viajes",
            onClick = { onItemSelected("Viajes") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Gastos") },
            label = { Text("Gastos") },
            selected = selectedItem == "Gastos",
            onClick = { onItemSelected("Gastos") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Transportista") },
            label = { Text("Transportista") },
            selected = selectedItem == "Transportista",
            onClick = { onItemSelected("Transportista") }
        )
    }
}