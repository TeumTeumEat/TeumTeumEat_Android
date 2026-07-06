package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.teumteumeat.teumteumeat.ui.component.CategorySelectorContent

@Composable
fun CategorySelectScreen(
    name: String = "",
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    navBackStackEntry: NavBackStackEntry,
) {
    CategorySelectorContent(
        speechBubbleText = "어떤 주제에 관심 있으세요?\n맞춤 퀴즈를 준비해드릴게요",
        categories = uiState.categories,
        selection = uiState.categorySelection,
        targetCategoryPage = uiState.targetCategoryPage,
        isLoading = uiState.isLoading,
        pageErrorMessage = uiState.pageErrorMessage,
        isCategorySelectionComplete = uiState.isCategorySelectionComplete,
        loadKey = navBackStackEntry.id,
        onLoadCategories = { viewModel.loadCategories() },
        onNavigateBack = { viewModel.navigateBackInCategoryDepth() },
        onToggleCategory = viewModel::toggleCategory,
        onNext = onNext,
    )
}