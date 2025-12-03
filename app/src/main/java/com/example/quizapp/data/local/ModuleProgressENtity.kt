package com.example.quizapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "module_progress")
data class ModuleProgressEntity(
    @PrimaryKey
    val moduleId: String,
    val score: Int,
    val totalQuestions: Int,
    val longestStreak: Int,
    val skippedQuestions: Int,
    val isCompleted: Boolean,
    val completedAt: Long,
    val lastQuestionIndex: Int = 0,
    val answeredQuestions: String = "", // NEW: Store as comma-separated question indices
    val userAnswers: String = "" // NEW: Store as comma-separated answer indices
)