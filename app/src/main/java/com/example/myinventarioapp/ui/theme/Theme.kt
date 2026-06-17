package com.example.myinventarioapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color



private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = androidx.compose.ui.graphics.Color(0xFF121212)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// 🎨 Paleta de marca L Moda — negro, blanco cálido y madera
val BrandBlack = Color(0xFF1A1A1A)
val BrandWarmWhite = Color(0xFFFDFAF7)
val BrandWoodMedium = Color(0xFFC8A882)
val BrandWoodLight = Color(0xFFE8D9C0)
val BrandWarmBackground = Color(0xFFF5EFE6)

// Variante para textos sobre fondo cálido (gris cálido, no negro puro)
val BrandTextSecondary = Color(0xFF8B7355)

// Color de alerta para stock bajo (se mantiene independiente de la paleta cálida
// para que el usuario lo distinga rápido como "atención")
val StockLowColor = Color(0xFFB3413E)
val StockOkColor = Color(0xFF1A1A1A)

@Composable
fun MyInventarioAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
        typography = Typography,
        content = content
    )
}
@Composable
fun AjustarBarraEstado() {
    val view = LocalView.current
    val context = LocalContext.current
    val window = (context as Activity).window
    val isDarkTheme = isSystemInDarkTheme()

    SideEffect {
        // Fondo fijo según tema: negro en oscuro, blanco en claro
        window.statusBarColor = if (isDarkTheme) {
            androidx.compose.ui.graphics.Color.Black.toArgb()
        } else {
            androidx.compose.ui.graphics.Color.White.toArgb()
        }

        // Íconos claros u oscuros según fondo
        WindowCompat.getInsetsController(window, view)
            .isAppearanceLightStatusBars = !isDarkTheme
    }
}