package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun SizeAnimationInvisible(
    isVisible: Boolean,
    clickEnabled: Boolean = isVisible,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        if (isVisible) 1f else 0f,
        label = "alphaAnim"
    )

    Box(
        modifier = Modifier
            .animateContentSize()
            .alpha(alpha)
            .then(
                // clickEnabled=false 이면 Initial 패스에서 모든 포인터 이벤트를 소비해
                // 자식 composable 의 clickable 이 실행되지 않도록 차단한다.
                if (!clickEnabled) Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial)
                                .changes.forEach { it.consume() }
                        }
                    }
                } else Modifier
            )
    ) {
        content()
    }
}


