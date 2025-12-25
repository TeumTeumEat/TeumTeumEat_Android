package com.teumteumeat.teumteumeat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.teumteumeat.teumteumeat.utils.LocalExtendedColors


data class ExtendedColors(
    val primary: Color,
    val error: Color,
    val buttonFillSecondary: Color,
    val modalShadow: Color,
    val errorContainer: Color,
    val primaryContainer: Color,
    val background: Color,
    val unableContainer: Color,
    val unableContent: Color,
    val textOnError: Color,
    val textOnPrimary: Color,
)

val LightExtendedColors = ExtendedColors(
    primary = Blue80,
    primaryContainer = Blue10,
    textOnPrimary = White100,

    unableContent = Gray50,
    unableContainer = Gray20,

    error = Red80,
    errorContainer = Red10,
    textOnError = White100,

    background = White10,
    buttonFillSecondary = Blue10,
    modalShadow = BlackTrp15,
)


private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Blue80,
    primaryContainer = White10,
    onPrimary = Color.White,

    secondary = PurpleGrey40,
    secondaryContainer = Gray20,

    tertiary = Black100,

    surfaceVariant = Gray50,
    onSurfaceVariant = Gray30,

    onTertiary = Black100,

    error = Red50,

    background = Color(0xFFFFFBFE),
    surface = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFFF2F3F5),
)

@Composable
fun TeumTeumEatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // ⭐ 기본값 false 추천
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        // ✅ Android 12+ & 동적 컬러 허용
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        // ✅ 우리가 정의한 컬러
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = when {
        darkTheme -> LightExtendedColors
        else -> LightExtendedColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        CompositionLocalProvider(
            LocalExtendedColors provides extendedColors
        ) {
            content()
        }
    }
}