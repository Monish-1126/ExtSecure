package com.example.extsecure.api

import com.google.gson.annotations.SerializedName

data class AnalyzeRequest(
    @SerializedName("extension_id") val extension_id: String
)

data class AnalyzeResponse(
    @SerializedName("extension_id") val extension_id: String,
    @SerializedName("risk_score")   val riskScore: Float,
    @SerializedName("risk_level")   val riskLevel: String
)