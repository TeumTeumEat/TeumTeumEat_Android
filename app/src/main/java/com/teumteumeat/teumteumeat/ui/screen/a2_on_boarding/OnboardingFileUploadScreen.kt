package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.runtime.Composable
import com.teumteumeat.teumteumeat.ui.component.FileUploadContent

@Composable
fun FileUploadScreen(
    name: String = "",
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    FileUploadContent(
        selectedFileName = uiState.selectedFileName,
        pageErrorMessage = uiState.pageErrorMessage,
        onFileSelected = { uri, fileName, mimeType, size ->
            viewModel.onFileSelected(uri, fileName, mimeType, size)
        },
        onFileDeleted = { viewModel.onFileDeleted() },
        onErrorShown = { viewModel.clearPageErrorMessage() },
        onNext = onNext,
    )
}