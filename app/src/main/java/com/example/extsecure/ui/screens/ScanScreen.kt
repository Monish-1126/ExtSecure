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
                    label = { Text("Extension ID") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Button(
                    onClick = {
                        scanViewModel.analyzeExtension(extensionId.trim())
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

                    Text("Analyze Extension")
                }
            }
        }
            }

        // RESULT CARD
        item {

            if (uiState is ScanUiState.Success) {

                val result = (uiState as ScanUiState.Success).response
                val riskColor = riskColor(result.riskLevel)

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
                            result.extensionName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            "Version ${result.version}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(
                            result.description,
                            fontSize = 13.sp,
                            color = Color.LightGray
                        )

                        Divider(color = Color.DarkGray)

                        Text("Risk Score", color = Color.Gray)

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(
                                text = "${(result.riskScore * 100).toInt()}%",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = riskColor
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            RiskBadge(level = result.riskLevel)
                        }
                    }
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
        }
    }
}
