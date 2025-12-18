package com.teumteumeat.teumteumeat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext



data class ExtendedColors(
    val secondaryDisabled: Color,
    val iconBlack: Color,
    val textColorBlack: Color,
    val textColorGray60: Color,
    val textColorGray40: Color,
    val textColorGray30: Color,
    val textColorGray20: Color,
    val textColorGray10: Color,
    val textColorBlackSecondary: Color,
    val outLineUnFocused: Color,
    val textFieldBackground: Color,
    val scrimLightReverse: Color,
    val unableVariantSecondary: Color,
    val onUnableVariantSecondary: Color,
    val disableTabColor: Color,
    val mainContentStrokeColor: Color,
    val mainLearningContentLabel: Color,
    val bottomSheetHandleColor: Color,
    val contentBgFoundation400: Color,
    // 필요한 만큼 추가 가능
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Blue80,
    onPrimary = Color.White,

    secondary = PurpleGrey40,
    tertiary = Pink40,

    surfaceVariant = Gray50,
    onSurfaceVariant = Gray30,

    primaryContainer = bgWhite10,

    secondaryContainer = Gray20,

    onTertiary = Black100,

    background = Color(0xFFFFFBFE),
    surface = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFFF2F3F5),
)

@Composable
fun TeumTeumEatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // todo. question-아래 두 줄의 코드의 차이점과 각 코드는 어느 상황에 최적화된 코드인지 판단하기
//             if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (darkTheme) DarkColorScheme else LightColorScheme

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