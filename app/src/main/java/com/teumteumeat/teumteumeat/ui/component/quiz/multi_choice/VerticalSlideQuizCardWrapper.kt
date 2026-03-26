package com.teumteumeat.teumteumeat.ui.component.quiz.multi_choice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

@Composable
fun VerticalSlideQuizCardWrapper(
    key: Any,
    onSelectAnswer: (String) -> Unit,
    content: @Composable (triggerSelect: (String) -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // Y축 이동을 위한 Animatable
    val offsetY = remember(key) { Animatable(0f) }

    // 🔹 아래로 날려보내고 정답을 제출하는 함수
    val animateAndSubmit: suspend (String) -> Unit = { answer ->
        // 1. 애니메이션은 별도의 코루틴에서 실행 (기다리지 않음)
        coroutineScope.launch {
            offsetY.animateTo(
                targetValue = 1500f,
                animationSpec = spring(stiffness = 150f, dampingRatio = 0.8f)
            )
        }

        // 2. 애니메이션 시작과 동시에(혹은 아주 짧은 딜레이 후) 바로 정답 제출
        // 사용자가 답을 선택했다는 로직을 즉시 수행합니다.
        onSelectAnswer(answer)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = offsetY.value
                // 아래로 내려갈 때 살짝 작아지는 효과 (원치 않으시면 scale 로직을 제거해도 됩니다)
                val scale = (1f - (offsetY.value / 3000f)).coerceAtLeast(0.8f)
                scaleX = scale
                scaleY = scale
                // 내려가면서 점점 투명해지는 효과 추가
                alpha = (1f - (offsetY.value / 1000f)).coerceIn(0f, 1f)
            }
    ) {
        content { answer ->
            coroutineScope.launch { animateAndSubmit(answer) }
        }
    }
}