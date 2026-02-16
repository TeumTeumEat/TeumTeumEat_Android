package com.teumteumeat.teumteumeat.ui.screen.common_screen

data class ProcessingUiState(
    val progress: Float = 0f,   // 0f ~ 1f
    val estimateTime: Long = 0L
)
