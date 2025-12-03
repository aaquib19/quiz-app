package com.example.quizapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.ui.theme.*

@Composable
fun ResultsScreen(
    correctAnswers: Int,
    totalQuestions: Int,
    longestStreak: Int,
    skippedQuestions: Int,
    onFinish: () -> Unit
) {
    val scorePercentage = if (totalQuestions > 0) {
        (correctAnswers.toFloat() / totalQuestions * 100)
    } else {
        0f
    }

    Column(
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Results",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = QuizDarkOnSurface,
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        CircularScoreIndicator(
            score = scorePercentage,
            modifier = Modifier.size(220.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        val message = when {
            scorePercentage >= 80 -> "Excellent work!"
            scorePercentage >= 60 -> "Good job!"
            scorePercentage >= 40 -> "Not bad, keep practicing!"
            else -> "Keep trying, you'll do better next time!"
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = QuizDarkPrimary.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = QuizDarkOnSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Longest Streak",
                value = "$longestStreak",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Skipped",
                value = "$skippedQuestions",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QuizDarkPrimary),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "Finish",
                style = MaterialTheme.typography.titleMedium,
                color = QuizDarkOnPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CircularScoreIndicator(score: Float, modifier: Modifier = Modifier) {
    val animatedScore by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1200),
        label = "progressAnimation"
    )

    Card(
        modifier = modifier,
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = QuizDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val strokeWidth = 18.dp.toPx()
                val sweepAngle = (animatedScore / 100f) * 360f

                // Background circle
                drawCircle(
                    color = QuizDarkBackground,
                    radius = (size.minDimension - strokeWidth) / 2,
                    style = Stroke(width = strokeWidth)
                )

                // Progress arc
                drawArc(
                    color = if (score >= 80) CorrectGreen else QuizDarkPrimary,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${animatedScore.toInt()}%",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = QuizDarkOnSurface,
                    letterSpacing = (-2).sp
                )
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = QuizDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = QuizDarkPrimary,
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = QuizDarkOnSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}