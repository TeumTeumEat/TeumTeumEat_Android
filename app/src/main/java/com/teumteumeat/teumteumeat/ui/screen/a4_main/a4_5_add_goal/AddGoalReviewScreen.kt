package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.data.mapper.toLable
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.ui.component.ReviewContent
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

@Composable
fun ReviewScreen(
    viewModel: AddGoalViewModel,
    uiState: UiStateAddGoalState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    val subjectLabel = if (uiState.goalTypeUiState == GoalTypeUiState.CATEGORY) "관심분야" else "문서이름"
    val subjectText = when (uiState.goalTypeUiState) {
        GoalTypeUiState.DOCUMENT -> uiState.selectedFileName.ifEmpty { "선택된 파일 없음" }
        GoalTypeUiState.CATEGORY -> listOfNotNull(
            uiState.categorySelection.depth1?.name ?: "IT",
            uiState.categorySelection.depth2?.name,
            uiState.categorySelection.depth3?.name,
            uiState.categorySelection.depth4?.name,
        ).joinToString(" > ").ifEmpty { "선택된 카테고리 없음" }

        else -> "선택 안함"
    }
    val promptText = if (uiState.promptInput.isBlank()) "미선택" else uiState.promptInput
    val studyPeriodText = uiState.studyPeriod?.let { "${it}주" } ?: "기간 설정 안함"

    ReviewContent(
        speechBubbleText = "'다음으로' 버튼을 눌러\n틈틈잇을 시작해보세요!",
        hintText = "수정이 필요하면\n왼쪽 상단의 < 돌아가기를 눌러주세요",
        subjectLabel = subjectLabel,
        subjectText = subjectText,
        difficultyText = uiState.difficulty.toLable(),
        promptText = promptText,
        studyPeriodText = studyPeriodText,
        onNext = {
            viewModel.submitOnBoarding()
        },
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "목표 추가 - 카테고리 선택 / 프롬프트 선택", group = "AddGoal")
@Composable
private fun ReviewScreen_CategoryPromptPreview() {
    TeumTeumEatTheme {
        ReviewContent(
            speechBubbleText = "'다음으로' 버튼을 눌러\n틈틈잇을 시작해보세요!",
            hintText = "수정이 필요하면\n왼쪽 상단의 < 돌아가기를 눌러주세요",
            subjectLabel = "관심분야",
            subjectText = "IT > 앱개발 > Android",
            difficultyText = "중",
            promptText = "출퇴근길에 가볍게 풀 수 있게 만들어주세요.",
            studyPeriodText = "2주",
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "목표 추가 - 문서 / 프롬프트 미선택", group = "AddGoal")
@Composable
private fun ReviewScreen_DocumentNoPromptPreview() {
    TeumTeumEatTheme {
        ReviewContent(
            speechBubbleText = "'다음으로' 버튼을 눌러\n틈틈잇을 시작해보세요!",
            hintText = "수정이 필요하면\n왼쪽 상단의 < 돌아가기를 눌러주세요",
            subjectLabel = "문서이름",
            subjectText = "학습자료_2024.pdf",
            difficultyText = "하",
            promptText = "입력 안함",
            studyPeriodText = "4주",
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "목표 추가 - 난이도 상 / 4주", group = "AddGoal")
@Composable
private fun ReviewScreen_HardLongPreview() {
    TeumTeumEatTheme {
        ReviewContent(
            speechBubbleText = "'다음으로' 버튼을 눌러\n틈틈잇을 시작해보세요!",
            hintText = "수정이 필요하면\n왼쪽 상단의 < 돌아가기를 눌러주세요",
            subjectLabel = "관심분야",
            subjectText = "IT > 앱개발 > iOS > Swift",
            difficultyText = Difficulty.HARD.toLable(),
            promptText = "심화 개념까지 깊이 있게 다뤄주세요.",
            studyPeriodText = "4주",
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "목표 추가 - 카테고리 미선택 초기", group = "AddGoal")
@Composable
private fun ReviewScreen_InitialPreview() {
    TeumTeumEatTheme {
        ReviewContent(
            speechBubbleText = "'다음으로' 버튼을 눌러\n틈틈잇을 시작해보세요!",
            hintText = "수정이 필요하면\n왼쪽 상단의 < 돌아가기를 눌러주세요",
            subjectLabel = "관심분야",
            subjectText = "선택된 카테고리 없음",
            difficultyText = Difficulty.NONE.toLable(),
            promptText = "입력 안함",
            studyPeriodText = "기간 설정 안함",
            onNext = {},
        )
    }
}
