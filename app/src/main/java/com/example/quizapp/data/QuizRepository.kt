package com.example.quizapp.data

import com.example.quizapp.data.local.ModuleProgressDao
import com.example.quizapp.data.local.ModuleProgressEntity
import com.example.quizapp.network.RetrofitClient
import kotlinx.coroutines.flow.Flow

class QuizRepository(private val moduleProgressDao: ModuleProgressDao) {
    private val apiService = RetrofitClient.apiService

    suspend fun getModules(): List<Module> {
        return apiService.getModules()
    }

    suspend fun getQuestionsForModule(questionsUrl: String): List<Question> {
        return apiService.getQuestionsFromUrl(questionsUrl)
    }

    // Local persistence methods
    suspend fun saveModuleProgress(progress: ModuleProgressEntity) {
        moduleProgressDao.saveProgress(progress)
    }

    suspend fun getModuleProgress(moduleId: String): ModuleProgressEntity? {
        return moduleProgressDao.getProgress(moduleId)
    }

    fun getAllProgress(): Flow<List<ModuleProgressEntity>> {
        return moduleProgressDao.getAllProgress()
    }
}