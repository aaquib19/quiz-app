package com.example.quizapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.data.QuizRepository
import com.example.quizapp.data.local.ModuleProgressEntity
import com.example.quizapp.data.local.UserAnswerEntity
import com.example.quizapp.ui.theme.QuizUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuizViewModel(private val repository: QuizRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState = _uiState.asStateFlow()

    private var currentModuleId: String? = null

    fun loadQuestionsForModule(moduleId: String, questionsUrl: String) {
        _uiState.update { QuizUiState().copy(isLoading = true) }
        currentModuleId = moduleId

        viewModelScope.launch {
            try {
                val questions = withContext(Dispatchers.IO) {
                    repository.getQuestionsForModule(questionsUrl)
                }

                val progress = withContext(Dispatchers.IO) {
                    repository.getModuleProgress(moduleId)
                }

                val userAnswersList = repository.getUserAnswersForModule(moduleId)
                val userAnswersMap = userAnswersList.associate {
                    it.questionId to it.selectedOptionIndex
                }

                when {
                    progress != null && !progress.isCompleted -> {
                        val resumeState = QuizUiState(
                            isLoading = false,
                            questions = questions,
                            userAnswers = userAnswersMap,
                            correctAnswersCount = progress.score,
                            longestStreak = progress.longestStreak,
                            currentStreak = 0,
                            isShowingResults = false
                        )

                        _uiState.update {
                            applyQuestionState(resumeState, progress.lastQuestionIndex)
                        }
                    }

                    progress != null && progress.isCompleted -> {
                        val completedState = QuizUiState(
                            isLoading = false,
                            questions = questions,
                            userAnswers = userAnswersMap,
                            correctAnswersCount = progress.score,
                            longestStreak = progress.longestStreak,
                            isShowingResults = true
                        )

                        _uiState.update {
                            applyQuestionState(completedState, 0)
                        }
                    }

                    else -> {
                        withContext(Dispatchers.IO) {
                            val initialProgress = ModuleProgressEntity(
                                moduleId = moduleId,
                                score = 0,
                                totalQuestions = questions.size,
                                longestStreak = 0,
                                skippedQuestions = questions.size,
                                isCompleted = false,
                                completedAt = 0L,
                                lastQuestionIndex = 0
                            )
                            repository.saveModuleProgress(initialProgress)
                        }

                        _uiState.update {
                            QuizUiState(
                                isLoading = false,
                                questions = questions,
                                userAnswers = emptyMap(),
                                isShowingResults = false
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun reattemptQuiz() {
        val moduleId = currentModuleId ?: return
        val questions = _uiState.value.questions
        if (questions.isEmpty()) return

        viewModelScope.launch {
            resetModuleProgress(moduleId)

            val initialProgress = ModuleProgressEntity(
                moduleId = moduleId,
                score = 0,
                totalQuestions = questions.size,
                longestStreak = 0,
                skippedQuestions = questions.size,
                isCompleted = false,
                completedAt = 0L,
                lastQuestionIndex = 0
            )
            repository.saveModuleProgress(initialProgress)

            _uiState.update {
                QuizUiState(
                    questions = questions,
                    isShowingResults = false,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun resetModuleProgress(moduleId: String) {
        repository.deleteAnswersForModule(moduleId)
    }

    fun selectAnswer(selectedOptionIndex: Int) {
        val currentState = _uiState.value
        if (currentState.isAnswerRevealed) return

        val currentQuestion = currentState.currentQuestion ?: return
        val currentQuestionId = currentQuestion.id
        val moduleId = currentModuleId ?: return

        if (currentState.userAnswers.containsKey(currentQuestionId)) {
            return
        }

        val isCorrect = selectedOptionIndex == currentQuestion.correctOptionIndex

        val answerEntity = UserAnswerEntity(
            moduleId = moduleId,
            questionId = currentQuestionId,
            selectedOptionIndex = selectedOptionIndex,
            isCorrect = isCorrect
        )

        viewModelScope.launch {
            repository.saveUserAnswer(answerEntity)
        }

        _uiState.update {
            it.copy(
                selectedAnswer = currentQuestion.options[selectedOptionIndex],
                isAnswerRevealed = true,
                userAnswers = it.userAnswers + (currentQuestionId to selectedOptionIndex)
            )
        }

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
        nextQuestion()
    }

    fun moveToNextQuestion() {
        nextQuestion()
    }

    private fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _uiState.update {
                applyQuestionState(currentState, currentState.currentQuestionIndex + 1)
            }
            saveCurrentProgress()
        } else {
            submitQuizAndFinish()
        }
    }

    fun previousQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            _uiState.update {
                applyQuestionState(currentState, currentState.currentQuestionIndex - 1)
            }
            saveCurrentProgress()
        }
    }

    fun submitQuizAndFinish() {
        _uiState.update { it.copy(isQuizFinished = true) }
    }

    fun saveProgressAndFinish(onSaved: () -> Unit) {
        viewModelScope.launch {
            currentModuleId?.let { moduleId ->
                val state = _uiState.value
                val skippedCount = state.skippedQuestionsCount

                val progress = ModuleProgressEntity(
                    moduleId = moduleId,
                    score = state.correctAnswersCount,
                    totalQuestions = state.questions.size,
                    longestStreak = state.longestStreak,
                    skippedQuestions = skippedCount,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis(),
                    lastQuestionIndex = state.currentQuestionIndex
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
                val skippedCount = state.skippedQuestionsCount

                val progress = ModuleProgressEntity(
                    moduleId = moduleId,
                    score = state.correctAnswersCount,
                    totalQuestions = state.questions.size,
                    longestStreak = state.longestStreak,
                    skippedQuestions = skippedCount,
                    isCompleted = false,
                    completedAt = 0L,
                    lastQuestionIndex = state.currentQuestionIndex
                )
                repository.saveModuleProgress(progress)
            }
        }
    }

    fun exitQuiz(onExitComplete: () -> Unit) {
        val moduleId = currentModuleId ?: return
        val state = _uiState.value

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val skippedCount = state.skippedQuestionsCount

                val progress = ModuleProgressEntity(
                    moduleId = moduleId,
                    score = state.correctAnswersCount,
                    totalQuestions = state.questions.size,
                    longestStreak = state.longestStreak,
                    skippedQuestions = skippedCount,
                    isCompleted = false,
                    completedAt = 0L,
                    lastQuestionIndex = state.currentQuestionIndex
                )
                repository.saveModuleProgress(progress)
            }
            onExitComplete()
        }
    }

    fun resetQuiz() {
        _uiState.update { QuizUiState() }
        currentModuleId = null
    }


    private fun applyQuestionState(state: QuizUiState, questionIndex: Int): QuizUiState {
        val question = state.questions.getOrNull(questionIndex) ?: return state
        val selectedAnswerIndex = state.userAnswers[question.id]

        return if (selectedAnswerIndex != null) {
            state.copy(
                currentQuestionIndex = questionIndex,
                selectedAnswer = question.options[selectedAnswerIndex],
                isAnswerRevealed = true,
                isAlreadyAnswered = true
            )
        } else {
            state.copy(
                currentQuestionIndex = questionIndex,
                selectedAnswer = null,
                isAnswerRevealed = false,
                isAlreadyAnswered = false
            )
        }
    }
}