package com.tickettrack.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.ui.main.components.BottomNavBar
import com.tickettrack.app.ui.main.components.TopBar

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val state = viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopBar(
                companyName = state.value.companyName,
                onLogout = { onLogout() }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedItem = state.value.selectedTab,
                onItemSelected = { viewModel.onTabSelected(it) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Resumen General",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Cards principales
            val cards = listOf(
                "Viajes Activos" to state.value.viajesActivos,
                "En Viaje" to "$${state.value.enViaje}",
                "Pendientes" to "${state.value.pendientes}",
                "Saldo Total" to "$${state.value.saldoTotal}"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (row in cards.chunked(2)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { (title, value) ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFEAF7F7)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = value.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFF187083)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
