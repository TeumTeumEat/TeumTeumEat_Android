package com.teumteumeat.teumteumeat.ui.component.quiz.ox

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SwipeableQuizCardWrapper(
    key: Any,
    onSelectAnswer: (String) -> Unit,
    content: @Composable (CardStatus, triggerYes: () -> Unit, triggerNo: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember(key) { Animatable(0f) }
    val pxThreshold = with(LocalDensity.current) { 100.dp.toPx() }

    // 람다 변수에서 suspend 키워드를 제거하여 일반 함수로 만듭니다.
    val animateAndSubmit: (String, Float) -> Unit = { answer, target ->
        // 1. 애니메이션은 코루틴 블록 안에서 실행 (중단 함수 호출 가능 영역)
        coroutineScope.launch {
            offsetX.animateTo(
                targetValue = target,
                animationSpec = spring(stiffness = 500f)
            )
        }

        // 2. 이 부분은 코루틴 밖이므로 즉시 실행됨
        onSelectAnswer(answer)
    }

    // 왼쪽: O (Accept), 오른쪽: X (Reject) 로 설정하신 방향 반영
    val currentStatus = when {
        offsetX.value < -pxThreshold -> CardStatus.Accept
        offsetX.value > pxThreshold -> CardStatus.Reject
        else -> CardStatus.Default
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(key) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            when {
                                offsetX.value < -pxThreshold -> { // 왼쪽 스와이프
                                    onSelectAnswer("O")
                                    offsetX.animateTo(-1000f)
                                }

                                offsetX.value > pxThreshold -> { // 오른쪽 스와이프
                                    onSelectAnswer("X")
                                    offsetX.animateTo(1000f)
                                }

                                else -> {
                                    offsetX.animateTo(0f, spring())
                                }
                            }
                        }
                    }
                )
            }
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = offsetX.value / 25f
            }
    ) {
        content(
            currentStatus,
            { coroutineScope.launch { animateAndSubmit("O", -1000f) } },
            { coroutineScope.launch { animateAndSubmit("X", 1000f) } }
        )
    }

    // 수정 전: val animateAndSubmit = suspend { answer: String, target: Float -> ... }
    // 수정 후: 일반적인 suspend 함수 형태로 정의


}

