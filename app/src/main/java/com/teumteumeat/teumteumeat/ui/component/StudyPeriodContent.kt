package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.StudyWeekOption
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

private val defaultStudyWeekOptions = listOf(
    StudyWeekOption("1주", 1),
    StudyWeekOption("2주", 2),
    StudyWeekOption("3주", 3),
    StudyWeekOption("4주", 4),
)

/**
 * 온보딩/목표 추가 플로우에서 공용으로 사용하는 학습 기간 설정 화면.
 *
 * @param speechBubbleText 상단 캐릭터 말풍선에 표시할 텍스트
 * @param studyPeriod 현재 선택된 학습 기간 (주 단위, null = 미선택)
 * @param onStudyWeekSelected 학습 기간 선택 콜백
 * @param onNext 다음으로 버튼 클릭 콜백
 */
@Composable
fun StudyPeriodContent(
    speechBubbleText: String,
    studyPeriod: Int?,
    onStudyWeekSelected: (Int) -> Unit,
    onNext: () -> Unit,
) {
    DefaultMonoBg(
        extensionHeight = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SpeechBubble(text = speechBubbleText)
                    Image(
                        painter = painterResource(R.drawable.char_onboarding_five_four),
                        contentDescription = "앞을 보는 캐릭터",
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    WeekRadioGroup(
                        options = defaultStudyWeekOptions,
                        selectedValue = studyPeriod,
                        onSelect = { option -> onStudyWeekSelected(option.value) }
                    )
                    Spacer(modifier = Modifier.height(60.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = MaterialTheme.appTypography.btnBold20_h24.copy(
                            color = MaterialTheme.extendedColors.backgroundW100
                        ),
                        isEnabled = studyPeriod != null,
                        onClick = onNext,
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "학습 기간 - 미선택")
@Composable
private fun StudyPeriodContent_NonePreview() {
    TeumTeumEatTheme {
        StudyPeriodContent(
            speechBubbleText = "얼마 동안 공부해 볼까요?\n내 속도에 맞는 학습 기간을 정해보세요",
            studyPeriod = null,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "학습 기간 - 1주 선택")
@Composable
private fun StudyPeriodContent_1WeekPreview() {
    TeumTeumEatTheme {
        StudyPeriodContent(
            speechBubbleText = "얼마 동안 공부해 볼까요?\n내 속도에 맞는 학습 기간을 정해보세요",
            studyPeriod = 1,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "학습 기간 - 4주 선택")
@Composable
private fun StudyPeriodContent_4WeekPreview() {
    TeumTeumEatTheme {
        StudyPeriodContent(
            speechBubbleText = "얼마 동안 공부해 볼까요?\n내 속도에 맞는 학습 기간을 정해보세요",
            studyPeriod = 4,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}