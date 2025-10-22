@file:OptIn(ExperimentalMaterial3Api::class)

package com.infosphere.ui.screens

import android.Manifest
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Priority
import com.infosphere.repository.CityRepository
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    userProfileViewModel: UserProfileViewModel,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val user by authViewModel.userProfile.collectAsStateWithLifecycle()
    val events by eventViewModel.events.collectAsStateWithLifecycle()
    val eventTypes by eventViewModel.allEventTypes.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var cityName by remember { mutableStateOf<String?>(null) }
    var hasPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect

        val permissionState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            try {
                val location = fusedLocationClient
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token // obligatoire pour await()
                    )
                    .await() // suspend function

                location?.let {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    val address = addresses?.firstOrNull()

                    val city = address?.locality ?: return@let
                    val region = address?.adminArea ?: ""
                    val country = address?.countryName ?: ""

                    cityName = "$city"

                    // Appel suspendable possible ici
                    CityRepository().addCityIfNotExists(city, region, country)
                } ?: run { cityName = "Localisation indisponible" }

            } catch (e: SecurityException) {
                Log.e("HomeScreen", "Erreur de permission : ${e.message}")
                cityName = "Permission refusée"
            } catch (e: Exception) {
                Log.e("HomeScreen", "Erreur localisation : ${e.message}")
                cityName = "Erreur localisation"
            }
        } else {
            cityName = "Permission refusée"
        }
    }

    LaunchedEffect(user?.selectedCityIds) {
        user?.selectedCityIds?.let { cityIds ->
            if (cityIds.isNotEmpty()) {
                eventViewModel.loadEventsByCities(cityIds)
            }
        }
    }

    var currentCityId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cityName) {
        cityName?.let { fullName ->
            val parts = fullName.split(",") // "Paris, Île-de-France, France"
            val city = parts.firstOrNull() ?: return@let

            // Cherche la ville en base et récupère son ID
            val result = CityRepository().searchCities(city)
            result.onSuccess { cities ->
                val cityObj = cities.firstOrNull()
                currentCityId = cityObj?.id

                // Charge les événements pour la ville actuelle + villes favorites
                val cityIds = mutableListOf<String>()
                currentCityId?.let { cityIds.add(it) }
                user?.selectedCityIds?.let { cityIds.addAll(it) }

                if (cityIds.isNotEmpty()) {
                    eventViewModel.loadEventsByCities(cityIds.distinct())
                }
            }.onFailure { e ->
                Log.e("HomeScreen", "Erreur récupération ville actuelle : ${e.message}")
            }
        }
    }

    Log.d("EventViewModel", "Fetched ${events}")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Accueil") }) }
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = modifier.padding(paddingValues),
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                val cityIdsToLoad = mutableListOf<String>()
                currentCityId?.let { cityIdsToLoad.add(it) }
                user?.selectedCityIds?.let { cityIdsToLoad.addAll(it) }
                if (cityIdsToLoad.isNotEmpty()) {
                    eventViewModel.loadEventsByCities(cityIdsToLoad.distinct())
                }
                isRefreshing = false
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

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
                            // --- Événements de la ville actuelle ---
                            currentCityId?.let { cityId ->
                                val cityEvents = events.filter { it.cityId == cityId }
                                if (cityEvents.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Événements de la ville dans laquelle vous êtes",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                                        )
                                    }
                                    items(cityEvents, key = { it.id }) { event ->
                                        EventCard(
                                            event = event,
                                            eventTypes = eventTypes,
                                            onClick = onEventClick,
                                        )
                                    }
                                }
                            }

                            // --- Événements des villes favorites ---
                            val favoriteCityEvents = user?.selectedCityIds?.let { cityIds ->
                                events.filter { it.cityId in cityIds && it.cityId != currentCityId }
                            } ?: emptyList()

                            if (favoriteCityEvents.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Événements dans vos villes favorites",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                                    )
                                }
                                items(favoriteCityEvents, key = { it.id }) { event ->
                                    EventCard(
                                        event = event,
                                        eventTypes = eventTypes,
                                        onClick = onEventClick,
                                    )
                                }
                            }

                            // --- Aucun événement ---
                            if ((currentCityId == null || events.none { it.cityId == currentCityId }) &&
                                favoriteCityEvents.isEmpty()
                            ) {
                                item {
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
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}
