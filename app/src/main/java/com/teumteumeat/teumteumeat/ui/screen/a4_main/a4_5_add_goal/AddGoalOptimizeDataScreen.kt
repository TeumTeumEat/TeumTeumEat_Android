package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.domain.model.defaultRequestPromptOptions
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.ui.component.OptimizeDataContent
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.DifficultyOption
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

@Composable
fun AddGoalOptimizerDataScreen(
    name: String,
    viewModel: AddGoalViewModel,
    uiState: UiStateAddGoalState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onCloseSheet: () -> Unit,
    onConfirmPrompt: () -> Unit,
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
        sheetTitle = "요청 프롬프트 선택",
        isNextEnabled = isNextEnabled,
        onDifficultySelected = viewModel::onDifficultySelected,
        onOpenPromptSheet = onOpenPromptSheet,
        onCloseSheet = onCloseSheet,
        onConfirmPrompt = onConfirmPrompt,
        onPromptSelected = viewModel::onPromptSelected,
        onNext = onNext,
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────

private val previewDifficultyOptions = listOf(
    DifficultyOption("상", Difficulty.HARD),
    DifficultyOption("중", Difficulty.MEDIUM),
    DifficultyOption("하", Difficulty.EASY),
)

@Preview(name = "목표 추가 - 초기 상태", showBackground = true)
@Composable
private fun AddGoalOptimizerDataScreen_InitialPreview() {
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

@Preview(name = "목표 추가 - 난이도 선택 완료", showBackground = true)
@Composable
private fun AddGoalOptimizerDataScreen_DifficultySelectedPreview() {
    TeumTeumEatTheme {
        OptimizeDataContent(
            speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
            difficulty = Difficulty.HARD,
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

@Preview(name = "목표 추가 - 퀴즈 유형 선택 완료", showBackground = true)
@Composable
private fun AddGoalOptimizerDataScreen_PromptSelectedPreview() {
    TeumTeumEatTheme {
        OptimizeDataContent(
            speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
            difficulty = Difficulty.MEDIUM,
            difficultyOptions = previewDifficultyOptions,
            promptInput = "면접에 도움이 되는 내용으로 만들어주세요.",
            promptOptions = defaultRequestPromptOptions,
            selectedPromptId = "interview",
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

@Preview(name = "목표 추가 - 바텀시트 표시", showBackground = true)
@Composable
private fun AddGoalOptimizerDataScreen_BottomSheetPreview() {
    TeumTeumEatTheme {
        OptimizeDataContent(
            speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
            difficulty = Difficulty.MEDIUM,
            difficultyOptions = previewDifficultyOptions,
            promptInput = "",
            promptOptions = defaultRequestPromptOptions,
            selectedPromptId = "exam",
            showBottomSheet = true,
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