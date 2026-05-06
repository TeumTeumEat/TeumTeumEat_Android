package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.runtime.Composable
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.component.LearningMethodSelectorContent

@Composable
fun SelectLearningMethodScreen(
    name: String = "",
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNextFileUpload: () -> Unit,
    onPrev: () -> Unit,
    onNextCateGorySelct: () -> Unit,
) {
    LearningMethodSelectorContent(
        selectedType = uiState.goalTypeUiState,
        onSelected = { viewModel.selectLearningMethod(it) },
        onNextClick = {
            viewModel.resetCategorySelection()
            viewModel.onFileDeleted()
            when (uiState.goalTypeUiState) {
                GoalTypeUiState.CATEGORY -> onNextCateGorySelct()
                GoalTypeUiState.DOCUMENT -> onNextFileUpload()
                GoalTypeUiState.NONE -> {}
            }
        },
    )
}