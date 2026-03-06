package com.example.extsecure.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface ApiService {
    @POST("analyze")
    suspend fun analyzeExtension(
        @Body request: AnalyzeRequest
    ): Response<AnalyzeResponse>
}