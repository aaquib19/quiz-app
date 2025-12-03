package com.example.quizapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "user_answer",
    foreignKeys = [
        ForeignKey(
            entity = ModuleProgressEntity::class,
            parentColumns = ["moduleId"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index(value = ["moduleId"])]
)
data class UserAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val moduleId: String,
    val questionId: Int,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val answeredAt: Long = System.currentTimeMillis()
)