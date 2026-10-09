package com.teumteumeat.teumteumeat.data.api.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueRankingResponse
import retrofit2.http.GET

interface LeagueApiService {

    /** 이번 주(월 00:00 ~ 다음 주 월 00:00, KST) 스낵 수 기준 주간 리그 랭킹을 조회한다. */
    @GET("/api/v1/league")
    suspend fun getLeagueRanking(): ApiResponse<LeagueRankingResponse, Any?>
}
