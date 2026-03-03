package com.example.extsecure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.extsecure.viewmodel.ScanViewModel
import com.example.extsecure.viewmodel.ScanUiState

@Composable
fun DetailScreen(
    extensionId: String,
    viewModel: ScanViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0F12))
            .padding(24.dp)
    ) {
        if (uiState is ScanUiState.Success) {
            val result = (uiState as ScanUiState.Success).response
            PremiumResultCard(result)
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No data available", color = Color.Gray)
            }
        }
    }
}