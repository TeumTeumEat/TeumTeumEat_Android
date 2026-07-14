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
 * [hasRunningGoal]      : 선택 가능한 진행중인 목표 존재 여부 — false면 [onSelectRunningGoal] 버튼 비활성화
 * [onStartNewGoal]      : 새로운 틈틈잇 시작하기 버튼 클릭 (목표 추가 화면으로 이동)
 * [onSelectRunningGoal] : 진행중인 틈틈잇 선택하기 버튼 클릭 (목표 선택 화면으로 이동)
 * [onDismiss]           : 뒤로가기 등으로 팝업을 닫을 때 호출
 */
@Composable
fun GoalCompletedDialog(
    showDialog: Boolean,
    hasRunningGoal: Boolean,
    onStartNewGoal: () -> Unit,
    onSelectRunningGoal: () -> Unit,
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
            isPrimaryBtnEnabled = hasRunningGoal,
            onPrimaryClick = onSelectRunningGoal,
            onSecondaryClick = onStartNewGoal,
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
