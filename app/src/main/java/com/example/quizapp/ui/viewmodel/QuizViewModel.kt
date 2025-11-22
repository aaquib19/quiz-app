package com.example.quizapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.data.QuizRepository
import com.example.quizapp.ui.theme.QuizUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: QuizRepository = QuizRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())

    val uiState = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            try {
                val questions = repository.getQuestions()
                _uiState.update { it.copy(isLoading = false, questions = questions) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectAnswer(selectedOptionIndex: Int) {
        val currentState = _uiState.value
        if (currentState.isAnswerRevealed) return

        _uiState.update {
            it.copy(selectedAnswer = currentState.currentQuestion?.options?.get(selectedOptionIndex), isAnswerRevealed = true)
        }

        val correctIndex = currentState.currentQuestion?.correctOptionIndex

        val isCorrect = selectedOptionIndex == correctIndex

        if (isCorrect) {
            val newStreak = currentState.currentStreak + 1
            _uiState.update {
                it.copy(
                    correctAnswersCount = it.correctAnswersCount + 1,
                    currentStreak = newStreak,
                    longestStreak = maxOf(it.longestStreak, newStreak)
                )
            }
        } else {
            _uiState.update { it.copy(currentStreak = 0) }
        }

        viewModelScope.launch {
            delay(2000)
            nextQuestion()
        }
    }
    fun skipQuestion() {
        _uiState.update { it.copy(skippedQuestionsCount = it.skippedQuestionsCount + 1) }
        nextQuestion()
    }

    private fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedAnswer = null,
                    isAnswerRevealed = false
                )
            }
        } else {
            // Quiz is finished
            _uiState.update { it.copy(isQuizFinished = true) }
        }
    }

    fun previousQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex - 1,
                    selectedAnswer = null,
                    isAnswerRevealed = false
                )
            }
        }
    }

    fun resetQuiz() {
        _uiState.update {
            it.copy(
                currentQuestionIndex = 0,
                selectedAnswer = null,
                isAnswerRevealed = false,
                currentStreak = 0,
                longestStreak = 0,
                correctAnswersCount = 0,
                skippedQuestionsCount = 0,
                isQuizFinished = false
            )
        }
    }
}