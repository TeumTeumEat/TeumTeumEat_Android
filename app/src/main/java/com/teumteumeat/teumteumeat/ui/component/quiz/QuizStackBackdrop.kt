package com.teumteumeat.teumteumeat.ui.component.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val GRANULAR_THRESHOLD = 10
private const val BUCKET_SIZE = 5

/** 남은 문제 수를 화면에 그릴 스택 장수(depth)로 변환한다.
 * [GRANULAR_THRESHOLD]장 이하로 남았을 때는 1장 단위로, 그 이상은 [BUCKET_SIZE]장 단위로만 깊이가 바뀐다. */
fun calculateStackDepth(remainingCount: Int): Int {
    if (remainingCount <= 0) return 0
    if (remainingCount <= GRANULAR_THRESHOLD) return remainingCount
    val extraBuckets = (remainingCount - GRANULAR_THRESHOLD + BUCKET_SIZE - 1) / BUCKET_SIZE
    return GRANULAR_THRESHOLD + extraBuckets
}

/** 퀴즈 카드 뒤에 쌓인 것처럼 보이는 회색 스택 데코레이션. [stackDepth]장 만큼 겹쳐 그린다. */
@Composable
fun QuizStackBackdrop(stackDepth: Int, modifier: Modifier = Modifier) {
    for (layer in stackDepth downTo 1) {
        Box(
            modifier = modifier
                .offset(y = (-6 * layer).dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(420.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.08f),
                )
                .background(
                    color = Color.LightGray.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(32.dp),
                )
        )
    }
}
