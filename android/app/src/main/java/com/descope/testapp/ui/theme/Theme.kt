package com.descope.testapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DescopePrimary,
    onPrimary = TextWhite,
    primaryContainer = DescopePrimaryDark,
    onPrimaryContainer = TextWhite,
    secondary = DescopeSecondary,
    onSecondary = TextDark,
    secondaryContainer = DescopeSecondaryDark,
    onSecondaryContainer = TextWhite,
    tertiary = DescopeTertiary,
    onTertiary = TextDark,
    tertiaryContainer = DescopeTertiaryDark,
    onTertiaryContainer = TextWhite,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextGray,
)

private val LightColorScheme = lightColorScheme(
    primary = DescopePrimary,
    onPrimary = TextWhite,
    primaryContainer = DescopePrimaryLight,
    onPrimaryContainer = TextWhite,
    secondary = DescopeSecondary,
    onSecondary = TextDark,
    secondaryContainer = DescopeSecondaryLight,
    onSecondaryContainer = TextDark,
    tertiary = DescopeTertiary,
    onTertiary = TextDark,
    tertiaryContainer = DescopeTertiaryLight,
    onTertiaryContainer = TextDark,
    background = LightBackground,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextGray,
)

@Composable
fun DescopeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default to use Descope branding
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
