package com.teumteumeat.teumteumeat.ui.component.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.extendedColors

/**
 * 목표 완료 상태에서 홈화면 진입 시 표시되는 안내 팝업.
 * [onStartNewGoal] : 새 목표 시작하기 버튼 클릭
 * [onDismiss]      : 닫기 버튼 클릭
 */
@Composable
fun GoalCompletedDialog(
    showDialog: Boolean,
    onStartNewGoal: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!showDialog) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        BaseModal(
            title = "풀고 있는 틈틈잇이 없어요",
            body = "먹을 간식이 없어요!\n새로운 지식을 먹여줄래요?",
            primaryButtonText = "진행중인 틈틈잇 선택하기",
            secondaryButtonText = "새로운 틈틈잇 시작하기",
            isVerticalButtons = true,
            onPrimaryClick = onStartNewGoal,
            onSecondaryClick = onDismiss,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GoalCompletedDialogPreview() {
    TeumTeumEatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.extendedColors.backSurface),
            contentAlignment = Alignment.Center
        ) {
            BaseModal(
                title = "풀고 있는 틈틈잇이 없어요",
                body = "먹을 간식이 없어요!\n새로운 지식을 먹여줄래요?",
                primaryButtonText = "진행중인 틈틈잇 선택하기",
                secondaryButtonText = "새로운 틈틈잇 시작하기",
                isVerticalButtons = true,
                onPrimaryClick = {},
                onSecondaryClick = {},
            )
        }
    }
}
