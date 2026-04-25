package com.example.gurultuolcer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF18201B),
    onPrimary = Color(0xFFF5F7F2),
    secondary = Color(0xFF4F6358),
    tertiary = Color(0xFF7A8F75),
    background = Color(0xFFF5F7F2),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EFE3),
    onBackground = Color(0xFF18201B),
    onSurface = Color(0xFF18201B),
    onSurfaceVariant = Color(0xFF4F6358),
    outlineVariant = Color(0xFFD9E3D4),
    secondaryContainer = Color(0xFFE6F0E1),
    onSecondaryContainer = Color(0xFF18201B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE4F1E0),
    onPrimary = Color(0xFF101611),
    secondary = Color(0xFFA8B8AE),
    tertiary = Color(0xFF7EE081),
    background = Color(0xFF0F1411),
    surface = Color(0xFF171E1A),
    surfaceVariant = Color(0xFF1F2923),
    onBackground = Color(0xFFF0F5EE),
    onSurface = Color(0xFFF0F5EE),
    onSurfaceVariant = Color(0xFFB6C5BA),
    outlineVariant = Color(0xFF314038),
    secondaryContainer = Color(0xFF26332C),
    onSecondaryContainer = Color(0xFFE4F1E0),
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontSize = 30.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)

@Composable
fun GurultuOlcerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surfaceVariant.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
