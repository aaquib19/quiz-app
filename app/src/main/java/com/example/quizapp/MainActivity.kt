package com.example.quizapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quizapp.data.QuizRepository
import com.example.quizapp.data.local.QuizDatabase
import com.example.quizapp.navigation.QuizNavigation
import com.example.quizapp.ui.theme.QuizAppTheme
import com.example.quizapp.ui.viewmodel.ModuleListViewModel
import com.example.quizapp.ui.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {

    // 1. Lazily create the database and the FULLY INITIALIZED repository
    private val database by lazy { QuizDatabase.getDatabase(applicationContext) }
    private val repository by lazy {
        QuizRepository(
            moduleProgressDao = database.moduleProgressDao(),
            userAnswerDao = database.userAnswerDao() // <-- FIX: Add the second DAO here
        )
    }

    // 2. Create a single factory that provides the repository to ViewModels
    private val appViewModelFactory by lazy { AppViewModelFactory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 3. Use the factory to create your ViewModels
                    val moduleListViewModel: ModuleListViewModel = viewModel(factory = appViewModelFactory)
                    val quizViewModel: QuizViewModel = viewModel(factory = appViewModelFactory)

                    QuizNavigation(
                        moduleListViewModel = moduleListViewModel,
                        quizViewModel = quizViewModel
                    )
                }
            }
        }
    }
}

/**
 * A ViewModelProvider.Factory that creates ViewModels with a shared QuizRepository.
 * This is a clean way to manage dependencies for your ViewModels.
 */
/**
 * A ViewModelProvider.Factory that creates ViewModels with a shared QuizRepository.
 * This is a clean way to manage dependencies for your ViewModels.
 */
class AppViewModelFactory(private val repository: QuizRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T { // <-- FIX: Changed 'Class' to 'Class<T>'
        return when {
            modelClass.isAssignableFrom(ModuleListViewModel::class.java) -> {
                ModuleListViewModel(repository) as T
            }
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> {
                QuizViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}