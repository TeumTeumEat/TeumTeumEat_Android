package com.teumteumeat.teumteumeat.data.network.model_response.notice

/**
 * `GET /api/v1/notices` 응답의 `data`.
 *
 * @property notices 최신순 공지 목록
 * @property hasNext 다음 페이지 존재 여부. false이면 더 조회할 공지가 없다.
 */
data class NoticeListResponse(
    val notices: List<NoticeResponse>,
    val hasNext: Boolean
)

/**
 * 공지 한 건.
 *
 * @property noticeId 공지 식별자. 다음 페이지 요청의 `lastNoticeId` 커서로 사용된다.
 * @property title 공지 제목
 * @property createdDate 등록 일시. 타임존 없는 ISO-8601 (예: `2026-10-01T12:00:00`)
 * @property detail 공지를 눌렀을 때 띄울 전체화면 데이터
 */
data class NoticeResponse(
    val noticeId: Long,
    val title: String,
    val createdDate: String,
    val detail: NoticeDetailResponse?
)

/**
 * 공지 전체화면 데이터.
 *
 * @property fullScreenImageUrl 전체화면 이미지 URL. 리사이즈는 URL 뒤 쿼리(`?w=&h=&f=&q=`)로 요청한다.
 * @property externalLink 외부 URL. 값이 있으면 액션 버튼이 외부 링크로 이동한다.
 * @property deepLink 이동 링크. 값이 있으면 액션 버튼이 해당 링크로 이동한다.
 * @property actionButtonText 액션 버튼 문구
 */
data class NoticeDetailResponse(
    val fullScreenImageUrl: String?,
    val externalLink: String?,
    val deepLink: String?,
    val actionButtonText: String?
)
