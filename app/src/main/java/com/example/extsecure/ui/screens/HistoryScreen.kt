package com.example.extsecure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.extsecure.database.ScanEntity
import com.example.extsecure.ui.components.RiskBadge
import com.example.extsecure.ui.components.riskColor
import com.example.extsecure.ui.theme.BgDark
import com.example.extsecure.viewmodel.ScanViewModel

@Composable
fun HistoryScreen(viewModel: ScanViewModel) {

    val history by viewModel.scanHistory.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp)
    ) {

        Text(
            "Scan History",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(history) { scan ->
                HistoryCard(scan)
            }
        }
    }
}

@Composable
private fun HistoryCard(scan: ScanEntity) {

    val color = riskColor(scan.riskLevel)

    Card {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(scan.extension_id, maxLines = 1)
                Text(scan.riskLevel, color = color)
            }
            Text("${(scan.riskScore * 100).toInt()}%")
        }
    }
}