package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.ui.component.StudyPeriodContent
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

@Composable
fun SetStudyPeriodScreen(
    name: String,
    viewModel: AddGoalViewModel,
    uiState: UiStateAddGoalState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    StudyPeriodContent(
        speechBubbleText = "선택한 주제에 대해\n공부할 기간을 선택해주세요!",
        studyPeriod = uiState.studyPeriod,
        onStudyWeekSelected = { viewModel.onStudyWeekSelected(it) },
        onNext = onNext,
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "목표 추가 - 학습 기간 미선택", group = "AddGoal")
@Composable
private fun SetStudyPeriodScreen_NonePreview() {
    TeumTeumEatTheme {
        StudyPeriodContent(
            speechBubbleText = "선택한 주제에 대해\n공부할 기간을 선택해주세요!",
            studyPeriod = null,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "목표 추가 - 1주 선택", group = "AddGoal")
@Composable
private fun SetStudyPeriodScreen_1WeekPreview() {
    TeumTeumEatTheme {
        StudyPeriodContent(
            speechBubbleText = "선택한 주제에 대해\n공부할 기간을 선택해주세요!",
            studyPeriod = 1,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "목표 추가 - 2주 선택", group = "AddGoal")
@Composable
private fun SetStudyPeriodScreen_2WeekPreview() {
    TeumTeumEatTheme {
        StudyPeriodContent(
            speechBubbleText = "선택한 주제에 대해\n공부할 기간을 선택해주세요!",
            studyPeriod = 2,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "목표 추가 - 4주 선택", group = "AddGoal")
@Composable
private fun SetStudyPeriodScreen_4WeekPreview() {
    TeumTeumEatTheme {
        StudyPeriodContent(
            speechBubbleText = "선택한 주제에 대해\n공부할 기간을 선택해주세요!",
            studyPeriod = 4,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}