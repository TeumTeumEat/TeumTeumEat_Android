package com.teumteumeat.teumteumeat.ui.screen.common_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import com.teumteumeat.teumteumeat.ui.component.modal.BaseModal

@Composable
fun PopupOverlay(
    popoUpErrorTitle: String?,
    popUpErrorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isPrimaryBtnFillSecondary: Boolean = false,
) {
    // ❗ 에러가 없으면 렌더링하지 않음
    if (popoUpErrorTitle.isNullOrBlank()) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {
        // 1️⃣ Dim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onDismiss()
                }
        )

        // 2️⃣ Center modal
        Box(
            modifier = Modifier.align(Alignment.Center)
        ) {
            BaseModal(
                title = popoUpErrorTitle,
                body = popUpErrorMessage ?: "",
                primaryButtonText = "확인",
                secondaryButtonText = null, // ✅ 단일 버튼
                onPrimaryClick = onConfirm,
                onSecondaryClick = null,
                isPrimaryBtnFillSecondary = isPrimaryBtnFillSecondary
            )
        }
    }
}
