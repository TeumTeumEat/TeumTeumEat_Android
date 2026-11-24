package com.teumteumeat.teumteumeat.domain.model.notice

import java.time.LocalDateTime

/**
 * 공지사항 한 건.
 *
 * @property noticeId 공지 식별자
 * @property title 공지 제목
 * @property createdDate 등록 일시. 서버 값을 해석할 수 없으면 null
 * @property detail 공지 전체화면 데이터. 서버가 내려주지 않으면 null
 */
data class Notice(
    val noticeId: Long,
    val title: String,
    val createdDate: LocalDateTime?,
    val detail: NoticeDetail?
)

/**
 * 공지 전체화면 데이터.
 *
 * [externalLink]와 [deepLink]가 모두 null이면 액션 버튼을 노출하지 않는다.
 * (빈 문자열은 매퍼에서 null로 정규화된다)
 *
 * @property fullScreenImageUrl 전체화면 이미지 URL (리사이즈 쿼리가 붙지 않은 원본 URL)
 * @property externalLink 외부 URL
 * @property deepLink 이동 링크
 * @property actionButtonText 액션 버튼 문구
 */
data class NoticeDetail(
    val fullScreenImageUrl: String?,
    val externalLink: String?,
    val deepLink: String?,
    val actionButtonText: String?
) {
    /** 액션 버튼을 노출해야 하는지 여부 */
    val hasAction: Boolean
        get() = externalLink != null || deepLink != null
}

/**
 * 공지 목록 한 페이지.
 *
 * @property notices 최신순 공지 목록
 * @property hasNext 다음 페이지 존재 여부
 */
data class NoticePage(
    val notices: List<Notice>,
    val hasNext: Boolean
) {
    /** 다음 페이지 요청에 사용할 커서(`lastNoticeId`). 목록이 비어 있으면 null */
    val nextCursor: Long?
        get() = notices.lastOrNull()?.noticeId
}
