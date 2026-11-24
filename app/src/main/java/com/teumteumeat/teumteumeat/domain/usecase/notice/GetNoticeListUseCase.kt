package com.teumteumeat.teumteumeat.domain.usecase.notice

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.notice.NoticePage
import com.teumteumeat.teumteumeat.domain.repository.notice.NoticeRepository
import javax.inject.Inject

/**
 * 공지 목록을 커서 방식(`lastNoticeId`)으로 한 페이지 조회한다.
 *
 * 첫 페이지는 [lastNoticeId]를 null로, 다음 페이지는 직전 [NoticePage.nextCursor]를 넘긴다.
 * [NoticePage.hasNext]가 false이면 더 이상 요청하지 않는다.
 */
class GetNoticeListUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {

    suspend operator fun invoke(
        lastNoticeId: Long? = null,
        size: Int = DEFAULT_PAGE_SIZE
    ): ApiResultV2<NoticePage> =
        noticeRepository.getNotices(
            lastNoticeId = lastNoticeId,
            size = size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)
        )

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        private const val MIN_PAGE_SIZE = 1
        private const val MAX_PAGE_SIZE = 50
    }
}
