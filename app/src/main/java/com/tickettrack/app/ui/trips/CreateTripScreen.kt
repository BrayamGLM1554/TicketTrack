package com.tickettrack.app.ui.trips

import android.content.Context
import androidx.compose.material.icons.filled.SearchOff
import android.location.Geocoder
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.tickettrack.app.domain.model.CreateTripRequest
import com.tickettrack.app.domain.model.TransportistaAvailable
import com.tickettrack.app.ui.theme.Primary
import com.tickettrack.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    token: String,
    onBackPressed: () -> Unit,
    onTripCreated: () -> Unit,
    viewModel: TripsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var tripName by remember { mutableStateOf("") }
    var cargo by remember { mutableStateOf("") }
    var budgetAssigned by remember { mutableStateOf("") }
    var selectedTransportista by remember { mutableStateOf<TransportistaAvailable?>(null) }
    var showTransportistaDialog by remember { mutableStateOf(false) }

    // Estados para origen y destino
    var useMapForOrigin by remember { mutableStateOf(false) }
    var useMapForDestination by remember { mutableStateOf(false) }
    var originText by remember { mutableStateOf("") }
    var destinationText by remember { mutableStateOf("") }
    var originLatLng by remember { mutableStateOf<LatLng?>(null) }
    var destinationLatLng by remember { mutableStateOf<LatLng?>(null) }
    var showOriginMapDialog by remember { mutableStateOf(false) }
    var showDestinationMapDialog by remember { mutableStateOf(false) }

    // Permisos de ubicación
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        // Cargar viajes primero, luego transportistas
        viewModel.loadTrips(token)
        delay(500) // Pequeño delay para asegurar que los viajes se carguen primero
        viewModel.loadAvailableTransportistas(token)
    }

    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            viewModel.clearCreateState()
            onTripCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Viaje") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Información del viaje",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre del viaje
            OutlinedTextField(
                value = tripName,
                onValueChange = { tripName = it },
                label = { Text("Nombre del viaje") },
                placeholder = { Text("Whirlpool - Bimbo 22/09/2025") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DriveFileRenameOutline,
                        contentDescription = null,
                        tint = Primary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Carga
            OutlinedTextField(
                value = cargo,
                onValueChange = { cargo = it },
                label = { Text("Carga") },
                placeholder = { Text("Refrigeradores industriales") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = Primary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Presupuesto
            OutlinedTextField(
                value = budgetAssigned,
                onValueChange = { budgetAssigned = it },
                label = { Text("Presupuesto asignado") },
                placeholder = { Text("4230.00") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Primary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Origen
            Text(
                text = "Origen",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { useMapForOrigin = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!useMapForOrigin) Primary else Color(0xFFE0E0E0)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual")
                }

                Button(
                    onClick = {
                        if (hasLocationPermission) {
                            useMapForOrigin = true
                            showOriginMapDialog = true
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (useMapForOrigin) Primary else Color(0xFFE0E0E0)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mapa")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = originText,
                onValueChange = { if (!useMapForOrigin) originText = it },
                label = { Text("Dirección de origen") },
                placeholder = { Text("Escribe o selecciona en el mapa") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !useMapForOrigin,
                readOnly = useMapForOrigin,
                trailingIcon = {
                    if (useMapForOrigin && originLatLng != null) {
                        IconButton(onClick = { showOriginMapDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar ubicación"
                            )
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary,
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledLabelColor = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Destino
            Text(
                text = "Destino",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { useMapForDestination = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!useMapForDestination) Primary else Color(0xFFE0E0E0)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual")
                }

                Button(
                    onClick = {
                        if (hasLocationPermission) {
                            useMapForDestination = true
                            showDestinationMapDialog = true
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (useMapForDestination) Primary else Color(0xFFE0E0E0)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mapa")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = destinationText,
                onValueChange = { if (!useMapForDestination) destinationText = it },
                label = { Text("Dirección de destino") },
                placeholder = { Text("Escribe o selecciona en el mapa") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !useMapForDestination,
                readOnly = useMapForDestination,
                trailingIcon = {
                    if (useMapForDestination && destinationLatLng != null) {
                        IconButton(onClick = { showDestinationMapDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar ubicación"
                            )
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = Color(0xFFF44336)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary,
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledLabelColor = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seleccionar transportista
            Text(
                text = "Transportista",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { showTransportistaDialog = true },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedTransportista?.name ?: "Seleccionar transportista",
                            fontSize = 16.sp,
                            color = if (selectedTransportista != null) {
                                Color(0xFF1A1A1A)
                            } else {
                                TextSecondary
                            }
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            if (uiState.availableTransportistas.isEmpty() && !uiState.isLoadingTransportistas) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No hay transportistas disponibles",
                    fontSize = 14.sp,
                    color = Color.Red
                )
            }

            // Error
            if (uiState.createError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.createError ?: "",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón crear
            Button(
                onClick = {
                    val origin = if (useMapForOrigin) {
                        originLatLng?.let { "${it.latitude}, ${it.longitude}" } ?: originText
                    } else {
                        originText
                    }

                    val destination = if (useMapForDestination) {
                        destinationLatLng?.let { "${it.latitude}, ${it.longitude}" } ?: destinationText
                    } else {
                        destinationText
                    }

                    viewModel.createTrip(
                        token = token,
                        request = CreateTripRequest(
                            origin = origin,
                            destination = destination,
                            cargo = cargo,
                            tripName = tripName,
                            budgetAssigned = budgetAssigned.toDoubleOrNull() ?: 0.0,
                            transportistaUid = selectedTransportista?.uid ?: ""
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isCreating &&
                        tripName.isNotBlank() &&
                        cargo.isNotBlank() &&
                        budgetAssigned.isNotBlank() &&
                        (if (useMapForOrigin) originLatLng != null else originText.isNotBlank()) &&
                        (if (useMapForDestination) destinationLatLng != null else destinationText.isNotBlank()) &&
                        selectedTransportista != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Color(0xFFE0E0E0)
                )
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crear Viaje",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Diálogo de selección de transportista
    if (showTransportistaDialog) {
        TransportistaSelectionDialog(
            transportistas = uiState.availableTransportistas,
            isLoading = uiState.isLoadingTransportistas,
            onDismiss = { showTransportistaDialog = false },
            onSelect = { transportista ->
                selectedTransportista = transportista
                showTransportistaDialog = false
            }
        )
    }

    // Diálogo de mapa para origen
    if (showOriginMapDialog) {
        MapLocationDialog(
            title = "Seleccionar Origen",
            initialLocation = originLatLng,
            onDismiss = { showOriginMapDialog = false },
            onConfirm = { latLng, address ->
                originLatLng = latLng
                originText = address
                showOriginMapDialog = false
            }
        )
    }

    // Diálogo de mapa para destino
    if (showDestinationMapDialog) {
        MapLocationDialog(
            title = "Seleccionar Destino",
            initialLocation = destinationLatLng,
            onDismiss = { showDestinationMapDialog = false },
            onConfirm = { latLng, address ->
                destinationLatLng = latLng
                destinationText = address
                showDestinationMapDialog = false
            }
        )
    }
}

@Composable
fun TransportistaSelectionDialog(
    transportistas: List<TransportistaAvailable>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSelect: (TransportistaAvailable) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Seleccionar Transportista",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Solo disponibles",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Normal
                )
            }
        },
        text = {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Cargando transportistas...",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                transportistas.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PersonOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay transportistas disponibles",
                                color = Color(0xFF1A1A1A),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Todos están en viajes activos",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "${transportistas.size} disponibles",
                            fontSize = 12.sp,
                            color = Color(0xFF66BB6A),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        transportistas.forEach { transportista ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = { onSelect(transportista) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = Color(0xFF66BB6A).copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF66BB6A)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = transportista.name,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = transportista.email,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFF66BB6A).copy(alpha = 0.2f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = "Disponible",
                                            fontSize = 10.sp,
                                            color = Color(0xFF66BB6A),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun MapLocationDialog(
    title: String,
    initialLocation: LatLng?,
    onDismiss: () -> Unit,
    onConfirm: (LatLng, String) -> Unit
) {
    val context = LocalContext.current
    val defaultLocation = initialLocation ?: LatLng(25.6866, -100.3161) // Monterrey, México
    var selectedLocation by remember { mutableStateOf(defaultLocation) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<PlaceResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showSearchResults by remember { mutableStateOf(false) }
    var locationAddress by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // Búsqueda automática con debounce
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 3) {
            showSearchResults = true
            isSearching = true
            delay(500) // Espera 500ms después de que el usuario deja de escribir

            searchLocation(
                context = context,
                query = searchQuery,
                onResults = { results ->
                    searchResults = results
                    isSearching = false
                }
            )
        } else {
            searchResults = emptyList()
            showSearchResults = false
            isSearching = false
        }
    }

    // Geocoder para obtener dirección de las coordenadas
    LaunchedEffect(selectedLocation) {
        try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocation(
                selectedLocation.latitude,
                selectedLocation.longitude,
                1
            )
            locationAddress = addresses?.firstOrNull()?.getAddressLine(0) ?:
                    "Lat: ${"%.6f".format(selectedLocation.latitude)}, Lng: ${"%.6f".format(selectedLocation.longitude)}"
        } catch (e: Exception) {
            locationAddress = "Lat: ${"%.6f".format(selectedLocation.latitude)}, Lng: ${"%.6f".format(selectedLocation.longitude)}"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar ubicación...") },
                    leadingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Primary
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    searchResults = emptyList()
                                    showSearchResults = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar"
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    // Mapa
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                            showSearchResults = false
                        }
                    ) {
                        Marker(
                            state = MarkerState(position = selectedLocation),
                            title = "Ubicación seleccionada"
                        )
                    }

                    // Resultados de búsqueda
                    if (showSearchResults && searchResults.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .padding(8.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            LazyColumn {
                                items(searchResults) { result ->
                                    SearchResultItem(
                                        result = result,
                                        onClick = {
                                            selectedLocation = result.location
                                            searchQuery = result.name
                                            showSearchResults = false
                                            searchResults = emptyList()

                                            // Mover cámara a la ubicación
                                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                                result.location,
                                                15f
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Mensaje de búsqueda
                    if (showSearchResults && searchResults.isEmpty() && !isSearching && searchQuery.length >= 3) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "No se encontraron resultados",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    // Info de la ubicación seleccionada
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shape = MaterialTheme.shapes.small,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ubicación seleccionada",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = locationAddress,
                                fontSize = 12.sp,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedLocation, locationAddress)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SearchResultItem(
    result: PlaceResult,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
                if (result.address.isNotEmpty()) {
                    Text(
                        text = result.address,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

data class PlaceResult(
    val name: String,
    val address: String,
    val location: LatLng
)

fun searchLocation(
    context: Context,
    query: String,
    onResults: (List<PlaceResult>) -> Unit
) {
    try {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocationName(query, 5)

        val results = addresses?.map { address ->
            PlaceResult(
                name = address.featureName ?: address.getAddressLine(0) ?: query,
                address = address.getAddressLine(0) ?: "",
                location = LatLng(address.latitude, address.longitude)
            )
        } ?: emptyList()

        onResults(results)
    } catch (e: Exception) {
        e.printStackTrace()
        onResults(emptyList())
    }
}