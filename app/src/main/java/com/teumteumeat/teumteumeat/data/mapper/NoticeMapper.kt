package com.teumteumeat.teumteumeat.data.mapper

import com.teumteumeat.teumteumeat.data.network.model_response.notice.NoticeDetailResponse
import com.teumteumeat.teumteumeat.data.network.model_response.notice.NoticeListResponse
import com.teumteumeat.teumteumeat.data.network.model_response.notice.NoticeResponse
import com.teumteumeat.teumteumeat.domain.model.notice.Notice
import com.teumteumeat.teumteumeat.domain.model.notice.NoticeDetail
import com.teumteumeat.teumteumeat.domain.model.notice.NoticePage
import java.time.LocalDateTime

fun NoticeListResponse.toDomain(): NoticePage =
    NoticePage(
        notices = notices.map { it.toDomain() },
        hasNext = hasNext
    )

fun NoticeResponse.toDomain(): Notice =
    Notice(
        noticeId = noticeId,
        title = title,
        // 날짜 하나가 깨져도 목록 전체가 실패하지 않도록 파싱 실패는 null로 처리한다.
        createdDate = runCatching { LocalDateTime.parse(createdDate) }.getOrNull(),
        detail = detail?.toDomain()
    )

fun NoticeDetailResponse.toDomain(): NoticeDetail =
    NoticeDetail(
        fullScreenImageUrl = fullScreenImageUrl.nullIfBlank(),
        externalLink = externalLink.nullIfBlank(),
        deepLink = deepLink.nullIfBlank(),
        actionButtonText = actionButtonText.nullIfBlank()
    )

private fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
