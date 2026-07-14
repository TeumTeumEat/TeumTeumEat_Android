package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.SubmitLoadingScreen
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

/**
 * SSE 상태별 PDF 등록 로딩 화면 애니메이션 디버그 뷰.
 *
 * 상단 칩을 클릭해 각 SSE 이벤트 상태로 전환하면서 애니메이션을 확인한다.
 * 프로덕션 코드에서는 참조하지 않는다.
 */
private enum class SseDebugState(
    val label: String,
    val step0: Boolean,
    val step1: Boolean,
    val step2: Boolean,
    val sseProgress: Float,
    val sseRemainMs: Long?,
    val sseStatusText: String?,
    val sseProgressText: String?,
) {
    UPLOAD(
        label = "파일 업로드",
        step0 = true,  step1 = false, step2 = false,
        sseProgress = 0f, sseRemainMs = null, sseStatusText = null,    sseProgressText = null,
    ),
    REGISTER(
        label = "문서 등록",
        step0 = true,  step1 = true,  step2 = false,
        sseProgress = 0f, sseRemainMs = null, sseStatusText = null,    sseProgressText = null,
    ),
    CONNECTED(
        label = "SSE 연결",
        step0 = true,  step1 = true,  step2 = true,
        sseProgress = 0f, sseRemainMs = null, sseStatusText = null,    sseProgressText = null,
    ),
    PENDING(
        label = "PENDING",
        step0 = true,  step1 = true,  step2 = true,
        sseProgress = 0.05f, sseRemainMs = null, sseStatusText = null,    sseProgressText = null,
    ),
    PROCESSING_30S(
        label = "PROCESSING·30초",
        step0 = true,  step1 = true,  step2 = true,
        sseProgress = 0.30f, sseRemainMs = 30_000L, sseStatusText = "30초 남았어요.", sseProgressText = "30% 완료",
    ),
    PROCESSING_10S(
        label = "PROCESSING·10초",
        step0 = true,  step1 = true,  step2 = true,
        sseProgress = 0.70f, sseRemainMs = 10_000L, sseStatusText = "10초 남았어요.", sseProgressText = "70% 완료",
    ),
    PROCESSING_3S(
        label = "PROCESSING·3초",
        step0 = true,  step1 = true,  step2 = true,
        sseProgress = 0.91f, sseRemainMs = 3_000L, sseStatusText = "3초 남았어요.",  sseProgressText = "91% 완료",
    ),
    PROCESSING_OVER(
        label = "시간 초과 중",
        step0 = true,  step1 = true,  step2 = true,
        sseProgress = 0.99f, sseRemainMs = 0L, sseStatusText = "잠시만 기다려주세요", sseProgressText = null,
    ),
    COMPLETED(
        label = "COMPLETED",
        step0 = true,  step1 = true,  step2 = true,
        sseProgress = 1.0f, sseRemainMs = 0L, sseStatusText = null,    sseProgressText = null,
    ),
}

@Composable
fun DocumentLoadingDebugScreen() {
    val initialState = SseDebugState.UPLOAD
    var selectedState by remember { mutableStateOf(initialState) }
    val visibleStates = remember {
        mutableStateListOf(initialState.step0, initialState.step1, initialState.step2)
    }

    LaunchedEffect(selectedState) {
        visibleStates[0] = selectedState.step0
        visibleStates[1] = selectedState.step1
        visibleStates[2] = selectedState.step2
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SseDebugState.values().forEach { state ->
                FilterChip(
                    selected = selectedState == state,
                    onClick = { selectedState = state },
                    label = {
                        Text(
                            text = state.label,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
        }

        SubmitLoadingScreen(
            visibleStates = visibleStates,
            isCompletedLoading = true,
            isDocumentFlow = true,
            sseProgress = selectedState.sseProgress,
            sseRemainMs = selectedState.sseRemainMs,
            sseStatusText = selectedState.sseStatusText,
            sseProgressText = selectedState.sseProgressText,
            onAnimationComplete = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "PDF 로딩 SSE 디버그")
@Composable
private fun DocumentLoadingDebugPreview() {
    TeumTeumEatTheme {
        DocumentLoadingDebugScreen()
    }
}
