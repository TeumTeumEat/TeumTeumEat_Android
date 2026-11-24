package com.teumteumeat.teumteumeat.data.api.notice

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_response.notice.NoticeListResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 공지사항 API.
 */
interface NoticeApiService {

    /**
     * ### 공지 목록 조회 (무한 스크롤, 최신순)
     * * 첫 요청은 [lastNoticeId] 없이 보내고, 다음 요청부터는 직전 응답의 마지막 공지 noticeId를 보낸다.
     * * 응답의 `hasNext`가 false이면 더 조회할 공지가 없다.
     *
     * @param lastNoticeId 직전 응답의 마지막 공지 ID. 첫 요청은 null (쿼리에서 생략됨)
     * @param size 조회할 공지 수 (1 ~ 50, 범위를 벗어나면 서버가 가까운 값으로 보정)
     */
    @GET("/api/v1/notices")
    suspend fun getNotices(
        @Query("lastNoticeId") lastNoticeId: Long?,
        @Query("size") size: Int
    ): ApiResponse<NoticeListResponse?, Any?>
}
