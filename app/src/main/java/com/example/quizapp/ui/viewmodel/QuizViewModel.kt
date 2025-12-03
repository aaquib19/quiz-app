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
                val progress = repository.getModuleProgress(moduleId)

                if (progress != null && !progress.isCompleted) {
                    // Resume from saved progress
                    val userAnswers = parseUserAnswers(progress.userAnswers, progress.answeredQuestions)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            questions = questions,
                            currentQuestionIndex = progress.lastQuestionIndex,
                            correctAnswersCount = progress.score,
                            longestStreak = progress.longestStreak,
                            skippedQuestionsCount = progress.skippedQuestions,
                            userAnswers = userAnswers,
                            currentStreak = 0
                        )
                    }

                    // Check if current question was already answered
                    checkIfQuestionAlreadyAnswered()
                } else {
                    // Fresh start
                    _uiState.update { it.copy(isLoading = false, questions = questions) }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectAnswer(selectedOptionIndex: Int) {
        val currentState = _uiState.value
        if (currentState.isAnswerRevealed) return

        val currentQuestionIndex = currentState.currentQuestionIndex

        // Check if this question was already answered
        if (currentState.userAnswers.containsKey(currentQuestionIndex)) {
            return
        }

        _uiState.update {
            it.copy(
                selectedAnswer = currentState.currentQuestion?.options?.get(selectedOptionIndex),
                isAnswerRevealed = true,
                userAnswers = it.userAnswers + (currentQuestionIndex to selectedOptionIndex)
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

        saveCurrentProgress()

        viewModelScope.launch {
            delay(2000)
            nextQuestion()
        }
    }

    fun skipQuestion() {
        _uiState.update { it.copy(skippedQuestionsCount = it.skippedQuestionsCount + 1) }
        saveCurrentProgress()
        nextQuestion()
    }

    private fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedAnswer = null,
                    isAnswerRevealed = false,
                    isAlreadyAnswered = false // RESET flag when moving
                )
            }
            saveCurrentProgress()
            checkIfQuestionAlreadyAnswered()
        }
    }

    private fun checkIfQuestionAlreadyAnswered() {
        val currentState = _uiState.value
        val currentQuestionIndex = currentState.currentQuestionIndex

        if (currentState.userAnswers.containsKey(currentQuestionIndex)) {
            val selectedOptionIndex = currentState.userAnswers[currentQuestionIndex]!!
            _uiState.update {
                it.copy(
                    selectedAnswer = currentState.currentQuestion?.options?.get(selectedOptionIndex),
                    isAnswerRevealed = true,
                    isAlreadyAnswered = true
                )
            }
        }
    }

    fun previousQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex - 1,
                    selectedAnswer = null,
                    isAnswerRevealed = false,
                    isAlreadyAnswered = false // RESET flag when moving
                )
            }
            saveCurrentProgress()
            checkIfQuestionAlreadyAnswered()
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
                    lastQuestionIndex = state.currentQuestionIndex,
                    answeredQuestions = serializeAnsweredQuestions(state.userAnswers),
                    userAnswers = serializeUserAnswers(state.userAnswers)
                )
                repository.saveModuleProgress(progress)
                onSaved()
            }
        }
    }

    private fun saveCurrentProgress() {
        viewModelScope.launch {
            currentModuleId?.let { moduleId ->
                val state = _uiState.value
                val progress = ModuleProgressEntity(
                    moduleId = moduleId,
                    score = state.correctAnswersCount,
                    totalQuestions = state.questions.size,
                    longestStreak = state.longestStreak,
                    skippedQuestions = state.skippedQuestionsCount,
                    isCompleted = false,
                    completedAt = 0L,
                    lastQuestionIndex = state.currentQuestionIndex,
                    answeredQuestions = serializeAnsweredQuestions(state.userAnswers),
                    userAnswers = serializeUserAnswers(state.userAnswers)
                )
                repository.saveModuleProgress(progress)
            }
        }
    }

    private fun serializeAnsweredQuestions(userAnswers: Map<Int, Int>): String {
        return userAnswers.keys.sorted().joinToString(",")
    }

    private fun serializeUserAnswers(userAnswers: Map<Int, Int>): String {
        return userAnswers.entries.sortedBy { it.key }.joinToString(",") { it.value.toString() }
    }

    private fun parseUserAnswers(userAnswersStr: String, answeredQuestionsStr: String): Map<Int, Int> {
        if (userAnswersStr.isEmpty() || answeredQuestionsStr.isEmpty()) return emptyMap()

        val questionIndices = answeredQuestionsStr.split(",").mapNotNull { it.toIntOrNull() }
        val answerIndices = userAnswersStr.split(",").mapNotNull { it.toIntOrNull() }

        return questionIndices.zip(answerIndices).toMap()
    }

    fun resetQuiz() {
        _uiState.update {
            QuizUiState()
        }
        currentModuleId = null
    }
}