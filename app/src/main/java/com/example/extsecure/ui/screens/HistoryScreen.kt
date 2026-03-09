package com.example.extsecure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.extsecure.database.ScanEntity
import com.example.extsecure.ui.components.RiskBadge
import com.example.extsecure.ui.components.riskColor
import com.example.extsecure.viewmodel.ScanViewModel

private val RISK_LEVELS = listOf("ALL", "LOW", "MEDIUM", "HIGH", "CRITICAL")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    viewModel: ScanViewModel,
    navController: NavController
) {
    val history by viewModel.scanHistoryFlow.collectAsState()
    val colors = MaterialTheme.colorScheme

    var searchQuery by remember { mutableStateOf("") }
    var selectedRiskFilter by remember { mutableStateOf("ALL") }
    var showClearDialog by remember { mutableStateOf(false) }

    // Filter logic
    val filteredHistory = remember(history, searchQuery, selectedRiskFilter) {
        history.filter { scan ->
            val matchesSearch = searchQuery.isBlank() ||
                    scan.extensionName.contains(searchQuery, ignoreCase = true) ||
                    scan.extensionId.contains(searchQuery, ignoreCase = true)
            val matchesRisk = selectedRiskFilter == "ALL" ||
                    scan.riskLevel.equals(selectedRiskFilter, ignoreCase = true)
            matchesSearch && matchesRisk
        }
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear History") },
            text = { Text("Are you sure you want to delete all scan history? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp)
    ) {
        // ── HEADER ROW ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Scan History",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onBackground
                )
                Text(
                    text = "${history.size} total scan(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            if (history.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Clear history",
                        tint = colors.error
                    )
                }
            }
        }

        // ── SEARCH BAR ──
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name or ID…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline
            )
        )

        Spacer(Modifier.height(12.dp))

        // ── RISK FILTER CHIPS ──
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RISK_LEVELS.forEach { level ->
                FilterChip(
                    selected = selectedRiskFilter == level,
                    onClick = { selectedRiskFilter = level },
                    label = { Text(level, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary.copy(alpha = 0.15f),
                        selectedLabelColor = colors.primary
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── CONTENT ──
        if (history.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = colors.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No scans yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Scan some extensions and they'll show up here",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No results matching your filter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredHistory) { scan ->
                    HistoryCard(scan = scan) {
                        navController.navigate("detail/${scan.extensionId}")
                    }
                }
            }
        }
    }
}


@Composable
fun HistoryCard(scan: ScanEntity, onClick: () -> Unit) {
    val color = riskColor(scan.riskLevel)
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = scan.extensionName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                RiskBadge(level = scan.riskLevel)
            }

            Text(
                text = "Risk Score: ${(scan.riskScore * 100).toInt()}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}