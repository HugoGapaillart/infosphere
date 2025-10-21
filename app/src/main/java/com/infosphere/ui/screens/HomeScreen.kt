@file:OptIn(ExperimentalMaterial3Api::class)

package com.infosphere.ui.screens

import android.Manifest
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.infosphere.models.Event
import com.infosphere.ui.components.EmptyState
import com.infosphere.ui.components.EventCard
import com.infosphere.viewmodel.AuthViewModel
import com.infosphere.viewmodel.EventViewModel
import com.infosphere.viewmodel.UserProfileViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    userProfileViewModel: UserProfileViewModel,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val user by userProfileViewModel.user.collectAsStateWithLifecycle()
    val events by eventViewModel.events.collectAsStateWithLifecycle()
    val eventTypes by userProfileViewModel.allEventTypes.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }

    // --- Contexte + localisation ---
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var cityName by remember { mutableStateOf<String?>(null) }
    var hasPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    // Demande la permission
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Si la permission est accordée → récupère la ville
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    cityName = addresses?.firstOrNull()?.locality ?: "Ville inconnue"
                } else {
                    cityName = "Localisation indisponible"
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Accueil") }) }
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = modifier.padding(paddingValues),
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                userProfileViewModel.loadUserProfile()
                isRefreshing = false
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Message de bienvenue
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = buildString {
                            append("Bonjour, ${currentUser?.displayName ?: currentUser?.email ?: "Utilisateur"} !")
                            if (cityName != null) append("\nVous êtes à $cityName")
                        },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Vérification des villes sélectionnées
                if (user?.selectedCityIds.isNullOrEmpty()) {
                    EmptyState(
                        message = "Sélectionnez vos villes dans votre profil pour voir les événements",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    if (events.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucun événement à venir",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(events, key = { it.id }) { event ->
                                EventCard(
                                    event = event,
                                    eventTypes = eventTypes,
                                    onClick = onEventClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
