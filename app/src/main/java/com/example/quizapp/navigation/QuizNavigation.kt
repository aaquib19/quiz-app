package com.example.quizapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quizapp.ui.viewmodel.QuizViewModel
import com.example.quizapp.ui.screens.QuizScreen
import com.example.quizapp.ui.screens.ResultsScreen
import com.example.quizapp.ui.screens.SplashScreen

@Composable
fun QuizNavigation(quizViewModel: QuizViewModel) {
    val navController = rememberNavController()
    val uiState by quizViewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                isLoading = uiState.isLoading,
                onQuizReady = { navController.navigate("quiz") }
            )
        }
        composable("quiz") {
            QuizScreen(
                uiState = uiState,
                onSelectAnswer = { quizViewModel.selectAnswer(it) },
                onSkip = { quizViewModel.skipQuestion() },
                onPrevious = { quizViewModel.previousQuestion() }
            )
        }
        composable("results") {
            ResultsScreen(
                correctAnswers = uiState.correctAnswersCount,
                totalQuestions = uiState.questions.size,
                longestStreak = uiState.longestStreak,
                skippedQuestions = uiState.skippedQuestionsCount,
                onRestart = {
                    quizViewModel.resetQuiz()
                    navController.navigate("quiz") {
                        popUpTo("quiz") { inclusive = true }
                    }
                }
            )
        }
    }

    LaunchedEffect(uiState.isQuizFinished) {
        if (uiState.isQuizFinished) {
            navController.navigate("results") {
                popUpTo("quiz") { inclusive = true }
            }
        }
    }
}