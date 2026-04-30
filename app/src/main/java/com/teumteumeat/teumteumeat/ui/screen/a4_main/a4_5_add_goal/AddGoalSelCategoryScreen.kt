package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.teumteumeat.teumteumeat.ui.component.CategorySelectorContent

@Composable
fun AddGoalCategorySelectScreen(
    name: String = "",
    viewModel: AddGoalViewModel,
    uiState: UiStateAddGoalState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    navBackStackEntry: NavBackStackEntry,
) {
    CategorySelectorContent(
        speechBubbleText = "관심 있는 분야를\n알려주세요",
        categories = uiState.categories,
        selection = uiState.categorySelection,
        targetCategoryPage = uiState.targetCategoryPage,
        isLoading = uiState.isLoading,
        pageErrorMessage = uiState.pageErrorMessage,
        isCategorySelectionComplete = uiState.isCategorySelectionComplete,
        loadKey = navBackStackEntry.id,
        onLoadCategories = { viewModel.loadCategories() },
        onNavigateBack = { viewModel.navigateBackInCategoryDepth() },
        onToggleDepth1 = viewModel::toggleDepth1,
        onToggleDepth2 = viewModel::toggleDepth2,
        onToggleDepth3 = viewModel::toggleDepth3,
        onToggleDepth4 = viewModel::toggleDepth4,
        onNext = onNext,
    )
}