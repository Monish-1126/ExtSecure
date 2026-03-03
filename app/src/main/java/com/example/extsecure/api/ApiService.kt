package com.example.extsecure.api

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("analyze")
    suspend fun analyzeExtension(
        @Body request: AnalyzeRequest
    ): AnalyzeResponse
}