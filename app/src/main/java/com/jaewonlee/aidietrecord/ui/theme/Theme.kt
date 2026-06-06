package com.jaewonlee.aidietrecord.ui.theme

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
    primary = Color(0xFF63CFA0),
    onPrimary = Color(0xFF0B1F16),
    primaryContainer = Color(0xFF183E2E),
    onPrimaryContainer = Color(0xFFD9F8E8),
    secondary = Color(0xFF8FCFB4),
    onSecondary = Color(0xFF0F2118),
    secondaryContainer = Color(0xFF24382E),
    onSecondaryContainer = Color(0xFFDDF2E7),
    tertiary = Color(0xFFD8B16C),
    onTertiary = Color(0xFF261A08),
    tertiaryContainer = Color(0xFF49361A),
    onTertiaryContainer = Color(0xFFFFE6B6),
    background = Color(0xFF101310),
    onBackground = Color(0xFFECEFEC),
    surface = Color(0xFF171A17),
    onSurface = Color(0xFFECEFEC),
    surfaceVariant = Color(0xFF242820),
    onSurfaceVariant = Color(0xFFB7C0B3),
    outline = Color(0xFF444C41),
    errorContainer = Color(0xFF5A211F)
)

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = Color.White,
    primaryContainer = AppPrimarySoft,
    onPrimaryContainer = AppTextPrimary,
    secondary = AppSuccess,
    onSecondary = Color.White,
    secondaryContainer = AppSuccessSoft,
    onSecondaryContainer = AppTextPrimary,
    tertiary = AppWarning,
    onTertiary = Color.White,
    tertiaryContainer = AppWarningSoft,
    onTertiaryContainer = AppTextPrimary,
    background = AppBackground,
    onBackground = AppTextPrimary,
    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = AppSurfaceSoft,
    onSurfaceVariant = AppTextMuted,
    outline = AppOutline,
    errorContainer = AppDangerSoft

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

@Composable
fun AIDietRecordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
