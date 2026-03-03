package com.example.extsecure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.extsecure.ui.components.RiskBadge
import com.example.extsecure.ui.components.riskColor
import com.example.extsecure.ui.theme.BgDark
import com.example.extsecure.ui.theme.CardBg
import com.example.extsecure.viewmodel.ScanUiState
import com.example.extsecure.viewmodel.ScanViewModel
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun HomeScreen(viewModel: ScanViewModel) {

    val uiState by viewModel.uiState.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()

    var extensionId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0F12))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {

        // Header
        Column {
            Text(
                "ExtSecure",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Analyze Chrome extensions for risk",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Input Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1C22)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(Modifier.padding(20.dp)) {

                OutlinedTextField(
                    value = extensionId,
                    onValueChange = { extensionId = it },
                    label = { Text("Extension ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.analyzeExtension(extensionId.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (uiState is ScanUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Analyzing...")
                    } else {
                        Text(
                            "Analyze Extension",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Result Section
        if (uiState is ScanUiState.Success) {
            val result = (uiState as ScanUiState.Success).response
            PremiumResultCard(result.extension_id, result.riskScore, result.riskLevel)
        }

        if (uiState is ScanUiState.Error) {
            Text(
                text = (uiState as ScanUiState.Error).message,
                color = Color(0xFFFF6B6B)
            )
        }
    }
}

@Composable
fun PremiumResultCard(extensionId: String, riskScore: Float, riskLevel: String) {

    val color = riskColor(riskLevel)
    val animatedProgress by animateFloatAsState(targetValue = riskScore)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C22)),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Text(
                "Risk Assessment",
                fontSize = 14.sp,
                color = Color.Gray
            )

            // Circular Risk Indicator
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = animatedProgress,
                    strokeWidth = 10.dp,
                    color = color,
                    modifier = Modifier.size(140.dp)
                )
                Text(
                    "${(riskScore * 100).toInt()}%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            RiskBadge(level = riskLevel)

            Text(
                extensionId,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}