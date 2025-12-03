package com.example.quizapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleProgressDao {
    @Query("SELECT * FROM module_progress WHERE moduleId = :moduleId")
    suspend fun getProgress(moduleId: String): ModuleProgressEntity?

    @Query("SELECT * FROM module_progress")
    fun getAllProgress(): Flow<List<ModuleProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun saveProgress(progress: ModuleProgressEntity)

    @Query("DELETE FROM module_progress WHERE moduleId = :moduleId")
    suspend fun deleteProgress(moduleId: String)
}