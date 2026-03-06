package com.example.extsecure.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.extsecure.api.AnalyzeResponse
import com.example.extsecure.database.ScanEntity
import com.example.extsecure.ui.components.RiskBadge
import com.example.extsecure.ui.components.riskColor
import com.example.extsecure.ui.theme.*
import com.example.extsecure.viewmodel.ScanUiState
import com.example.extsecure.viewmodel.ScanViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScanScreen(
    scanViewModel: ScanViewModel,
    navController: NavController
) {

    val uiState by scanViewModel.uiState.collectAsState()
    val isNetworkAvailable by scanViewModel.isNetworkAvailable.collectAsState()

    var extensionId by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(24.dp)
    ) {

        // TITLE
        item {
            Column {

                Text(
                    text = "ExtSecure",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Analyze Chrome extensions for malicious behavior",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        // NETWORK WARNING
        if (!isNetworkAvailable) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0D0D)),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Text(
                        text = "⚠ No internet connection",
                        color = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // INPUT CARD
        item{
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(
                    value = extensionId,
                    onValueChange = { extensionId = it },
                    label = { Text("Extension IDs") },
                    placeholder = {
                        Text(
                            "Paste one extension ID per line\nExample:\nkbfnbcaeplbcioakkpcpgfkobkghlhen\neimadpbcbfnmbkopoojfekhnkhdbieeh",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),   // bigger input area
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                val ids = extensionId
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                Text(
                    text = "${ids.size} extension(s) ready to scan",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Button(
                    onClick = {
                        val ids = extensionId
                            .split("\n")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        scanViewModel.analyzeExtensionsBatch(ids)
                    },
                    enabled = isNetworkAvailable,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Analyze ${ids.size} Extension(s)")
                }
            }
        }
            }
        item {
            Text(
                text = "Scan Results",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (uiState is ScanUiState.Success) {

                val results = (uiState as ScanUiState.Success).responses

                val successCount = results.count { it.response != null }
                val failCount = results.count { it.error != null }

                Text(
                    text = "Results: $successCount success • $failCount failed",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
        // RESULT CARD
        item {


            if (uiState is ScanUiState.Success) {

                val results = (uiState as ScanUiState.Success).responses

                results.forEach { result ->

                    if (result.response != null) {

                        val extension = result.response
                        val riskColor = riskColor(extension.riskLevel)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier = Modifier.padding(22.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {

                                Text(
                                    extension.extensionName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Text(
                                    "Version ${extension.version}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )

                                Text(
                                    extension.description,
                                    fontSize = 13.sp,
                                    color = Color.LightGray
                                )

                                Divider(color = Color.DarkGray)

                                Text("Risk Score", color = Color.Gray)

                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Text(
                                        text = "${(extension.riskScore * 100).toInt()}%",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = riskColor
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    RiskBadge(level = extension.riskLevel)
                                }
                            }
                        }

                    } else {

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1A1A)),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text = "Extension ID: ${result.extensionId}",
                                    color = Color.Gray
                                )

                                Text(
                                    text = result.error ?: "Unknown error",
                                    color = Color.Red
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        item {
            if (uiState is ScanUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = AccentPurple
                )
            }
            if (uiState is ScanUiState.Error) {
                val message = (uiState as ScanUiState.Error).message

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1A1A))
                ) {
                    Text(
                        text = message,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
