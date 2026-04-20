package com.teumteumeat.teumteumeat.ui.component.image

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch

@Composable
fun BouncingImage(
    foodRes: Int,
    onTab: () -> Unit
) {
    // 1. 애니메이션을 위한 scale 상태값 (초기값 1.0)
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Image(
        painter = painterResource(id = foodRes),
        contentDescription = "음식 이미지",
        modifier = Modifier
            .fillMaxSize()
            // 2. 현재 애니메이션 값 적용
            .scale(scale.value)
            // 3. 터치 이벤트 처리
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // 눌렀을 때: 크기를 0.9배로 축소
                        coroutineScope.launch {
                            scale.animateTo(0.9f, animationSpec = tween(100))
                        }
                        
                        // 사용자가 손을 뗄 때까지 대기
                        val released = tryAwaitRelease()
                        
                        // 뗐을 때 또는 취소되었을 때: 다시 1.0배로 복구
                        coroutineScope.launch {
                            scale.animateTo(1f, animationSpec = tween(100))
                        }
                    },
                    onTap = {
                        onTab()
                    }
                )
            },
        contentScale = ContentScale.Fit
    )
}