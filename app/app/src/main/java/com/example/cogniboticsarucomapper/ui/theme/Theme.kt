package com.example.cogniboticsarucomapper.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = CogniTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F1EE),
    onPrimaryContainer = CogniNavy,
    secondary = CogniNavy,
    onSecondary = Color.White,
    background = CogniMist,
    onBackground = CogniInk,
    surface = Color.White,
    onSurface = CogniInk,
    surfaceVariant = Color(0xFFE8EEF2),
    onSurfaceVariant = CogniMuted,
    outline = Color(0xFF718594),
    outlineVariant = CogniOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = CogniTealLight,
    onPrimary = Color(0xFF003638),
    primaryContainer = Color(0xFF004F52),
    onPrimaryContainer = Color(0xFFB4EFEB),
    secondary = Color(0xFFB4CDE3),
    background = Color(0xFF0C1924),
    onBackground = Color(0xFFE4EDF3),
    surface = Color(0xFF142633),
    onSurface = Color(0xFFE4EDF3),
    surfaceVariant = Color(0xFF233946),
    onSurfaceVariant = Color(0xFFB7C9D4),
    outline = Color(0xFF8A9EAB),
    outlineVariant = Color(0xFF3B515F)
)

@Composable
fun CogniboticsArucoMapperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
