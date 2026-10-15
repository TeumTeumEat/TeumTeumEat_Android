package com.teumteumeat.teumteumeat.ui.component.modal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

/** 홈 진입 시 노출하는 공지 모달 종류 */
enum class NoticeModalType {
    /** 인스타그램 게시글 공지 — 콘텐츠 클릭 시 게시글 링크로 이동 */
    INSTAGRAM,

    /** 그 외 공지(업데이트 등) — 콘텐츠 클릭 시 공지사항 목록으로 이동 */
    GENERAL,
}

/**
 * 홈 공지 모달에 표시할 UI 모델.
 *
 * @property noticeId 공지 식별자
 * @property type     모달 종류
 * @property title    모달 제목 ([NoticeModalType.GENERAL] 전용, 인스타그램 모달은 고정 제목 사용)
 * @property linkUrl  이동할 링크 ([NoticeModalType.INSTAGRAM] 전용)
 */
data class NoticeModalUiModel(
    val noticeId: Long,
    val type: NoticeModalType,
    val title: String = "",
    val linkUrl: String? = null,
)

private const val INSTAGRAM_MODAL_TITLE = "인스타그램 게시글 !"

/**
 * [NoticeModalUiModel]의 종류에 맞는 공지 모달을 표시한다. 모델이 null이면 아무것도 그리지 않는다.
 *
 * @param onContentClick     제목·이미지 영역 클릭 (타입별 이동은 호출부에서 처리)
 * @param onSnoozeWeekClick  "일주일간 보지않기" 클릭
 * @param onDismiss          바깥 영역 터치·뒤로가기로 닫을 때
 */
@Composable
fun NoticeModalHost(
    model: NoticeModalUiModel?,
    onContentClick: (NoticeModalUiModel) -> Unit,
    onSnoozeWeekClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (model == null) return

    when (model.type) {
        NoticeModalType.INSTAGRAM -> InstagramNoticeDialog(
            showDialog = true,
            onContentClick = { onContentClick(model) },
            onSnoozeWeekClick = onSnoozeWeekClick,
            onDismiss = onDismiss,
        )

        NoticeModalType.GENERAL -> GeneralNoticeDialog(
            showDialog = true,
            title = model.title,
            onContentClick = { onContentClick(model) },
            onSnoozeWeekClick = onSnoozeWeekClick,
            onDismiss = onDismiss,
        )
    }
}

/**
 * 인스타그램 게시글 공지 모달. 콘텐츠를 누르면 게시글 링크로 바로 이동한다.
 */
@Composable
fun InstagramNoticeDialog(
    showDialog: Boolean,
    onContentClick: () -> Unit,
    onSnoozeWeekClick: () -> Unit,
    onDismiss: () -> Unit,
    title: String = INSTAGRAM_MODAL_TITLE,
    @DrawableRes imageRes: Int = R.drawable.food_burger,
) {
    NoticeDialogFrame(
        showDialog = showDialog,
        onDismiss = onDismiss,
    ) {
        NoticePopupCard(
            title = title,
            imageRes = imageRes,
            onContentClick = onContentClick,
            onSnoozeWeekClick = onSnoozeWeekClick,
        )
    }
}

/**
 * 일반 공지(업데이트 등) 모달. 콘텐츠를 누르면 공지사항 목록으로 이동한다.
 */
@Composable
fun GeneralNoticeDialog(
    showDialog: Boolean,
    title: String,
    onContentClick: () -> Unit,
    onSnoozeWeekClick: () -> Unit,
    onDismiss: () -> Unit,
    @DrawableRes imageRes: Int = R.drawable.food_burger,
) {
    NoticeDialogFrame(
        showDialog = showDialog,
        onDismiss = onDismiss,
    ) {
        NoticePopupCard(
            title = title,
            imageRes = imageRes,
            onContentClick = onContentClick,
            onSnoozeWeekClick = onSnoozeWeekClick,
        )
    }
}

/** 공지 모달 공통 Dialog 래퍼. 바깥 터치·뒤로가기로 닫을 수 있다. */
@Composable
private fun NoticeDialogFrame(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
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
        content()
    }
}

/** 공지 모달 공통 카드. 제목 + 이미지(클릭 영역) 아래에 "일주일간 보지않기"를 배치한다. */
@Composable
private fun NoticePopupCard(
    title: String,
    @DrawableRes imageRes: Int,
    onContentClick: () -> Unit,
    onSnoozeWeekClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)

    Surface(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = MaterialTheme.extendedColors.modalShadow,
                spotColor = MaterialTheme.extendedColors.modalShadow,
            )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onContentClick)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.appTypography.titleSemiBold24,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "일주일간 보지않기",
                style = MaterialTheme.appTypography.bodyMedium14,
                color = MaterialTheme.extendedColors.textSecondary,
                modifier = Modifier
                    .clickable(onClick = onSnoozeWeekClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GeneralNoticeModalPreview() {
    TeumTeumEatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.extendedColors.backSurface),
            contentAlignment = Alignment.Center
        ) {
            NoticePopupCard(
                title = "기능 OPEN!",
                imageRes = R.drawable.food_burger,
                onContentClick = {},
                onSnoozeWeekClick = {},
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InstagramNoticeModalPreview() {
    TeumTeumEatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.extendedColors.backSurface),
            contentAlignment = Alignment.Center
        ) {
            NoticePopupCard(
                title = INSTAGRAM_MODAL_TITLE,
                imageRes = R.drawable.food_burger,
                onContentClick = {},
                onSnoozeWeekClick = {},
            )
        }
    }
}
