package com.teumteumeat.teumteumeat.domain.repository.notice

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.notice.NoticePage

interface NoticeRepository {

    /**
     * 공지 목록을 최신순으로 한 페이지 조회한다.
     *
     * @param lastNoticeId 직전 응답의 마지막 공지 ID. 첫 요청은 null
     * @param size 조회할 공지 수 (1 ~ 50)
     */
    suspend fun getNotices(
        lastNoticeId: Long?,
        size: Int
    ): ApiResultV2<NoticePage>
}
