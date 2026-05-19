package com.mobile.shopease.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ShopEaseOrange,
    onPrimary = Color.White,
    primaryContainer = ShopEaseOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = ShopEaseOrangeLight,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = ErrorColor,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ShopEaseOrange,
    onPrimary = Color.White,
    primaryContainer = ShopEaseOrangeLight,
    onPrimaryContainer = Color.Black,
    secondary = ShopEaseOrangeDark,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = ErrorColor,
    onError = Color.White
)

@Composable
fun ShopEaseTheme(
    darkTheme: Boolean = false, // Default to light mode as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
