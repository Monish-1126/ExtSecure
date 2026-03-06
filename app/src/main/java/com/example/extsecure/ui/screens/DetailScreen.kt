package com.example.extsecure.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.extsecure.api.AnalyzeResponse
import com.example.extsecure.ui.components.RiskBadge
import com.example.extsecure.ui.components.riskColor
import com.example.extsecure.viewmodel.ScanViewModel
import com.example.extsecure.viewmodel.ScanUiState

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