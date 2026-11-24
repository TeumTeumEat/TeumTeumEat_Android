package com.teumteumeat.teumteumeat.ui.screen.c4_notice_list

import com.teumteumeat.teumteumeat.domain.model.notice.NoticeDetail

/**
 * 공지사항 목록 화면(무한 스크롤) UI 상태.
 *
 * @property notices          지금까지 불러온 공지 목록 (최신순)
 * @property lastNoticeId     다음 페이지 요청에 보낼 커서 (마지막으로 불러온 공지 ID, 첫 요청 전에는 null)
 * @property hasNext          다음 페이지 존재 여부 (서버 hasNext)
 * @property isInitialLoading 첫 페이지 로딩 중 여부
 * @property isLoadingMore    다음 페이지 로딩 중 여부 (하단 인디케이터 노출)
 * @property errorMessage     첫 페이지 로딩 실패 메시지 (null이면 정상)
 */
data class UiStateNoticeList(
    val notices: List<NoticeItemUiModel> = emptyList(),
    val lastNoticeId: Long? = null,
    val hasNext: Boolean = true,
    val isInitialLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * 공지 목록 한 줄에 표시할 UI 모델.
 *
 * @property noticeId 공지 식별자
 * @property title    공지 제목
 * @property dateText 표시용 날짜 (예: "3월 12일")
 * @property isNew    읽지 않은 공지 여부 (true면 우측에 빨간 점 노출). 읽음 상태는 로컬에서 판단한다.
 * @property detail   공지를 눌렀을 때 띄울 전체화면 데이터 (공지 상세 화면 연동 시 사용)
 */
data class NoticeItemUiModel(
    val noticeId: Long,
    val title: String,
    val dateText: String,
    val isNew: Boolean,
    val detail: NoticeDetail? = null,
)
