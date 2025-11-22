package com.example.quizapp.ui.theme

import com.example.quizapp.data.Question

data class QuizUiState(
    val isLoading: Boolean = true,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null,
    val isAnswerRevealed: Boolean = false,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val correctAnswersCount: Int = 0,
    val skippedQuestionsCount: Int = 0,
    val isQuizFinished: Boolean = false
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentQuestionIndex)
}