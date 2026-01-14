package com.teumteumeat.teumteumeat.ui.screen.common_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.teumteumeat.teumteumeat.ui.component.loading.FullScreenLoading

@Composable
fun AuthBlockingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {}, // ⭐ 클릭 흡수
        contentAlignment = Alignment.Center
    ) {
         FullScreenLoading()
    }
}