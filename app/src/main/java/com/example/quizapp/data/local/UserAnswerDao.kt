package com.example.quizapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAnswer(answer: UserAnswerEntity): Long

    @Query("SELECT * FROM user_answer WHERE moduleId = :moduleId")
    fun getAnswersForModule(moduleId: String): Flow<List<UserAnswerEntity>>

    @Query("SELECT * FROM user_answer WHERE moduleId = :moduleId")
    suspend fun getAnswersForModuleSync(moduleId: String): List<UserAnswerEntity>

    @Query("SELECT * FROM user_answer WHERE moduleId = :moduleId AND questionId = :questionId LIMIT 1")
    suspend fun getAnswerForQuestion(moduleId: String, questionId: Int): UserAnswerEntity?

    @Query("DELETE FROM user_answer WHERE moduleId = :moduleId")
    suspend fun deleteAnswersForModule(moduleId: String)

    @Query("SELECT * FROM user_answer")
    suspend fun getAllAnswers(): List<UserAnswerEntity>
}