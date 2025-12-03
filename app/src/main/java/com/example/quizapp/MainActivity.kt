package com.example.quizapp

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val database = QuizDatabase.getDatabase(applicationContext)
                    val repository = QuizRepository(database.moduleProgressDao())

                    val factory = ViewModelFactory(repository)

                    val moduleListViewModel: ModuleListViewModel = viewModel(factory = factory)
                    val quizViewModel: QuizViewModel = viewModel(factory = factory)

                    QuizNavigation(
                        moduleListViewModel = moduleListViewModel,
                        quizViewModel = quizViewModel
                    )
                }
            }
        }
    }
}

class ViewModelFactory(private val repository: QuizRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ModuleListViewModel::class.java) -> {
                ModuleListViewModel(repository) as T
            }
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> {
                QuizViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}