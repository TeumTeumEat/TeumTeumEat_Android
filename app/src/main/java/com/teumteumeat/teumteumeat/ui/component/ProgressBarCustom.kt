package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

@Composable
fun CustomProgressBar(
    modifier: Modifier = Modifier,
    currentStep: Int = 0,
    totalSteps: Int = 5,
    animationDuration: Int = 300
) {
    // 1) Progress 계산 (0f ~ 1f)
    val targetProgress = (currentStep.toFloat() / totalSteps.toFloat())
        .coerceIn(0f, 1f)

    // 2) 애니메이션 적용된 Progress 값
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = LinearOutSlowInEasing
        ),
        label = "progressAnimation"
    )

    // 3️⃣ UI
    Box(
        modifier = modifier
            .height(15.dp) // 이미지와 유사한 높이
            .clip(RoundedCornerShape(50)) // 완전 pill
            .background(MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
    
    /*// 3) UI 표시
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
        // % 텍스트 표시 (옵션)
        *//*Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )*//*
    }*/
}

@Preview(showBackground = false)
@Composable
fun OnBoardingPreview() {
    var currentPage = 1
    var totalPage = 5
    TeumTeumEatTheme {
        DefaultMonoBg(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color.White
        ) {
            Column (
                modifier = Modifier.padding(all = 20.dp)
            ){
                CustomProgressBar(
                    currentStep = currentPage,
                    totalSteps = totalPage,
                )
            }
        }
    }

}