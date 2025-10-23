package com.infosphere.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infosphere.enums.LetterStatus
import com.infosphere.viewmodel.WordGuessViewModel
import kotlinx.coroutines.launch

@Composable
fun WordGameGrid(
    viewModel: WordGuessViewModel,
    modifier: Modifier = Modifier,
    onRequestNewWord: suspend () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val columns = uiState.targetWord.length
    val maxRows = uiState.maxTries

    var currentGuess by remember { mutableStateOf("") }

    // Colors
    val correctColor = Color(0xFF4CAF50)
    val presentColor = Color(0xFFFFA000)
    val absentColor = Color(0xFFD1D5DB)
    val emptyBg = Color(0xFFF8FAFC)

    val scope = rememberCoroutineScope()
    var isFetching by remember { mutableStateOf(false) }

    val inputLocked = uiState.isGameOver || isFetching
    val canSubmit = !inputLocked && currentGuess.length == columns

    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Essais restants : ${uiState.remainingTries}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grille fixe : afficher maxRows lignes (remplir avec les essais existants)
            Column(modifier = Modifier.fillMaxWidth()) {
                for (row in 0 until maxRows) {
                    val isPreviewRow = row == uiState.guesses.size
                    val guessToDisplay = when {
                        row < uiState.guesses.size -> uiState.guesses[row]
                        isPreviewRow -> currentGuess
                        else -> ""
                    }

                    val statusesNullable: List<LetterStatus?> = when {
                        row < uiState.letterResults.size -> uiState.letterResults[row].map { it as LetterStatus? }
                        else -> List(columns) { null }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 0 until columns) {
                            val ch = guessToDisplay.getOrNull(i)?.toString() ?: ""
                            val status = if (isPreviewRow) null else statusesNullable[i]

                            val bg = when (status) {
                                LetterStatus.CORRECT -> correctColor
                                LetterStatus.PRESENT -> presentColor
                                LetterStatus.ABSENT -> absentColor
                                null -> emptyBg
                            }

                            val textColor = when (status) {
                                LetterStatus.CORRECT, LetterStatus.PRESENT -> Color.White
                                LetterStatus.ABSENT -> Color.Black
                                null -> Color.Black
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(bg, RoundedCornerShape(6.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ch,
                                    color = textColor,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Si l'utilisateur a perdu (isGameOver vrai et pas de victoire), afficher le mot cible
            if (uiState.isGameOver && !uiState.isWin) {
                Text(
                    text = "Mot : ${uiState.targetWord}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            OutlinedTextField(
                value = currentGuess,
                onValueChange = { if (!inputLocked && it.length <= columns) currentGuess = it },
                label = { Text("Proposer un mot") },
                singleLine = true,
                enabled = !inputLocked,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = {
                        if (canSubmit) {
                            viewModel.submitGuess(currentGuess)
                            currentGuess = ""
                        }
                    },
                    enabled = canSubmit
                ) {
                    Text("Essayer")
                }

                OutlinedButton(onClick = {
                    // déclencher l'appel API via la lambda fournie
                    scope.launch {
                        isFetching = true
                        try {
                            onRequestNewWord()
                            // la nouvelle cible doit être définie par la lambda (setTargetWord), qui réinitialise l'état
                            currentGuess = ""
                        } catch (e: Exception) {
                            Log.e("WordGuessCard", "Erreur lors du fetch de nouveau mot", e)
                        } finally {
                            isFetching = false
                        }
                    }
                }) {
                    if (isFetching) {
                        Text("Chargement...")
                    } else {
                        Text("Réinitialiser")
                    }
                }
            }

            // Affiche l'historique textuel simple (optionnel)
            if (uiState.guesses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Précédents essais : ${uiState.guesses.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (uiState.isGameOver) {
                Spacer(modifier = Modifier.height(8.dp))
                val result = if (uiState.isWin) "Gagné !" else "Perdu"
                Text(text = result, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}