package com.infosphere.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.infosphere.enums.LetterStatus

data class GameState(
    val targetWord: String,
    val maxTries: Int,
    val remainingTries: Int,
    val guesses: List<String> = emptyList(),
    val letterResults: List<List<LetterStatus>> = emptyList(),
    val isGameOver: Boolean = false,
    val isWin: Boolean = false
)

class WordGuessViewModel(
    initialTargetWord: String,
    private val maxTries: Int = 5
) : ViewModel() {
    private var currentTarget: String = initialTargetWord.uppercase()

    private val _uiState = MutableStateFlow(
        GameState(
            targetWord = currentTarget,
            maxTries = maxTries,
            remainingTries = maxTries,
            guesses = emptyList(),
            letterResults = emptyList()
        )
    )
    val uiState: StateFlow<GameState> = _uiState

    fun setTargetWord(newTarget: String) {
        currentTarget = newTarget.trim().uppercase()
        _uiState.update {
            it.copy(
                targetWord = currentTarget,
                guesses = emptyList(),
                letterResults = emptyList(),
                remainingTries = maxTries,
                isGameOver = false,
                isWin = false
            )
        }
    }

    fun submitGuess(guessRaw: String) {
        val guess = guessRaw.trim().uppercase()
        if (_uiState.value.isGameOver) return
        if (guess.length != _uiState.value.targetWord.length) return

        val current = _uiState.value
        val newRemaining = current.remainingTries - 1

        val target = current.targetWord
        val n = target.length

        val counts = mutableMapOf<Char, Int>()
        for (c in target) counts[c] = counts.getOrDefault(c, 0) + 1

        val statuses = MutableList(n) { LetterStatus.ABSENT }

        for (i in 0 until n) {
            if (guess[i] == target[i]) {
                statuses[i] = LetterStatus.CORRECT
                counts[guess[i]] = counts.getOrDefault(guess[i], 0) - 1
            }
        }

        for (i in 0 until n) {
            if (statuses[i] == LetterStatus.CORRECT) continue
            val ch = guess[i]
            val available = counts.getOrDefault(ch, 0)
            if (available > 0) {
                statuses[i] = LetterStatus.PRESENT
                counts[ch] = available - 1
            } else {
                statuses[i] = LetterStatus.ABSENT
            }
        }

        val isWin = statuses.all { it == LetterStatus.CORRECT }

        _uiState.update {
            it.copy(
                guesses = it.guesses + guess,
                letterResults = it.letterResults + listOf(statuses),
                remainingTries = newRemaining,
                isWin = isWin,
                isGameOver = isWin || newRemaining <= 0
            )
        }
    }

    fun reset() {
        currentTarget = currentTarget.uppercase()
        _uiState.value = GameState(
            targetWord = currentTarget,
            maxTries = maxTries,
            remainingTries = maxTries,
            guesses = emptyList(),
            letterResults = emptyList(),
            isGameOver = false,
            isWin = false
        )
    }
}
