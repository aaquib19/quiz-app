package com.example.quizapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.data.QuizRepository
import com.example.quizapp.data.local.ModuleProgressEntity
import com.example.quizapp.ui.theme.QuizUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: QuizRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState = _uiState.asStateFlow()

    private var currentModuleId: String? = null

    fun loadQuestionsForModule(moduleId: String, questionsUrl: String) {
        currentModuleId = moduleId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val questions = repository.getQuestionsForModule(questionsUrl)
                _uiState.update { it.copy(isLoading = false, questions = questions) }

                val progress = repository.getModuleProgress(moduleId)
                progress?.let {
                    _uiState.update { currentState ->
                        currentState.copy(
                            currentQuestionIndex = it.lastQuestionIndex
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectAnswer(selectedOptionIndex: Int) {
        val currentState = _uiState.value
        if (currentState.isAnswerRevealed) return

        _uiState.update {
            it.copy(
                selectedAnswer = currentState.currentQuestion?.options?.get(selectedOptionIndex),
                isAnswerRevealed = true
            )
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
            // Do not automatically finish the quiz on the last question.
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

    fun submitQuizAndFinish() {
        _uiState.update { it.copy(isQuizFinished = true) }
    }

    fun saveProgressAndFinish(onSaved: () -> Unit) {
        viewModelScope.launch {
            currentModuleId?.let { moduleId ->
                val state = _uiState.value
                val progress = ModuleProgressEntity(
                    moduleId = moduleId,
                    score = state.correctAnswersCount,
                    totalQuestions = state.questions.size,
                    longestStreak = state.longestStreak,
                    skippedQuestions = state.skippedQuestionsCount,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis(),
                    lastQuestionIndex = state.currentQuestionIndex
                )
                repository.saveModuleProgress(progress)
                onSaved()
            }
        }
    }

    fun resetQuiz() {
        _uiState.update {
            QuizUiState()
        }
        currentModuleId = null
    }
}