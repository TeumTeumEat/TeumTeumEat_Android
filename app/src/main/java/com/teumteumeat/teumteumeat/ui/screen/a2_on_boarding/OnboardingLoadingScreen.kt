package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.getValue
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
    // PDF 문서 플로우 전용 SSE 진행 상태
    isDocumentFlow: Boolean = false,
    sseProgress: Float = 0f,
    sseStatusText: String? = null,   // "N초 남았어요." / "잠시만 기다려주세요"
    sseProgressText: String? = null, // "XX% 완료"
) {
    val extendedColors = MaterialTheme.extendedColors
    val typography = MaterialTheme.appTypography

    // 카테고리 플로우: 1.8초 선형 애니메이션
    val progressAnimatable = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (!isDocumentFlow) {
            progressAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = minDurationMs.toInt(), easing = LinearEasing)
            )
            onAnimationComplete()
        }
    }

    // 문서 플로우: SSE 진행률 → 부드러운 원형 프로그레스 애니메이션
    val animatedSseProgress by animateFloatAsState(
        targetValue = if (isDocumentFlow) sseProgress else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "sseProgressAnim"
    )
    LaunchedEffect(sseProgress) {
        if (isDocumentFlow && sseProgress >= 1.0f) {
            onAnimationComplete()
        }
    }

    val effectiveProgress = if (isDocumentFlow) animatedSseProgress else progressAnimatable.value

    val stepLabels = if (isDocumentFlow) {
        listOf("파일 업로드 중", "문서 등록 중", "퀴즈 생성 중")
    } else {
        listOf("대중교통 이용 시간 취합 중", "난이도와 프롬프트 적용 중", "해당 카테고리 퀴즈 생성 중")
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
                progress = effectiveProgress,
                isCompletedLoading = isCompletedLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            val subtitleText = if (isDocumentFlow && sseStatusText != null) sseStatusText else "잠시만 기다려주세요"
            Text(
                text = "틈틈잇을 생성하는 중",
                style = typography.subtitleSemiBold18.copy(lineHeight = 24.sp),
                color = extendedColors.textPrimary
            )

            Text(
                text = subtitleText,
                style = typography.subtitleSemiBold18.copy(lineHeight = 24.sp),
                color = extendedColors.textPrimary
            )

            if (isDocumentFlow && sseProgressText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sseProgressText,
                    style = typography.captionRegular14.copy(lineHeight = 24.sp),
                    color = extendedColors.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 체크 리스트
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
                    LoadingCheckItem(stepLabels[0])
                }

                AnimatedVisibility(
                    visible = visibleStates[1],
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    LoadingCheckItem(stepLabels[1])
                }

                AnimatedVisibility(
                    visible = visibleStates[2],
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    LoadingCheckItem(stepLabels[2])
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Document SSE Processing")
@Composable
private fun DocumentLoadingScreenPreview() {
    TeumTeumEatTheme {
        val visibleStates = remember { mutableStateListOf(true, true, true) }
        SubmitLoadingScreen(
            visibleStates = visibleStates,
            isCompletedLoading = false,
            isDocumentFlow = true,
            sseProgress = 0.52f,
            sseStatusText = "약 6초 남았어요",
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
