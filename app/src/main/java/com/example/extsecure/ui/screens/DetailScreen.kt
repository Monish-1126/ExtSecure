package com.example.extsecure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.extsecure.api.AnalyzeResponse
import com.example.extsecure.viewmodel.ScanViewModel
import com.example.extsecure.viewmodel.ScanUiState

@Composable
fun DetailScreen(
    extensionId: String,
    viewModel: ScanViewModel
) {

    val scan by viewModel.getScanByExtensionId(extensionId).observeAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0F12))
            .padding(24.dp)
    ) {

        scan?.let {

            PremiumResultCard(
                AnalyzeResponse(
                    extensionId = it.extensionId,
                    extensionName = it.extensionName,
                    description = it.description,
                    version = it.version,
                    permissions = it.permissions.split(",").filter { p -> p.isNotBlank() },
                    riskScore = it.riskScore,
                    riskLevel = it.riskLevel
                )
            )

        } ?: Text("Loading...", color = Color.Gray)
    }
}