package com.example.extsecure.api

import com.google.gson.annotations.SerializedName

data class AnalyzeRequest(
    @SerializedName("extension_id") val extension_id: String
)

data class AnalyzeResponse(

    @SerializedName("extension_id")
    val extensionId: String,

    @SerializedName("extension_name")
    val extensionName: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("version")
    val version: String,

    @SerializedName("permissions")
    val permissions: List<String>? = emptyList(),

    @SerializedName("risk_score")
    val riskScore: Float,

    @SerializedName("risk_level")
    val riskLevel: String
)