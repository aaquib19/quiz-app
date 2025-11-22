package com.example.quizapp.data

import com.example.quizapp.network.RetrofitClient

class QuizRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getQuestions(): List<Question> {
        return apiService.getQuestions()
    }
}