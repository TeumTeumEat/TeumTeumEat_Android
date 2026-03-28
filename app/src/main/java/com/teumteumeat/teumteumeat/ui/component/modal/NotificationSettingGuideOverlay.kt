package com.teumteumeat.teumteumeat.ui.component.modal

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
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.NotificationSettingGuideType

@Composable
fun NotificationSettingGuideOverlay(
    notificationGuideType: NotificationSettingGuideType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (notificationGuideType == NotificationSettingGuideType.NONE) return

    // ✅ 타입에 따라 문구/타이틀 분기
    val (title, body, primary, secondary) = when (notificationGuideType) {
        NotificationSettingGuideType.ENABLE -> {
            Quad(
                "알림을 켜려면 설정이 필요해요",
                "알림 권한이 꺼져 있어요.\n기기 설정에서 알림을 허용해주세요.",
                "설정 화면",
                "취소"
            )
        }

        NotificationSettingGuideType.DISABLE -> {
            Quad(
                "알림을 끄려면 설정이 필요해요",
                "알림은 앱에서 직접 끌 수 없어요.\n기기 설정에서 변경할 수 있어요.",
                "설정 화면",
                "취소"
            )
        }

        NotificationSettingGuideType.NONE -> {
            // 여기로 올 일 없음
            Quad("", "", "", "")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {
        // 1) Dim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        )

        // 2) Center modal
        Box(modifier = Modifier.align(Alignment.Center)) {
            BaseModal(
                title = title,
                body = body,
                primaryButtonText = primary,
                secondaryButtonText = secondary,
                onPrimaryClick = onConfirm,
                onSecondaryClick = onDismiss
            )
        }
    }
}

/**
 * 간단히 4개 값을 묶기 위한 helper (data class 사용)
 */
private data class Quad(
    val first: String,
    val second: String,
    val third: String,
    val fourth: String
)
