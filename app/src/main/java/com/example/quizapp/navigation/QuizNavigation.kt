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

private object NavigationRoutes {
    const val MODULE_LIST = "moduleList"
    const val QUIZ = "quiz/{moduleId}/{questionsUrl}"
    const val RESULTS = "results"

    fun quiz(moduleId: String, questionsUrl: String): String {
        val encodedUrl = Uri.encode(questionsUrl)
        return "quiz/$moduleId/$encodedUrl"
    }
}

private object NavigationArgs {
    const val MODULE_ID = "moduleId"
    const val QUESTIONS_URL = "questionsUrl"
}

@Composable
fun QuizNavigation(
    moduleListViewModel: ModuleListViewModel,
    quizViewModel: QuizViewModel
) {
    val navController = rememberNavController()
    val moduleListState by moduleListViewModel.uiState.collectAsState()
    val quizState by quizViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.MODULE_LIST
    ) {
        composable(NavigationRoutes.MODULE_LIST) {
            ModuleListScreen(
                uiState = moduleListState,
                onModuleClick = { moduleId, questionsUrl ->
                    navController.navigate(NavigationRoutes.quiz(moduleId, questionsUrl))
                }
            )
        }

        composable(
            route = NavigationRoutes.QUIZ,
            arguments = listOf(
                navArgument(NavigationArgs.MODULE_ID) { type = NavType.StringType },
                navArgument(NavigationArgs.QUESTIONS_URL) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString(NavigationArgs.MODULE_ID)
                ?: return@composable
            val encodedUrl = backStackEntry.arguments?.getString(NavigationArgs.QUESTIONS_URL)
                ?: return@composable
            val questionsUrl = Uri.decode(encodedUrl)

            LaunchedEffect(moduleId) {
                quizViewModel.loadQuestionsForModule(moduleId, questionsUrl)
            }

            QuizScreen(
                uiState = quizState,
                onSelectAnswer = quizViewModel::selectAnswer,
                onExit = {
                    quizViewModel.exitQuiz {
                        navController.popBackStack()
                    }
                },
                onSkip = quizViewModel::skipQuestion,
                onPrevious = quizViewModel::previousQuestion,
                viewModel = quizViewModel
            )
        }

        composable(NavigationRoutes.RESULTS) {
            ResultsScreen(
                correctAnswers = quizState.correctAnswersCount,
                totalQuestions = quizState.questions.size,
                longestStreak = quizState.longestStreak,
                skippedQuestions = quizState.skippedQuestionsCount,
                onFinish = {
                    quizViewModel.saveProgressAndFinish {
                        moduleListViewModel.refreshModules()
                        navController.navigate(NavigationRoutes.MODULE_LIST) {
                            popUpTo(NavigationRoutes.MODULE_LIST) { inclusive = true }
                        }
                    }
                }
            )
        }
    }

    LaunchedEffect(quizState.isQuizFinished) {
        if (quizState.isQuizFinished) {
            navController.navigate(NavigationRoutes.RESULTS) {
                popUpTo(NavigationRoutes.QUIZ) { inclusive = true }
            }
        }
    }
}