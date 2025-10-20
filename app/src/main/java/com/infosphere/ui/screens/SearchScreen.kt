package com.infosphere.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infosphere.ui.components.EmptyState
import com.infosphere.ui.components.EventCard
import com.infosphere.ui.components.LoadingIndicator
import com.infosphere.viewmodel.EventViewModel
import com.infosphere.viewmodel.OperationState
import com.infosphere.viewmodel.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    eventViewModel: EventViewModel,
    userProfileViewModel: UserProfileViewModel,
    modifier: Modifier = Modifier
) {
    val searchResults by eventViewModel.searchResults.collectAsStateWithLifecycle()
    val operationState by eventViewModel.operationState.collectAsStateWithLifecycle()
    val cities by userProfileViewModel.allCities.collectAsStateWithLifecycle()
    val eventTypes by userProfileViewModel.allEventTypes.collectAsStateWithLifecycle()
    
    var selectedCityId by remember { mutableStateOf<String?>(null) }
    var selectedTypeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandedCity by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Rechercher") })
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // City Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCity,
                onExpandedChange = { expandedCity = it }
            ) {
                OutlinedTextField(
                    value = cities.find { it.id == selectedCityId }?.name ?: "Sélectionner une ville",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Ville") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) }
                )
                ExposedDropdownMenu(
                    expanded = expandedCity,
                    onDismissRequest = { expandedCity = false }
                ) {
                    cities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city.name) },
                            onClick = {
                                selectedCityId = city.id
                                expandedCity = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Event Types
            Text("Types d'événement", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                eventTypes.forEach { type ->
                    FilterChip(
                        selected = type.id in selectedTypeIds,
                        onClick = {
                            selectedTypeIds = if (type.id in selectedTypeIds) {
                                selectedTypeIds - type.id
                            } else {
                                selectedTypeIds + type.id
                            }
                        },
                        label = { Text(type.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Button
            Button(
                onClick = {
                    eventViewModel.searchEvents(
                        cityId = selectedCityId,
                        eventTypes = selectedTypeIds.toList().takeIf { it.isNotEmpty() }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCityId != null
            ) {
                Text("Rechercher")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            when (operationState) {
                is OperationState.Loading -> LoadingIndicator()
                is OperationState.Error -> EmptyState(
                    message = (operationState as OperationState.Error).message
                )
                else -> {
                    if (searchResults.isEmpty()) {
                        EmptyState(message = "Aucun résultat")
                    } else {
                        LazyColumn {
                            items(searchResults, key = { it.id }) { event ->
                                EventCard(
                                    event = event,
                                    eventTypes = eventTypes,
                                    onClick = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    // Simplified FlowRow - in production use accompanist or custom implementation
    Column(modifier = modifier) {
        content()
    }
}
