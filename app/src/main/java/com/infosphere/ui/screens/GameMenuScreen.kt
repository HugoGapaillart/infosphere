package com.infosphere.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.infosphere.enums.GameMode
import com.infosphere.ui.navigation.Screen

@Composable
fun GameMenuScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf<GameMode?>(null) }

    val modes = GameMode.entries.toList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Choisis un mode de jeu",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Grid-like 2 columns using chunked
        val rows = modes.chunked(2)
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEach { rowModes ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowModes.forEach { mode ->
                        val isSelected = mode == selectedMode
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedMode = mode
                                    navController.navigate(Screen.GameScreen.createRoute(mode.name))
                                },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 12.dp else 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Icon per mode
                                val icon = when (mode) {
                                    GameMode.NORMAL -> Icons.Default.SportsEsports
                                    GameMode.DAILY -> Icons.Default.CalendarToday
                                    GameMode.WEEKLY -> Icons.Default.DateRange
                                    GameMode.MONTHLY -> Icons.Default.EventAvailable
                                }

                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(36.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = mode.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = when (mode) {
                                        GameMode.NORMAL -> "Partie classique"
                                        GameMode.DAILY -> "Un mot par jour"
                                        GameMode.WEEKLY -> "Défi hebdomadaire"
                                        GameMode.MONTHLY -> "Challenge du mois"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // if row has only one element, fill remaining space
                    if (rowModes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bouton retour ou aide
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Retour")
        }
    }
}
