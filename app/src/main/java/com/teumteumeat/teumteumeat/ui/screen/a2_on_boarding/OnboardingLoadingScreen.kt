package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.component.GoalProgress
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun SubmitLoadingScreen(
    modifier: Modifier = Modifier,
    title: String = "",
    message: String = "",
    minDurationMs: Long = 1800L,
    visibleStates: SnapshotStateList<Boolean>,
    isCompletedLoading: Boolean,
    onAnimationComplete: () -> Unit = {},
) {
    val extendedColors = MaterialTheme.extendedColors
    val typography = MaterialTheme.appTypography

    val progressAnimatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progressAnimatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = minDurationMs.toInt(),
                easing = LinearEasing
            )
        )
        onAnimationComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.backgroundW100)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            GoalProgress(
                progress = progressAnimatable.value,
                isCompletedLoading = isCompletedLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "틈틈잇을 생성하는 중\n잠시만 기다려주세요",
                style = typography.subtitleSemiBold18.copy(
                    lineHeight = 24.sp,
                ),
                color = extendedColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // ✅ 체크 리스트
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Column(
                modifier = modifier
                    .wrapContentSize()
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                AnimatedVisibility(
                    visible = visibleStates[0],
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    LoadingCheckItem("대중교통 이용 시간 취합 중")
                }

                AnimatedVisibility(
                    visible = visibleStates[1],
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    LoadingCheckItem("난이도와 프롬프트 적용 중")
                }

                AnimatedVisibility(
                    visible = visibleStates[2],
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    LoadingCheckItem("해당 카테고리 퀴즈 생성 중")
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnBoardingLoadingScreenPreview() {
    TeumTeumEatTheme {
        val visibleStates = remember { mutableStateListOf(true, true, true) }
        SubmitLoadingScreen(
            minDurationMs = 1800L,
            visibleStates = visibleStates,
            isCompletedLoading = false,
        )
    }
}

@Composable
private fun LoadingCheckItem(
    text: String,
) {
    val colors = MaterialTheme.extendedColors
    val typography = MaterialTheme.appTypography

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = colors.primary,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.textOnPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = typography.captionRegular14,
            color = colors.textPrimary
        )
    }
}