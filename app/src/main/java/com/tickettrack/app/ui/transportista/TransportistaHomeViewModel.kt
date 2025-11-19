package com.tickettrack.app.ui.transportista

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.Expense
import com.tickettrack.app.domain.model.Trip
import com.tickettrack.app.domain.model.TripStatus
import com.tickettrack.app.domain.repository.IExpensesRepository
import com.tickettrack.app.domain.repository.ITripsRepository
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class TransportistaHomeUiState(
    val isLoading: Boolean = false,
    val currentTrip: Trip? = null,
    val totalExpenses: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val recentExpenses: List<Expense> = emptyList(), // NUEVO: Lista de gastos recientes
    val error: String? = null
)

class TransportistaHomeViewModel(
    private val tripsRepository: ITripsRepository,
    private val expensesRepository: IExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportistaHomeUiState())
    val uiState: StateFlow<TransportistaHomeUiState> = _uiState.asStateFlow()

    fun loadCurrentTrip(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Usar el nuevo endpoint para usuarios USER
            val tripsResult = tripsRepository.getAssignedTrips(token)

            tripsResult.fold(
                onSuccess = { trips ->
                    // Buscar el viaje activo (PENDING o IN_PROGRESS)
                    val activeTrip = trips.firstOrNull {
                        it.status == TripStatus.PENDING || it.status == TripStatus.IN_PROGRESS
                    }

                    if (activeTrip != null) {
                        loadTripExpenses(token, activeTrip)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentTrip = null,
                            totalExpenses = 0.0,
                            remainingBudget = 0.0,
                            recentExpenses = emptyList()
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar viaje actual"
                    )
                }
            )
        }
    }

    private suspend fun loadTripExpenses(token: String, trip: Trip) {
        val expensesResult = expensesRepository.getMyExpenses(token)

        expensesResult.fold(
            onSuccess = { expenses ->
                // Filtrar solo los gastos del viaje actual
                val currentTripExpenses = expenses.filter { it.tripId == trip.id }

                // Calcular totales
                val totalExpenses = currentTripExpenses.sumOf { it.amount }
                val remainingBudget = trip.budgetAssigned - totalExpenses

                // Ordenar por fecha de creación (más reciente primero) y tomar los últimos 3
                val recentExpenses = currentTripExpenses
                    .sortedByDescending { parseCreatedAt(it.createdAt) }
                    .take(3)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTrip = trip,
                    totalExpenses = totalExpenses,
                    remainingBudget = remainingBudget,
                    recentExpenses = recentExpenses
                )
            },
            onFailure = {
                // Si falla cargar gastos, al menos mostramos el viaje
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTrip = trip,
                    totalExpenses = 0.0,
                    remainingBudget = trip.budgetAssigned,
                    recentExpenses = emptyList()
                )
            }
        )
    }

    // Helper para parsear la fecha ISO 8601
    private fun parseCreatedAt(createdAt: String): Instant {
        return try {
            Instant.parse(createdAt)
        } catch (e: Exception) {
            Instant.MIN // Si hay error, poner al final
        }
    }
}