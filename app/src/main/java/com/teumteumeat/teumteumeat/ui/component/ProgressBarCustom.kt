package com.teumteumeat.teumteumeat.ui.component

import android.util.Size
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.extendedColors
import kotlin.math.min

@Composable
fun CustomProgressBar(
    modifier: Modifier = Modifier,
    currentStep: Int = 0,
    totalSteps: Int = 5,
    animationDuration: Int = 300,
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

@Composable
fun CustomProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,               // ⭐ 직접 progress 주입
    animationDuration: Int = 1000, // ⭐ 1초로 변경
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = LinearEasing
        ),
        label = "progressAnimation"
    )

    Box(
        modifier = modifier
            .height(15.dp)
            .clip(RoundedCornerShape(50))
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
}


@Composable
fun GoalProgress(
    progress: Float
) {

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.quiz_comp)
    )

    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )


    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(260.dp)
    ) {

        CircularProgressCanvas(progress)

        // 🔵 로티 (무한 반복)
        LottieAnimation(
            composition = composition,
            progress = { lottieProgress },
            modifier = Modifier.size(150.dp)
        )
    }
}

@Composable
fun CircularProgressCanvas(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 25f
) {

    val trackColor = MaterialTheme.extendedColors.unableContainer
    val fillPrimaryColor = MaterialTheme.extendedColors.primary
    val fillSecondaryColor = MaterialTheme.extendedColors.btnFillSecondary

    val animatable = remember { Animatable(0f) }

    LaunchedEffect(progress) {

        val target = progress.coerceIn(0f, 1f)

        // 🔥 줄어드는 값은 무시
        if (target > animatable.value) {
            animatable.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = 10000,
                    easing = LinearEasing
                )
            )
        }
    }

/*
    // 🎯 10초 동안 일정 속도 증가
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 10_000,
            easing = LinearEasing
        ),
        label = "progressAnimation"
    )*/

    Canvas(
        modifier = modifier.size(260.dp)
    ) {

        val diameter = min(size.width, size.height)
        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

        // 🔵 배경 트랙
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            size = arcSize,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )

        // 🔵 진행 게이지 그라데이션
        val gradientBrush = Brush.sweepGradient(
            colors = listOf(
                fillPrimaryColor,
                fillSecondaryColor,
                fillPrimaryColor
            )
        )

        drawArc(
            brush = gradientBrush,
            startAngle = -90f,
            sweepAngle = 360f * animatable.value,
            useCenter = false,
            size = arcSize,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
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