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
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
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
            // Title - with null safety
            Text(
                text = module.title ?: "Unknown Module",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description - with null safety
            Text(
                text = module.description ?: "No description available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Progress info - only if progress exists
            if (progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${progress.totalQuestions} Questions | Score: ${progress.score}/${progress.totalQuestions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = QuizDarkPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Button
            Button(
                onClick = { onModuleClick(module.id, module.questions_url) },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuizDarkPrimary
                )
            ) {
                Text(if (progress?.isCompleted == true) "Review" else "Start")
            }
        }
    }
}