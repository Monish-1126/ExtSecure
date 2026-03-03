package com.example.extsecure.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.extsecure.database.ScanEntity
import com.example.extsecure.ui.components.RiskBadge
import com.example.extsecure.ui.components.riskColor
import com.example.extsecure.ui.theme.BgDark
import com.example.extsecure.viewmodel.ScanViewModel


@Composable
fun HistoryScreen(
    viewModel: ScanViewModel,
    navController: NavController
) {
    val history by viewModel.scanHistory.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0F12))
            .padding(24.dp)
    ) {

        Text(
            text = "Scan History",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(history, key = { it.id }) { scan ->
                ModernHistoryCard(scan) {
                    navController.navigate("detail/${scan.extension_id}")
                }
            }
        }
    }
}
@Composable
fun ModernHistoryCard(
    scan: ScanEntity,
    onClick: () -> Unit
) {
    val color = riskColor(scan.riskLevel)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1C22)
        ),
        border = BorderStroke(1.5.dp, color),
        elevation = CardDefaults.cardElevation(10.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color, CircleShape)
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.extension_id,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Text(
                    text = scan.riskLevel,
                    fontSize = 12.sp,
                    color = color
                )
            }

            Text(
                text = "${"%.0f".format(scan.riskScore * 100)}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
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