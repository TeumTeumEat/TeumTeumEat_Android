package com.teumteumeat.teumteumeat.data.api.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueLatestResultResponse
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueMeDto
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueRankingResponse
import retrofit2.http.GET

interface LeagueApiService {

    /** 이번 주(월 00:00 ~ 다음 주 월 00:00, KST) 스낵 수 기준 주간 리그 랭킹을 조회한다. */
    @GET("/api/v1/league")
    suspend fun getLeagueRanking(): ApiResponse<LeagueRankingResponse, Any?>

    /**
     * 직전 주(지난주 월 00:00 ~ 이번 주 월 00:00, KST) 리그의 내 최종 순위를 조회한다. 결과 안내 모달에 쓴다.
     * 이름과 달리 '직전 주' 한정이다 — 그 이전 주에 활동하고 직전 주를 쉰 경우 `rank` 가 null 로 내려온다.
     */
    @GET("/api/v1/league/results/latest")
    suspend fun getLatestLeagueResult(): ApiResponse<LeagueLatestResultResponse, Any?>

    /**
     * 이번 주 리그에서 내 순위와 주간/오늘 스낵 수만 조회한다(랭킹 목록 없음).
     * 리그 화면 밖(홈, FCM 순위 알림 등)에서 내 순위만 필요할 때 쓴다. 순위 기준은 [getLeagueRanking] 과 같다.
     */
    @GET("/api/v1/league/me")
    suspend fun getMyLeagueRank(): ApiResponse<LeagueMeDto, Any?>
}
