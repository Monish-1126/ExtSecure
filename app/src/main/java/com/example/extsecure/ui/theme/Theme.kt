package com.example.extsecure.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary   = Purple80,
    secondary = PurpleGrey80,
    tertiary  = Pink80,
    background = BgDark,
    surface    = CardBg,
    onBackground = Color.White,
    onSurface    = Color.White
)

@Composable
fun ExtSecureTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content
    )
}