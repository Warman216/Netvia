package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AppleBlueDark,
    onPrimary = Color.White,
    primaryContainer = AppleBlueDark.copy(alpha = 0.2f),
    onPrimaryContainer = Color.White,
    secondary = AppleGreenDark,
    onSecondary = Color.White,
    secondaryContainer = AppleGreenDark.copy(alpha = 0.2f),
    onSecondaryContainer = AppleGreenDark,
    tertiary = AppleIndigo,
    background = AppleSystemGroupedBackgroundDark,
    onBackground = AppleLabelDark,
    surface = AppleSystemBackgroundDark,
    onSurface = AppleLabelDark,
    surfaceVariant = AppleSecondarySystemGroupedBackgroundDark,
    onSurfaceVariant = AppleSecondaryLabelDark,
    outline = AppleSeparatorDark,
    error = AppleRedDark,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AppleBlue,
    onPrimary = Color.White,
    primaryContainer = AppleBlue.copy(alpha = 0.12f),
    onPrimaryContainer = AppleBlue,
    secondary = AppleGreen,
    onSecondary = Color.White,
    secondaryContainer = AppleGreen.copy(alpha = 0.12f),
    onSecondaryContainer = AppleGreen,
    tertiary = AppleIndigo,
    background = AppleSystemGroupedBackgroundLight,
    onBackground = AppleLabelLight,
    surface = AppleSystemBackgroundLight,
    onSurface = AppleLabelLight,
    surfaceVariant = AppleSystemGroupedBackgroundLight,
    onSurfaceVariant = AppleSecondaryLabelLight,
    outline = AppleSeparatorLight,
    error = AppleRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent high-polish theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
