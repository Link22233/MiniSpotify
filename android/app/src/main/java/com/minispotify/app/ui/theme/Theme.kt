package com.minispotify.app.ui.theme

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

private val ColorLightOnPrimary = Color(0xFF003E12)
private val ColorDarkOnPrimary = Color(0xFF0A1F0A)

private val LightColors =
    lightColorScheme(
        primary = SeedPrimary,
        onPrimary = ColorLightOnPrimary,
        background = SeedSurfaceLight,
        surface = SeedSurfaceLight,
    )

private val DarkColors =
    darkColorScheme(
        primary = SeedPrimary,
        onPrimary = ColorDarkOnPrimary,
        background = SeedSurfaceDark,
        surface = SeedSurfaceDark,
        onBackground = SeedOnDark,
        onSurface = SeedOnDark,
    )

@Composable
fun MiniSpotifyTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            useDarkTheme -> DarkColors
            else -> LightColors
        }
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content,
    )
}
