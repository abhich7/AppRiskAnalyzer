package com.cyberprotect.privacyanalyzer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PrivacyDarkScheme = darkColorScheme(
    primary = Cyan,
    onPrimary = BgDeep,
    secondary = Cyan2,
    onSecondary = BgDeep,
    tertiary = Orange,
    background = BgDeep,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextMuted,
    error = Red,
    onError = TextPrimary,
    outline = BorderCyan
)

@Composable
fun PrivacyAnalyzerTheme(
    // Always dark — this is a security app; a light theme isn't part of the brand.
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PrivacyDarkScheme,
        typography = AppTypography,
        content = content
    )
}
