package com.teumteumeat.teumteumeat.ui.screen.common_screen

data class ErrorState(
    val title: String,
    val description: String,
    val retryLabel: String = "다시 시도하기",
    val onRetry: () -> Unit
)