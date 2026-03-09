package com.example.extsecure.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.extsecure.ui.theme.*

@Composable
fun RiskBadge(level: String, modifier: Modifier = Modifier) {
    val color = riskColor(level)
    Text(
        text = level,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

fun riskColor(level: String): Color = when (level.uppercase()) {
    "CRITICAL" -> RiskCritical
    "HIGH"     -> RiskHigh
    "MEDIUM"   -> RiskMedium
    else       -> RiskLow
}