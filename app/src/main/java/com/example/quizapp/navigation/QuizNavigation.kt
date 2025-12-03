package com.example.quizapp.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quizapp.ui.screens.ModuleListScreen
import com.example.quizapp.ui.screens.QuizScreen
import com.example.quizapp.ui.screens.ResultsScreen
import com.example.quizapp.ui.viewmodel.ModuleListViewModel
import com.example.quizapp.ui.viewmodel.QuizViewModel

@Composable
fun QuizNavigation(
    moduleListViewModel: ModuleListViewModel,
    quizViewModel: QuizViewModel
) {
    val navController = rememberNavController()
    val moduleListState by moduleListViewModel.uiState.collectAsState()
    val quizState by quizViewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = "moduleList") {
        composable("moduleList") {
            ModuleListScreen(
                uiState = moduleListState,
                onModuleClick = { moduleId, questionsUrl ->
                    quizViewModel.resetQuiz()
                    val encodedUrl = Uri.encode(questionsUrl)
                    navController.navigate("quiz/$moduleId/$encodedUrl")
                }
            )
        }

        composable(
            route = "quiz/{moduleId}/{questionsUrl}",
            arguments = listOf(
                navArgument("moduleId") { type = NavType.StringType },
                navArgument("questionsUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: return@composable
            val encodedUrl = backStackEntry.arguments?.getString("questionsUrl") ?: return@composable
            val questionsUrl = Uri.decode(encodedUrl)

            LaunchedEffect(moduleId) {
                quizViewModel.loadQuestionsForModule(moduleId, questionsUrl)
            }

            QuizScreen(
                uiState = quizState,
                onSelectAnswer = { quizViewModel.selectAnswer(it) },
                // UPDATED: Use the new manual submission function
                onSubmitQuiz = { quizViewModel.submitQuizAndFinish() },
                onSkip = { quizViewModel.skipQuestion() },
                onPrevious = { quizViewModel.previousQuestion() }
            )
        }

        composable("results") {
            ResultsScreen(
                correctAnswers = quizState.correctAnswersCount,
                totalQuestions = quizState.questions.size,
                longestStreak = quizState.longestStreak,
                skippedQuestions = quizState.skippedQuestionsCount,
                onFinish = {
                    quizViewModel.saveProgressAndFinish {
                        moduleListViewModel.refreshModules()
                        navController.navigate("moduleList") {
                            popUpTo("moduleList") { inclusive = true }
                        }
                    }
                }
            )
        }
    }

    LaunchedEffect(quizState.isQuizFinished) {
        if (quizState.isQuizFinished) {
            navController.navigate("results") {
                popUpTo("quiz/{moduleId}/{questionsUrl}") { inclusive = true }
            }
        }
    }
}