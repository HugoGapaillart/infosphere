package com.infosphere.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.infosphere.enums.GameMode
import com.infosphere.ui.components.WordGameGrid
import com.infosphere.viewmodel.WordFetchViewModel
import com.infosphere.viewmodel.WordGuessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameMode: GameMode,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // ViewModel local pour la logique de guessing
    val wordVm = remember { WordGuessViewModel(initialTargetWord = "MAISON") }

    // ViewModel réseau pour récupérer les mots via WordApi
    val fetchVm: WordFetchViewModel = viewModel()

    // Collecter l'état réseau
    val isLoading by fetchVm.isLoading.collectAsStateWithLifecycle()
    val errorMessage by fetchVm.errorMessage.collectAsStateWithLifecycle()
    val currentWord by fetchVm.currentWord.collectAsStateWithLifecycle()

    // Dès qu'on reçoit un mot remote, on l'envoie au wordVm
    LaunchedEffect(currentWord) {
        currentWord?.let { wordVm.setTargetWord(it) }
    }

    // Charger un mot initial pour le mode demandé
    LaunchedEffect(gameMode) {
        fetchVm.fetchNewWord(gameMode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Jeu — ${gameMode.name.lowercase().replaceFirstChar { it.uppercaseChar() }}")
                        Text(
                            text = when (gameMode) {
                                GameMode.NORMAL -> "Partie classique"
                                GameMode.DAILY -> "Un mot par jour"
                                GameMode.WEEKLY -> "Défi hebdomadaire"
                                GameMode.MONTHLY -> "Challenge du mois"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, elevation = CardDefaults.cardElevation(6.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        WordGameGrid(
                            viewModel = wordVm,
                            onRequestNewWord = {
                                fetchVm.fetchNewWord(gameMode)
                            }
                        )
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            errorMessage?.let { msg ->
                Text(text = msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
