package com.teumteumeat.teumteumeat.ui.screen.common_screen

data class ErrorState(
    val title: String,
    val description: String,
    val retryLabel: String = "다시 시도하기",
    val onRetry: () -> Unit,

    // Secondary Action (선택: 재로그인)
    val secondaryLabel: String? = null,
    val onSecondaryAction: (() -> Unit)? = null,
)