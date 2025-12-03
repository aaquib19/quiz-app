package com.example.quizapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quizapp.ui.theme.ModuleListUiState
import com.example.quizapp.ui.theme.ModuleWithProgress
import com.example.quizapp.ui.theme.QuizDarkPrimary

@Composable
fun ModuleListScreen(
    uiState: ModuleListUiState,
    onModuleClick: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Quiz Modules",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        val errorMessage = when {
                            uiState.error.contains("Unable to resolve host", ignoreCase = true) ->
                                "Internet Connection Required"
                            uiState.error.contains("timeout", ignoreCase = true) ->
                                "Connection Timeout"
                            else ->
                                "Error"
                        }

                        val errorDescription = when {
                            uiState.error.contains("Unable to resolve host", ignoreCase = true) ->
                                "Please check your internet connection and restart the app."
                            uiState.error.contains("timeout", ignoreCase = true) ->
                                "The server is taking too long to respond. Please try again."
                            else ->
                                uiState.error
                        }

                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.modules) { moduleWithProgress ->
                        ModuleCard(
                            moduleWithProgress = moduleWithProgress,
                            onModuleClick = onModuleClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleCard(
    moduleWithProgress: ModuleWithProgress,
    onModuleClick: (String, String) -> Unit
) {
    val module = moduleWithProgress.module
    val progress = moduleWithProgress.progress

    val buttonText = when {
        progress == null -> "Start"
        else -> "Review"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = module.title ?: "Unknown Module",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = module.description ?: "No description available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${progress.totalQuestions} Questions | Score: ${progress.score}/${progress.totalQuestions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = QuizDarkPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onModuleClick(module.id, module.questions_url) },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuizDarkPrimary
                )
            ) {
                Text(buttonText)
            }
        }
    }
}