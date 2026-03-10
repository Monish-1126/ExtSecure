package com.example.extsecure.ui.screens

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.extsecure.api.AnalyzeResponse
import com.example.extsecure.ui.components.RiskBadge
import com.example.extsecure.ui.components.riskColor
import com.example.extsecure.viewmodel.ScanViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PremiumResultCard(response: AnalyzeResponse) {
    val color = riskColor(response.riskLevel)
    val colors = MaterialTheme.colorScheme

    val animatedProgress by animateFloatAsState(
        targetValue = response.riskScore,
        animationSpec = tween(800)
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
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
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface
                )
                Text(
                    text = "v${response.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = response.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.8f)
                )
            }

            HorizontalDivider(color = colors.outline.copy(alpha = 0.3f))

            // ───────── Risk Meter ─────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    strokeWidth = 10.dp,
                    color = color,
                    trackColor = colors.surfaceVariant,
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    text = "${(response.riskScore * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 28.sp),
                    color = color
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RiskBadge(level = response.riskLevel)
            }

            HorizontalDivider(color = colors.outline.copy(alpha = 0.3f))

            // ───────── Permissions ─────────
            Text(
                text = "Permissions (${response.permissions?.size ?: 0})",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )

            if (response.permissions.isNullOrEmpty()) {
                Text(
                    text = "No permissions requested",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    response.permissions.forEach { permission ->
                        AssistChip(
                            onClick = {},
                            label = { Text(permission, fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colors.surfaceVariant,
                                labelColor = colors.onSurface
                            )
                        )
                    }
                }
            }

            // Subtle ID display
            Text(
                text = response.extensionId,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun DetailScreen(
    extensionId: String,
    viewModel: ScanViewModel,
    navController: NavController
) {
    val scanLiveData = remember(extensionId) { viewModel.getScanByExtensionId(extensionId) }
    val scan by scanLiveData.observeAsState()
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            scan?.let { scanEntity ->
                val response = AnalyzeResponse(
                    extensionId = scanEntity.extensionId,
                    extensionName = scanEntity.extensionName,
                    description = scanEntity.description,
                    version = scanEntity.version,
                    permissions = scanEntity.permissions.split(",").filter { p -> p.isNotBlank() },
                    riskScore = scanEntity.riskScore,
                    riskLevel = scanEntity.riskLevel
                )

                PremiumResultCard(response)

            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        }

        // ── SHARE FAB ──
        scan?.let { scanEntity ->
            FloatingActionButton(
                onClick = {
                    val shareText = buildString {
                        appendLine("🛡 ExtSecure Scan Report")
                        appendLine("━━━━━━━━━━━━━━━━━━━━━━")
                        appendLine("Extension: ${scanEntity.extensionName}")
                        appendLine("Version: ${scanEntity.version}")
                        appendLine("Risk Level: ${scanEntity.riskLevel}")
                        appendLine("Risk Score: ${(scanEntity.riskScore * 100).toInt()}%")
                        appendLine()
                        appendLine("Permissions: ${scanEntity.permissions.ifBlank { "None" }}")
                        appendLine()
                        appendLine("Extension ID: ${scanEntity.extensionId}")
                        appendLine("Chrome Web Store: https://chromewebstore.google.com/detail/-/${scanEntity.extensionId}")
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "ExtSecure: ${scanEntity.extensionName}")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share scan report"))
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        }
    }
}