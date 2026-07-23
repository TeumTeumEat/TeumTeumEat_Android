package com.teumteumeat.teumteumeat.ui.component.image

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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

    // pointerInput(Unit)의 제스처 감지 코루틴은 최초 1회만 launch되어 재시작되지 않으므로,
    // onTab을 직접 캡처하면 이후 recomposition에서 바뀐 콜백(예: snackState 변화)이 반영되지 않는다.
    // rememberUpdatedState로 항상 최신 콜백을 참조하도록 한다.
    val currentOnTab by rememberUpdatedState(onTab)

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
                        currentOnTab()
                    }
                )
            },
        contentScale = ContentScale.Fit
    )
}