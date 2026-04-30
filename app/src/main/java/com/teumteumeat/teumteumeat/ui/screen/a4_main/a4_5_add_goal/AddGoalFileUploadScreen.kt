package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.runtime.Composable
import com.teumteumeat.teumteumeat.ui.component.FileUploadContent

@Composable
fun AddGoalFileUploadScreen(
    name: String = "",
    viewModel: AddGoalViewModel,
    uiState: UiStateAddGoalState,
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