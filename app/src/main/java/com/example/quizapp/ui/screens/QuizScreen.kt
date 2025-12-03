package com.example.quizapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.data.Question
import com.example.quizapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(
    uiState: QuizUiState,
    onSelectAnswer: (Int) -> Unit,
    onSubmitQuiz: () -> Unit,
    onSkip: () -> Unit,
    onPrevious: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        QuizDarkBackground,
                        QuizDarkBackground.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = QuizDarkPrimary,
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading questions...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuizDarkOnSurface.copy(alpha = 0.7f)
                )
            }
        } else if (uiState.questions.isEmpty()) {
            ErrorState()
        } else {
            uiState.currentQuestion?.let { question ->
                val isLastQuestion = uiState.currentQuestionIndex == uiState.questions.size - 1
                var hasNavigated by remember { mutableStateOf(false) }
                var swipeDirection by remember { mutableStateOf<Int?>(null) }

                AnimatedContent(
                    targetState = uiState.currentQuestionIndex,
                    transitionSpec = {
                        if (targetState > initialState) { // Next question
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) with slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            )
                        } else { // Previous question
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300)
                            ) with slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            )
                        }
                    },
                    label = "questionTransition"
                ) { _ ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { _, dragAmount ->
                                        if (!uiState.isAnswerRevealed) {
                                            if (!hasNavigated && dragAmount.x > 50) {
                                                hasNavigated = true
                                                swipeDirection = 1
                                            }
                                            else if (!hasNavigated && dragAmount.x < -50) {
                                                hasNavigated = true
                                                swipeDirection = -1
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        if (hasNavigated) {
                                            if (swipeDirection == 1) {
                                                onPrevious()
                                            } else if (swipeDirection == -1) {
                                                onSkip()
                                            }
                                        }
                                        hasNavigated = false
                                        swipeDirection = null
                                    }
                                )
                            }
                    ) {
                        LinearProgressIndicator(
                            progress = (uiState.currentQuestionIndex + 1).toFloat() / uiState.questions.size,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = QuizDarkPrimary,
                            trackColor = QuizDarkSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        QuestionHeader(
                            questionNumber = uiState.currentQuestionIndex + 1,
                            totalQuestions = uiState.questions.size,
                            currentStreak = uiState.currentStreak
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        QuestionCard(question = question.question)

                        Spacer(modifier = Modifier.height(40.dp))

                        OptionsList(
                            question = question,
                            selectedAnswer = uiState.selectedAnswer,
                            isRevealed = uiState.isAnswerRevealed,
                            onOptionSelected = onSelectAnswer
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Always show all three buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onPrevious,
                                enabled = uiState.currentQuestionIndex > 0 && !uiState.isAnswerRevealed
                            ) {
                                Text(
                                    text = "← Previous",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (uiState.currentQuestionIndex > 0 && !uiState.isAnswerRevealed) QuizDarkPrimary else Color.Gray
                                )
                            }

                            Button(
                                onClick = onSubmitQuiz,
                                enabled = true,
                                modifier = Modifier
                                    .height(48.dp)
                                    .width(120.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = QuizDarkPrimary,
                                    disabledContainerColor = QuizDarkPrimary.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = "Submit",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = QuizDarkOnPrimary
                                )
                            }

                            TextButton(
                                onClick = onSkip,
                                // Disable on the last question since there is no "next" question
                                enabled = !uiState.isAnswerRevealed && !isLastQuestion
                            ) {
                                Text(
                                    text = "Next →",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (!uiState.isAnswerRevealed && !isLastQuestion) QuizDarkOnSurface.copy(alpha = 0.6f) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- The rest of the composables in this file remain the same ---

@Composable
fun ErrorState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(IncorrectRed.copy(alpha = 0.2f))
                .padding(16.dp),
            tint = IncorrectRed
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = QuizDarkOnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please check your internet connection and restart the app.",
            style = MaterialTheme.typography.bodyMedium,
            color = QuizDarkOnSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun QuestionHeader(
    questionNumber: Int,
    totalQuestions: Int,
    currentStreak: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "QUESTION $questionNumber",
                style = MaterialTheme.typography.labelMedium,
                color = QuizDarkPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "of $totalQuestions",
                style = MaterialTheme.typography.bodySmall,
                color = QuizDarkOnSurface.copy(alpha = 0.6f)
            )
        }
        StreakCounter(streak = currentStreak)
    }
}

@Composable
fun StreakCounter(streak: Int) {
    val isActive = streak >= 3
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) StreakActiveColor else QuizDarkSurface,
        animationSpec = tween(400), label = "bgColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = tween(400), label = "scale"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .scale(scale),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Streak",
            tint = if (isActive) Color.White else QuizDarkOnSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$streak",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isActive) Color.White else QuizDarkOnSurface.copy(alpha = 0.5f),
            fontSize = 18.sp
        )
        if (isActive) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "STREAK",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
fun QuestionCard(question: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = QuizDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = question,
            modifier = Modifier.padding(28.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = QuizDarkOnSurface,
            lineHeight = 32.sp
        )
    }
}

@Composable
fun OptionsList(
    question: Question,
    selectedAnswer: String?,
    isRevealed: Boolean,
    onOptionSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        question.options.forEachIndexed { index, option ->
            val delay = index * 50
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300, delayMillis = delay)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(300, delayMillis = delay)
                        )
            ) {
                OptionButton(
                    text = option,
                    optionLabel = ('A' + index).toString(),
                    isSelected = option == selectedAnswer,
                    isCorrect = index == question.correctOptionIndex,
                    isRevealed = isRevealed,
                    onClick = { onOptionSelected(index) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionButton(
    text: String,
    optionLabel: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isRevealed: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !isRevealed && isSelected -> QuizDarkPrimary.copy(alpha = 0.15f)
            !isRevealed -> QuizDarkSurface
            isCorrect -> CorrectGreen.copy(alpha = 0.2f)
            isSelected && !isCorrect -> IncorrectRed.copy(alpha = 0.2f)
            else -> QuizDarkSurface
        },
        animationSpec = tween(400), label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            !isRevealed && isSelected -> QuizDarkPrimary
            !isRevealed -> Color.Transparent
            isCorrect -> CorrectGreen
            isSelected && !isCorrect -> IncorrectRed
            else -> Color.Transparent
        },
        animationSpec = tween(400), label = "borderColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected && !isRevealed) 0.98f else 1f,
        animationSpec = tween(200), label = "scale"
    )

    Card(
        onClick = onClick,
        enabled = !isRevealed,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected && !isRevealed) 4.dp else 2.dp
        ),
        border = if (borderColor != Color.Transparent) {
            androidx.compose.foundation.BorderStroke(2.dp, borderColor)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !isRevealed && isSelected -> QuizDarkPrimary
                            !isRevealed -> QuizDarkOnSurface.copy(alpha = 0.1f)
                            isCorrect -> CorrectGreen
                            isSelected && !isCorrect -> IncorrectRed
                            else -> QuizDarkOnSurface.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isRevealed && isCorrect) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (isRevealed && isSelected && !isCorrect) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = optionLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    !isRevealed -> QuizDarkOnSurface
                    isCorrect -> CorrectGreen
                    isSelected && !isCorrect -> IncorrectRed
                    else -> Color.Gray
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}