package com.example.tailtrace.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF1AA79A), onPrimary = Color.White,
    secondary = Color(0xFFE8590C), onSecondary = Color.White,
    tertiary = Color(0xFF12395C),
    background = Color(0xFFFAF7F0), onBackground = Color(0xFF12395C),
    surface = Color.White, onSurface = Color(0xFF12395C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4DD0C4), onPrimary = Color(0xFF00201D),
    secondary = Color(0xFFFF8A4C), onSecondary = Color.Black,
    tertiary = Color(0xFF9CC7EC),
    background = Color(0xFF0F1B26), onBackground = Color(0xFFEAF2F8),
    surface = Color(0xFF172736), onSurface = Color(0xFFEAF2F8),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp),
)

@Composable
fun TailTraceTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}