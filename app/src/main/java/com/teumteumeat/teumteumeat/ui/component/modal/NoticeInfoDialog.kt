package com.teumteumeat.teumteumeat.ui.component.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.extendedColors

/** 안내 모달의 제목·본문·버튼 사이 간격 */
private val SECTION_SPACING = 16.dp

/**
 * 공지사항 목록 우상단 정보(ⓘ) 아이콘을 눌렀을 때 화면 중앙에 표시되는 안내 모달.
 * 확인 버튼, 모달 바깥 영역 터치, 뒤로가기 중 어느 것으로든 닫힌다.
 *
 * @param showDialog 모달 표시 여부
 * @param onDismiss  모달을 닫을 때 호출
 */
@Composable
fun NoticeInfoDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
) {
    if (!showDialog) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        BaseModal(
            title = "틈틈잇 소식이란?",
            body = "새로운 기능과 이벤트를 알려드려요.\n놓친 소식은 여기서 다시 확인할 수 있어요!",
            primaryButtonText = "확인",
            onPrimaryClick = onDismiss,
            titleBodySpacing = SECTION_SPACING,
            bodyButtonSpacing = SECTION_SPACING,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NoticeInfoDialogPreview() {
    TeumTeumEatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.extendedColors.backSurface),
            contentAlignment = Alignment.Center
        ) {
            BaseModal(
                title = "틈틈잇 소식이란?",
                body = "새로운 기능과 이벤트를 알려드려요.\n놓친 소식은 여기서 다시 확인할 수 있어요!",
                primaryButtonText = "확인",
                onPrimaryClick = {},
                titleBodySpacing = SECTION_SPACING,
                bodyButtonSpacing = SECTION_SPACING,
            )
        }
    }
}
