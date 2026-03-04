package com.example.extsecure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.extsecure.api.AnalyzeResponse
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.FlowRow
import com.example.extsecure.ui.components.RiskBadge
import com.example.extsecure.ui.components.riskColor
import com.example.extsecure.viewmodel.ScanUiState
import com.example.extsecure.viewmodel.ScanViewModel
import androidx.compose.ui.graphics.Brush
import com.example.extsecure.ui.theme.CardBg

@Composable
fun HomeScreen(viewModel: ScanViewModel) {

    val uiState by viewModel.uiState.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()

    var extensionId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0E0F12),
                        Color(0xFF12141A)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(36.dp)
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
        if (!isNetworkAvailable) {

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF3B0D0D)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = "⚠ No internet connection",
                    color = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
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
                    onClick = { viewModel.analyzeExtension(extensionId.trim()) },
                    enabled = isNetworkAvailable && uiState !is ScanUiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E7CFF)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    )
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

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = result.extensionName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Version ${result.version}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = result.description,
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )

                    Divider(color = Color.DarkGray)

                    Text(
                        text = "Risk Score",
                        color = Color.Gray
                    )

                    Text(
                        text = "${(result.riskScore * 100).toInt()}%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor(result.riskLevel)
                    )

                    RiskBadge(level = result.riskLevel)

                }
            }
        }

        if (uiState is ScanUiState.Error) {
            Text(
                text = (uiState as ScanUiState.Error).message,
                color = Color(0xFFFF6B6B)
            )
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PremiumResultCard(response: AnalyzeResponse) {

    val color = riskColor(response.riskLevel)

    val animatedProgress by animateFloatAsState(
        targetValue = response.riskScore,
        animationSpec = tween(800)
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1C22)
        ),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ───────── Extension Info ─────────
            Column {
                Text(
                    text = response.extensionName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Text(
                    text = "v${response.version}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = response.description,
                    fontSize = 13.sp,
                    color = Color(0xFFBDBDBD)
                )
            }

            Divider(color = Color.DarkGray)

            // ───────── Risk Meter ─────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = animatedProgress,
                    strokeWidth = 10.dp,
                    color = color,
                    modifier = Modifier.size(120.dp)
                )

                Text(
                    text = "${(response.riskScore * 100).toInt()}%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            RiskBadge(level = response.riskLevel)

            Divider(color = Color.DarkGray)

            // ───────── Permissions ─────────
            Text(
                text = "Permissions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (response.permissions ?: emptyList()).forEach { permission ->
                    AssistChip(
                        onClick = {},
                        label = { Text(permission, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF2A2D36),
                            labelColor = Color.LightGray
                        )
                    )
                }
            }

            // Optional subtle ID display
            Text(
                text = response.extensionId,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        }
    }
}