package com.example.quizapp.network

import com.example.quizapp.data.Question
import retrofit2.http.GET

interface ApiService {
    @GET("53846277a8fcb034e482906ccc0d12b2/raw")
    suspend fun getQuestions(): List<Question>
}