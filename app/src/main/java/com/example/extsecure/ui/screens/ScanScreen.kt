package com.example.extsecure.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.extsecure.ui.components.RiskBadge
import com.example.extsecure.ui.components.riskColor
import com.example.extsecure.viewmodel.ScanUiState
import com.example.extsecure.viewmodel.ScanViewModel

/**
 * Extracts Chrome extension IDs from lines that may contain full Chrome Web Store URLs.
 * Supports formats like:
 *   https://chromewebstore.google.com/detail/some-name/abcdefghijklmnopqrstuvwxyzabcdef
 *   abcdefghijklmnopqrstuvwxyzabcdef
 */
private val WEBSTORE_URL_REGEX =
    Regex("""https?://chromewebstore\.google\.com/detail/[^/]*/([a-z]{32})""")

private fun extractExtensionId(line: String): String {
    val match = WEBSTORE_URL_REGEX.find(line.trim())
    return match?.groupValues?.get(1) ?: line.trim()
}

@Composable
fun ScanScreen(
    scanViewModel: ScanViewModel,
    navController: NavController
) {
    val uiState by scanViewModel.uiState.collectAsState()
    val isNetworkAvailable by scanViewModel.isNetworkAvailable.collectAsState()
    val colors = MaterialTheme.colorScheme

    var extensionId by remember { mutableStateOf("") }

    // Compute IDs — supports newline, comma, semicolon separated input
    val ids = remember(extensionId) {
        extensionId
            .split("\n", "\r\n", ",", ";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { extractExtensionId(it) }
            .filter { it.isNotEmpty() && it.matches(Regex("[a-z]{32}")) }
            .distinct()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(24.dp)
        ) {
            // ── SUBTITLE ──
            item {
                Text(
                    text = "Analyze Chrome extensions for malicious behavior",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant
                )
            }

            // ── NETWORK WARNING ──
            if (!isNetworkAvailable) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⚠ No internet connection",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // ── INPUT CARD ──
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = extensionId,
                            onValueChange = { extensionId = it },
                            label = { Text("Extension IDs or URLs") },
                            placeholder = {
                                Text(
                                    "Paste one extension ID or Chrome Web Store URL per line\n" +
                                            "Example:\nkbfnbcaeplbcioakkpcpgfkobkghlhen\nhttps://chromewebstore.google.com/detail/name/id",
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            ),
                            singleLine = false,
                            maxLines = 20,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.outline
                            )
                        )

                        Text(
                            text = "${ids.size} extension(s) ready to scan",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )

                        val isEnabled = isNetworkAvailable && ids.isNotEmpty()
                        val buttonAlpha = if (isEnabled) 1f else 0.4f

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = if (isEnabled) 8.dp else 0.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = colors.primary.copy(alpha = 0.3f),
                                    spotColor = colors.primary.copy(alpha = 0.3f)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            colors.primary.copy(alpha = buttonAlpha),
                                            Color(0xFF6D28D9).copy(alpha = buttonAlpha)
                                        )
                                    )
                                )
                                .then(
                                    if (isEnabled) Modifier.clickable {
                                        scanViewModel.analyzeExtensionsBatch(ids)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (ids.isEmpty()) "Enter IDs to Analyze"
                                           else "Analyze ${ids.size} Extension(s)",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── RESULTS HEADER ──
            item {
                Text(
                    text = "Scan Results",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onBackground
                )
                if (uiState is ScanUiState.Success) {
                    val results = (uiState as ScanUiState.Success).responses
                    val successCount = results.count { it.response != null }
                    val failCount = results.count { it.error != null }
                    Text(
                        text = "$successCount success · $failCount failed",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // ── RESULT CARDS ──
            if (uiState is ScanUiState.Success) {
                val results = (uiState as ScanUiState.Success).responses

                results.forEach { result ->
                    item {
                        if (result.response != null) {
                            val extension = result.response
                            val rColor = riskColor(extension.riskLevel)

                            Card(
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                shape = RoundedCornerShape(22.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable {
                                        navController.navigate("detail/${extension.extensionId}")
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(22.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        extension.extensionName,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        "Version ${extension.version}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                    Text(
                                        extension.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurface.copy(alpha = 0.8f)
                                    )
                                    HorizontalDivider(color = colors.outline.copy(alpha = 0.3f))
                                    Text(
                                        "Risk Score",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${(extension.riskScore * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = rColor
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        RiskBadge(level = extension.riskLevel)
                                    }
                                }
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.errorContainer
                                ),
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Extension ID: ${result.extensionId}",
                                        color = colors.onSurfaceVariant
                                    )
                                    Text(
                                        text = result.error ?: "Unknown error",
                                        color = colors.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── ERROR STATE ──
            if (uiState is ScanUiState.Error) {
                item {
                    val message = (uiState as ScanUiState.Error).message
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = colors.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = message,
                            color = colors.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // ── IDLE HINT ──
            if (uiState is ScanUiState.Idle) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Enter extension IDs above and tap Analyze",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── LOADING OVERLAY ──
        if (uiState is ScanUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        color = colors.primary,
                        strokeWidth = 5.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Analyzing extensions…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onBackground
                    )
                }
            }
        }
    }
}
