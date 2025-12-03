package com.example.quizapp.network

import com.example.quizapp.data.Module
import com.example.quizapp.data.Question
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET("53846277a8fcb034e482906ccc0d12b2/raw")
    suspend fun getQuestions(): List<Question>

    @GET("ee986f16da9d8303c1acfd364ece22c5/raw")
    suspend fun getModules(): List<Module>

    @GET
    suspend fun getQuestionsFromUrl(@Url url: String): List<Question>
}