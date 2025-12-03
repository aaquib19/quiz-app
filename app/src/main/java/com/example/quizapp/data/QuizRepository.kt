package com.example.quizapp.data

import com.example.quizapp.data.local.ModuleProgressDao
import com.example.quizapp.data.local.ModuleProgressEntity
import com.example.quizapp.data.local.UserAnswerDao
import com.example.quizapp.data.local.UserAnswerEntity
import com.example.quizapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class QuizRepository(
    private val moduleProgressDao: ModuleProgressDao,
    private val userAnswerDao: UserAnswerDao
) {
    private val apiService = RetrofitClient.apiService

    // --- Network Methods ---
    suspend fun getModules(): List<Module> {
        return apiService.getModules()
    }

    suspend fun getQuestionsForModule(questionsUrl: String): List<Question> {
        return apiService.getQuestionsFromUrl(questionsUrl)
    }

    // --- Module Progress Methods ---
    suspend fun getModuleProgress(moduleId: String): ModuleProgressEntity? {
        return moduleProgressDao.getProgress(moduleId)
    }

    fun getAllProgress(): Flow<List<ModuleProgressEntity>> {
        return moduleProgressDao.getAllProgress()
    }

    suspend fun saveModuleProgress(progress: ModuleProgressEntity) {
        moduleProgressDao.saveProgress(progress)
    }

    // --- User Answer Methods ---
    suspend fun saveUserAnswer(answer: UserAnswerEntity) = withContext(Dispatchers.IO) {
        userAnswerDao.saveAnswer(answer)
    }

    suspend fun getUserAnswersForModule(moduleId: String): List<UserAnswerEntity> =
        withContext(Dispatchers.IO) {
            userAnswerDao.getAnswersForModuleSync(moduleId)
        }

    suspend fun deleteAnswersForModule(moduleId: String) {
        userAnswerDao.deleteAnswersForModule(moduleId)
    }
}