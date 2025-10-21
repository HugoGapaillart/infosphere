package com.infosphere.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infosphere.ui.components.EmptyState
import com.infosphere.ui.components.LoadingIndicator
import com.infosphere.viewmodel.EventViewModel
import com.infosphere.viewmodel.OperationState
import com.infosphere.viewmodel.UserProfileViewModel
import com.infosphere.ui.components.CompactEventCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    eventViewModel: EventViewModel,
    userProfileViewModel: UserProfileViewModel,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchResults by eventViewModel.searchResults.collectAsStateWithLifecycle()
    val operationState by eventViewModel.operationState.collectAsStateWithLifecycle()
    val eventTypes by eventViewModel.allEventTypes.collectAsStateWithLifecycle()
    val cities by userProfileViewModel.allCities.collectAsStateWithLifecycle()
    
    // Convert eventTypes list to map for CompactEventCard
    val eventTypesMap = remember(eventTypes) {
        eventTypes.associateBy { it.id }
    }

    var selectedCityId by remember { mutableStateOf<String?>(null) }
    var selectedTypeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandedCity by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    Scaffold() { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // City Selection
                    Text(
                        "Ville",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedCity,
                        onExpandedChange = { expandedCity = it }
                    ) {
                        OutlinedTextField(
                            value = cities.find { it.id == selectedCityId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                            placeholder = { Text("Sélectionner une ville") },
                            trailingIcon = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (selectedCityId != null) {
                                        IconButton(onClick = { selectedCityId = null }) {
                                            Icon(Icons.Default.Clear, "Effacer")
                                        }
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Event Types Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Types d'événement",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        if (selectedTypeIds.isNotEmpty()) {
                            TextButton(
                                onClick = { selectedTypeIds = emptySet() }
                            ) {
                                Text("Tout effacer")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Scrollable horizontal chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(eventTypes, key = { it.id }) { type ->
                            FilterChip(
                                selected = type.id in selectedTypeIds,
                                onClick = {
                                    selectedTypeIds = if (type.id in selectedTypeIds) {
                                        selectedTypeIds - type.id
                                    } else {
                                        selectedTypeIds + type.id
                                    }
                                },
                                label = { Text(type.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = type.id in selectedTypeIds,
                                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    selectedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Search Button
                    Button(
                        onClick = {
                            eventViewModel.searchEvents(
                                cityId = selectedCityId,
                                eventTypes = selectedTypeIds.toList().takeIf { it.isNotEmpty() }
                            )
                            hasSearched = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = selectedCityId != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Rechercher",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Results Section
            when {
                operationState is OperationState.Loading -> {
                    LoadingIndicator()
                }
                operationState is OperationState.Error -> {
                    EmptyState(
                        message = (operationState as OperationState.Error).message
                    )
                }
                hasSearched -> {
                    if (searchResults.isEmpty()) {
                        EmptyState(message = "Aucun résultat trouvé")
                    } else {
                        // Results Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${searchResults.size} événement${if (searchResults.size > 1) "s" else ""} trouvé${if (searchResults.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Results List with compact horizontal cards
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults, key = { it.id }) { event ->
                                CompactEventCard(
                                    event = event,
                                    onClick = { onEventClick(event.id) }
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Initial state - show helper text
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Commencez votre recherche",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sélectionnez une ville et des types d'événement",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}