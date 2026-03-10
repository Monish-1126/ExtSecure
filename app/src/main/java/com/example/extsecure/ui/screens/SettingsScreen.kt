package com.example.extsecure.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.extsecure.viewmodel.ScanViewModel

@Composable
fun SettingsScreen(
    viewModel: ScanViewModel,
    themeMode: Int,               // 0 = System, 1 = Light, 2 = Dark
    onThemeModeChanged: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Clear history confirmation
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History") },
            text = { Text("This will permanently delete all scan history. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) {
                    Text("Clear", color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Theme picker dialog
    if (showThemeDialog) {
        val options = listOf("System Default", "Light", "Dark")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    options.forEachIndexed { index, label ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onThemeModeChanged(index)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == index,
                                onClick = {
                                    onThemeModeChanged(index)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {

        // ── APPEARANCE SECTION ──
        SettingsSectionHeader(title = "Appearance")

        val themeLabel = when (themeMode) {
            1 -> "Light"
            2 -> "Dark"
            else -> "System Default"
        }
        SettingsItem(
            icon = Icons.Default.DarkMode,
            title = "Theme",
            subtitle = themeLabel,
            onClick = { showThemeDialog = true }
        )

        Spacer(Modifier.height(24.dp))

        // ── DATA SECTION ──
        SettingsSectionHeader(title = "Data")

        SettingsItem(
            icon = Icons.Default.DeleteSweep,
            title = "Clear Scan History",
            subtitle = "Delete all saved scan results",
            onClick = { showClearDialog = true },
            isDestructive = true
        )

        Spacer(Modifier.height(24.dp))

        // ── API INFO SECTION ──
        SettingsSectionHeader(title = "API")

        SettingsItem(
            icon = Icons.Default.Language,
            title = "API Endpoint",
            subtitle = "extsecure-api.onrender.com",
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://extsecure-api.onrender.com"))
                context.startActivity(intent)
            }
        )

        SettingsItem(
            icon = Icons.Default.Code,
            title = "API Documentation",
            subtitle = "View Swagger/OpenAPI docs",
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://extsecure-api.onrender.com/docs"))
                context.startActivity(intent)
            }
        )

        Spacer(Modifier.height(24.dp))

        // ── ABOUT SECTION ──
        SettingsSectionHeader(title = "About")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "ExtSecure",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Analyze Chrome extensions for potential security risks.\nPowered by AI-driven risk assessment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = colors.outline.copy(alpha = 0.2f))
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Your data stays on-device. Only extension IDs are sent for analysis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = colors.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val tint = if (isDestructive) colors.error else colors.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDestructive) colors.error else colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = tint
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}


