package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun SizeAnimationInvisible(
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        if (isVisible) 1f else 0f,
        label = "alphaAnim"
    )

    Box(
        modifier = Modifier
            .animateContentSize()  // 사이즈 애니메이션
            .alpha(alpha)           // 보임/안보임
    ) {
        content()
    }
}


