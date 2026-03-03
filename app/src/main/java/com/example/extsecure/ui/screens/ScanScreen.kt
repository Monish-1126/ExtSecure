package com.example.extsecure.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.extsecure.database.ScanEntity
import com.example.extsecure.ui.components.RiskBadge
import com.example.extsecure.ui.components.riskColor
import com.example.extsecure.ui.theme.*
import com.example.extsecure.viewmodel.ScanUiState
import com.example.extsecure.viewmodel.ScanViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScanScreen(scanViewModel: ScanViewModel = viewModel()) {

    val uiState by scanViewModel.uiState.collectAsState()
    val isNetworkAvailable by scanViewModel.isNetworkAvailable.collectAsState()
    val history by scanViewModel.scanHistory.observeAsState(emptyList())

    var extensionId by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // ── Network Banner ───────────────────────────────────────────────────
        AnimatedVisibility(visible = !isNetworkAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RiskCritical)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚠  No network connection",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }

            // ── Header ────────────────────────────────────────────────────────
            item {
                Text(
                    text = "🔍 Extension Scanner",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Analyze Chrome extensions for malicious behavior",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // ── Input Card ────────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = extensionId,
                            onValueChange = {
                                extensionId = it
                                if (uiState is ScanUiState.Error) scanViewModel.resetState()
                            },
                            label = { Text("Extension ID") },
                            placeholder = {
                                Text(
                                    "e.g. aapbdbdomjkkjkaonfhkkikfgjllcleb",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            ),
                            singleLine = true,
                            isError = uiState is ScanUiState.Error,
                            supportingText = {
                                if (uiState is ScanUiState.Error) {
                                    Text(
                                        text = (uiState as ScanUiState.Error).message,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                keyboard?.hide()
                                scanViewModel.analyzeExtension(extensionId.trim())
                            }),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Purple80,
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        Button(
                            onClick = {
                                keyboard?.hide()
                                scanViewModel.analyzeExtension(extensionId.trim())
                            },
                            enabled = isNetworkAvailable && uiState !is ScanUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (uiState is ScanUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Analyzing…")
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Analyze Extension", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ── Result Card ───────────────────────────────────────────────────
            if (uiState is ScanUiState.Success) {
                item {
                    val result = (uiState as ScanUiState.Success).response
                    ResultCard(
                        extensionId = result.extension_id,
                        riskScore   = result.riskScore,
                        riskLevel   = result.riskLevel
                    )
                }
            }

            // ── History ───────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scan History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = { scanViewModel.clearHistory() }) {
                        Text("Clear All", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            if (history.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No scans yet", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                items(history, key = { it.id }) { scan ->
                    HistoryItem(scan)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── Result Card ───────────────────────────────────────────────────────────────
@Composable
private fun ResultCard(extensionId: String, riskScore: Float, riskLevel: String) {
    val color = riskColor(riskLevel)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, color, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = extensionId,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.LightGray
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "Risk Score", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "${"%.1f".format(riskScore * 100)}%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                RiskBadge(
                    level = riskLevel,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

// ── History Row ───────────────────────────────────────────────────────────────
@Composable
private fun HistoryItem(scan: ScanEntity) {
    val color = riskColor(scan.riskLevel)
    val dateStr = remember(scan.timestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(scan.timestamp))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.extension_id,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    maxLines = 1
                )
                Text(text = dateStr, fontSize = 11.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${"%.1f".format(scan.riskScore * 100)}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                RiskBadge(level = scan.riskLevel)
            }
        }
    }
}