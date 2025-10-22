package com.infosphere.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infosphere.enums.GameMode
import com.infosphere.repository.WordRepository
import com.infosphere.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WordFetchViewModel(
    private val repository: WordRepository = WordRepository(RetrofitClient.api)
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentWord = MutableStateFlow<String?>(null)
    val currentWord: StateFlow<String?> = _currentWord.asStateFlow()

    fun fetchNewWord(gameMode: GameMode, sizeForRandom: Int = 5) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val remote = when (gameMode) {
                    GameMode.DAILY -> repository.getDailyWord()
                    GameMode.WEEKLY -> repository.getWeeklyWord()
                    GameMode.MONTHLY -> repository.getMonthlyWord()
                    GameMode.NORMAL -> repository.getRandomWordByMaxSize(sizeForRandom)
                }

                val word = remote?.name?.trim()?.uppercase()
                if (!word.isNullOrEmpty()) {
                    _currentWord.value = word
                } else {
                    _errorMessage.value = "Aucun mot disponible"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Erreur réseau"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
