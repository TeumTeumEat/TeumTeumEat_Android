package com.teumteumeat.teumteumeat.ui.screen.common_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun BottomFadeOverlay(
    modifier: Modifier = Modifier,
    height: Dp = 170.dp,
    startColor: Color = Color.Transparent,
    endColor: Color = MaterialTheme.extendedColors.backgroundW100,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        startColor,
                        endColor
                    )
                )
            )
    )
}
