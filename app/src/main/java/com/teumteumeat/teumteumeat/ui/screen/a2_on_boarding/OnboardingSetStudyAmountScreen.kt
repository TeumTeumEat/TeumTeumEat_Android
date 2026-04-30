package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.ui.component.StudyAmountContent
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

@Composable
fun SetStudyAmountScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    StudyAmountContent(
        speechBubbleText = "얼마 동안 공부해 볼까요?\n내 속도에 맞는 학습 기간을 정해보세요",
        studyPeriod = uiState.studyPeriod,
        onStudyWeekSelected = { viewModel.onStudyWeekSelected(it) },
        onNext = onNext,
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "온보딩 - 학습 기간 미선택", group = "Onboarding")
@Composable
private fun SetStudyPeriodScreen_NonePreview() {
    TeumTeumEatTheme {
        StudyAmountContent(
            speechBubbleText = "얼마 동안 공부해 볼까요?\n내 속도에 맞는 학습 기간을 정해보세요",
            studyPeriod = null,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "온보딩 - 1주 선택", group = "Onboarding")
@Composable
private fun SetStudyPeriodScreen_1WeekPreview() {
    TeumTeumEatTheme {
        StudyAmountContent(
            speechBubbleText = "얼마 동안 공부해 볼까요?\n내 속도에 맞는 학습 기간을 정해보세요",
            studyPeriod = 1,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "온보딩 - 4주 선택", group = "Onboarding")
@Composable
private fun SetStudyPeriodScreen_4WeekPreview() {
    TeumTeumEatTheme {
        StudyAmountContent(
            speechBubbleText = "얼마 동안 공부해 볼까요?\n내 속도에 맞는 학습 기간을 정해보세요",
            studyPeriod = 4,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}