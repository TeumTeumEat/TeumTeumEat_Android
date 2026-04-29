package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.runtime.Composable
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.component.LearningMethodSelectorContent

@Composable
fun SelectInputMethodScreen(
    name: String = "",
    onPrev: () -> Unit,
    viewModel: AddGoalViewModel,
    uiState: UiStateAddGoalState,
    onNextFileUpload: () -> Unit,
    onNextCateGorySelect: () -> Unit,
) {
    LearningMethodSelectorContent(
        selectedType = uiState.goalTypeUiState,
        onSelected = { viewModel.selectLearningMethod(it) },
        onNextClick = {
            viewModel.resetCategorySelection()
            viewModel.onFileDeleted()
            viewModel.updateCategorySelectionComplete(false)
            when (uiState.goalTypeUiState) {
                GoalTypeUiState.CATEGORY -> onNextCateGorySelect()
                GoalTypeUiState.DOCUMENT -> onNextFileUpload()
                GoalTypeUiState.NONE -> {}
            }
        },
    )
}