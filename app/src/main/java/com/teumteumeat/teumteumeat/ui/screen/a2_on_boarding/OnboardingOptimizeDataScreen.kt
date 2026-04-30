package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.domain.model.defaultRequestPromptOptions
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.ui.component.OptimizeDataContent
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

@Composable
fun OptimizeDataScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onCloseSheet: () -> Unit,
    onConfirmPrompt: () -> Unit,
    setSheetTitle: String,
    onOpenPromptSheet: () -> Unit,
) {
    val difficultyOptions = listOf(
        DifficultyOption("상", Difficulty.HARD),
        DifficultyOption("중", Difficulty.MEDIUM),
        DifficultyOption("하", Difficulty.EASY),
    )
    val isNextEnabled = uiState.promptInput.length <= 30 &&
            uiState.difficulty != Difficulty.NONE

    OptimizeDataContent(
        speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
        difficulty = uiState.difficulty,
        difficultyOptions = difficultyOptions,
        promptInput = uiState.promptInput,
        promptOptions = uiState.promptOptions,
        selectedPromptId = uiState.selectedPromptId,
        showBottomSheet = uiState.showBottomSheet,
        sheetTitle = setSheetTitle,
        isNextEnabled = isNextEnabled,
        onDifficultySelected = viewModel::onDifficultySelected,
        onOpenPromptSheet = onOpenPromptSheet,
        onCloseSheet = onCloseSheet,
        onConfirmPrompt = onConfirmPrompt,
        onPromptSelected = viewModel::onPromptSelected,
        onNext = {
            viewModel.setUserName()
            onNext()
        },
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────

private val previewDifficultyOptions = listOf(
    DifficultyOption("상", Difficulty.HARD),
    DifficultyOption("중", Difficulty.MEDIUM),
    DifficultyOption("하", Difficulty.EASY),
)

@Preview(name = "온보딩 - 초기 상태", showBackground = true)
@Composable
private fun OptimizeDataScreen_InitialPreview() {
    TeumTeumEatTheme {
        OptimizeDataContent(
            speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
            difficulty = Difficulty.NONE,
            difficultyOptions = previewDifficultyOptions,
            promptInput = "",
            promptOptions = defaultRequestPromptOptions,
            selectedPromptId = null,
            showBottomSheet = false,
            sheetTitle = "요청 프롬프트 선택",
            isNextEnabled = false,
            onDifficultySelected = {},
            onOpenPromptSheet = {},
            onCloseSheet = {},
            onConfirmPrompt = {},
            onPromptSelected = {},
            onNext = {},
        )
    }
}

@Preview(name = "온보딩 - 난이도 선택 완료", showBackground = true)
@Composable
private fun OptimizeDataScreen_DifficultySelectedPreview() {
    TeumTeumEatTheme {
        OptimizeDataContent(
            speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
            difficulty = Difficulty.MEDIUM,
            difficultyOptions = previewDifficultyOptions,
            promptInput = "",
            promptOptions = defaultRequestPromptOptions,
            selectedPromptId = null,
            showBottomSheet = false,
            sheetTitle = "요청 프롬프트 선택",
            isNextEnabled = true,
            onDifficultySelected = {},
            onOpenPromptSheet = {},
            onCloseSheet = {},
            onConfirmPrompt = {},
            onPromptSelected = {},
            onNext = {},
        )
    }
}

@Preview(name = "온보딩 - 퀴즈 유형 선택 완료", showBackground = true)
@Composable
private fun OptimizeDataScreen_PromptSelectedPreview() {
    TeumTeumEatTheme {
        OptimizeDataContent(
            speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
            difficulty = Difficulty.EASY,
            difficultyOptions = previewDifficultyOptions,
            promptInput = "기초부터 차근차근 개념을 익히고 싶어요.",
            promptOptions = defaultRequestPromptOptions,
            selectedPromptId = "step_by_step",
            showBottomSheet = false,
            sheetTitle = "요청 프롬프트 선택",
            isNextEnabled = true,
            onDifficultySelected = {},
            onOpenPromptSheet = {},
            onCloseSheet = {},
            onConfirmPrompt = {},
            onPromptSelected = {},
            onNext = {},
        )
    }
}