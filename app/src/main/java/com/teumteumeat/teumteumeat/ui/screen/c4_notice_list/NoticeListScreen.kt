package com.teumteumeat.teumteumeat.ui.screen.c4_notice_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.header.TitleBar
import com.teumteumeat.teumteumeat.ui.component.modal.NoticeInfoDialog
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

/** 마지막 아이템에서 몇 개 앞에 도달했을 때 다음 페이지를 요청할지 결정하는 값 */
private const val LOAD_MORE_THRESHOLD = 3

/**
 * 공지사항 목록 화면 ("틈틈잇 소식").
 *
 * @param uiState        목록·로딩 상태
 * @param onBackClick    뒤로가기 클릭
 * @param onNoticeClick  공지 항목 클릭 (noticeId 전달)
 * @param onLoadMore     목록 하단 근처 도달 시 다음 페이지 요청
 * @param onRetryClick   첫 페이지 로딩 실패 후 재시도
 */
@Composable
fun NoticeListScreen(
    uiState: UiStateNoticeList,
    onBackClick: () -> Unit,
    onNoticeClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val theme = MaterialTheme.extendedColors

    // 우측 상단 정보(ⓘ) 아이콘 안내 모달 표시 여부 (화면 내부 UI 상태)
    var showInfoDialog by remember { mutableStateOf(false) }

    DefaultMonoBg {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            topBar = {
                TitleBar(
                    title = "틈틈잇 소식",
                    onBackClick = onBackClick,
                    rightContent = {
                        IconButton(
                            onClick = { showInfoDialog = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "공지사항 안내",
                                tint = theme.iconBlack,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(theme.backSurface)
            ) {
                when {
                    uiState.errorMessage != null && uiState.notices.isEmpty() -> {
                        FullScreenErrorModal(
                            errorState = ErrorState(
                                title = "문제가 발생했어요",
                                description = uiState.errorMessage,
                                onRetry = onRetryClick,
                            ),
                            isShowBackBtn = false,
                            onBack = onBackClick,
                        )
                    }

                    uiState.isInitialLoading -> {
                        LoadingScreen(
                            title = "소식 불러오는 중",
                            message = "잠시만 기다려주세요"
                        )
                    }

                    uiState.notices.isEmpty() -> {
                        NoticeEmptyContent(modifier = Modifier.align(Alignment.Center))
                    }

                    else -> {
                        NoticeList(
                            uiState = uiState,
                            onNoticeClick = onNoticeClick,
                            onLoadMore = onLoadMore,
                        )
                    }
                }
            }
        }

        NoticeInfoDialog(
            showDialog = showInfoDialog,
            onDismiss = { showInfoDialog = false },
        )
    }
}

/** 무한 스크롤 공지 목록. 하단 [LOAD_MORE_THRESHOLD]개 이내로 스크롤되면 [onLoadMore]를 호출한다. */
@Composable
private fun NoticeList(
    uiState: UiStateNoticeList,
    onNoticeClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            lastVisibleIndex >= layoutInfo.totalItemsCount - 1 - LOAD_MORE_THRESHOLD
        }
    }

    // 페이지가 추가된 뒤에도 여전히 하단 근처라면(짧은 목록 등) 다음 페이지를 이어서 요청한다.
    LaunchedEffect(shouldLoadMore, uiState.notices.size) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = uiState.notices,
            key = { it.noticeId }
        ) { notice ->
            NoticeRow(
                notice = notice,
                onClick = { onNoticeClick(notice.noticeId) }
            )
        }

        if (uiState.isLoadingMore) {
            item(key = "loading_more") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.extendedColors.backSurface)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.extendedColors.primary
                    )
                }
            }
        }
    }
}

/** 공지 한 줄. 제목 + 날짜, 읽지 않은 공지면 우측에 빨간 점을 표시한다. */
@Composable
private fun NoticeRow(
    notice: NoticeItemUiModel,
    onClick: () -> Unit,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.backgroundW100)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notice.title,
                    style = typo.bodyMedium14,
                    color = theme.iconBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notice.dateText,
                    style = typo.captionRegular12,
                    color = theme.textSecondary,
                )
            }

            if (notice.isNew) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 12.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(theme.alertPrimary)
                )
            }
        }

        // 항목 구분선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(theme.unableContainer)
        )
    }
}

/** 등록된 공지가 없을 때 표시하는 빈 화면 */
@Composable
private fun NoticeEmptyContent(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = "등록된 공지사항이 없어요",
        style = MaterialTheme.appTypography.bodyMedium14Reg,
        color = MaterialTheme.extendedColors.textSecondary
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NoticeListScreenPreview() {
    TeumTeumEatTheme {
        NoticeListScreen(
            uiState = UiStateNoticeList(
                isInitialLoading = false,
                notices = listOf(
                    NoticeItemUiModel(3, "틈틈잇 공지사항입니다!", "3월 12일", isNew = true),
                    NoticeItemUiModel(2, "리그 관련 오픈 이벤트", "2월 22일", isNew = true),
                    NoticeItemUiModel(1, "틈틈잇 공지사항입니다!", "1월 11일", isNew = false),
                ),
                hasNext = false,
            ),
            onBackClick = {},
            onNoticeClick = {},
            onLoadMore = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NoticeListScreenLoadingMorePreview() {
    TeumTeumEatTheme {
        NoticeListScreen(
            uiState = UiStateNoticeList(
                isInitialLoading = false,
                isLoadingMore = true,
                notices = listOf(
                    NoticeItemUiModel(2, "리그 관련 오픈 이벤트", "2월 22일", isNew = false),
                    NoticeItemUiModel(1, "틈틈잇 공지사항입니다!", "1월 11일", isNew = false),
                ),
            ),
            onBackClick = {},
            onNoticeClick = {},
            onLoadMore = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NoticeListScreenEmptyPreview() {
    TeumTeumEatTheme {
        NoticeListScreen(
            uiState = UiStateNoticeList(isInitialLoading = false, hasNext = false),
            onBackClick = {},
            onNoticeClick = {},
            onLoadMore = {},
            onRetryClick = {},
        )
    }
}
