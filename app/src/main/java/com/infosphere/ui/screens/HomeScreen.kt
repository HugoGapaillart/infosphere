@file:OptIn(ExperimentalMaterial3Api::class)

package com.infosphere.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infosphere.models.Event
import com.infosphere.ui.components.EmptyState
import com.infosphere.ui.components.EventCard
import com.infosphere.viewmodel.AuthViewModel
import com.infosphere.viewmodel.EventViewModel
import com.infosphere.viewmodel.UserProfileViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

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

    LaunchedEffect(user?.selectedCityIds) {
        user?.selectedCityIds?.let { cityIds ->
            if (cityIds.isNotEmpty()) {
                eventViewModel.loadEventsByCities(cityIds)
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
                        text = "Bonjour, ${currentUser?.displayName ?: currentUser?.email ?: "Utilisateur"} !",
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
