package com.example.quizapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onRestart: () -> Unit
) {
    val scorePercentage = if (totalQuestions > 0) {
        (correctAnswers.toFloat() / totalQuestions * 100)
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QuizDarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Results",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = QuizDarkOnSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        CircularScoreIndicator(
            score = scorePercentage,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        val message = when {
            scorePercentage >= 80 -> "Excellent work!"
            scorePercentage >= 60 -> "Good job!"
            scorePercentage >= 40 -> "Not bad, keep practicing!"
            else -> "Keep trying, you'll do better next time!"
        }
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = QuizDarkOnSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(title = "Longest Streak", value = "$longestStreak", modifier = Modifier.weight(1f))
            StatCard(title = "Skipped", value = "$skippedQuestions", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = QuizDarkPrimary)
        ) {
            Text(text = "Restart Quiz", style = MaterialTheme.typography.titleMedium, color = QuizDarkOnPrimary)
        }
    }
}

@Composable
fun CircularScoreIndicator(score: Float, modifier: Modifier = Modifier) {
    val animatedScore by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1000), label = "progressAnimation"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 16.dp.toPx()
            val sweepAngle = (animatedScore / 100f) * 360f

            drawCircle(
                color = QuizDarkSurface,
                radius = (size.minDimension - strokeWidth) / 2,
                style = Stroke(width = strokeWidth)
            )

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
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = QuizDarkOnSurface
            )
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = QuizDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = QuizDarkPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = QuizDarkOnSurface
            )
        }
    }
}