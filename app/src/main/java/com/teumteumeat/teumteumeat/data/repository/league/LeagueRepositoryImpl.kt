package com.teumteumeat.teumteumeat.data.repository.league

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.league.LeagueApiService
import com.teumteumeat.teumteumeat.data.datastore.LeagueResultDataStore
import com.teumteumeat.teumteumeat.data.mapper.toDomain
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import com.teumteumeat.teumteumeat.domain.repository.league.LeagueRepository
import com.teumteumeat.teumteumeat.utils.Utils.RepositoryUtils.requireNotNullOrError
import java.time.LocalDate
import javax.inject.Inject

/**
 * 리그 Repository 실제 구현.
 * 주간 랭킹(GET /api/v1/league)과 직전 주 결과(GET /api/v1/league/results/latest)는 서버와 연동하고,
 * 결과 모달을 이미 확인했는지는 서버가 모르므로 [resultDataStore] 에 현재 로그인한 계정(토큰의 유저 id) 별로 기록한다.
 *
 * 주의: `/results/latest` 는 이름과 달리 '직전 주' 한정이라 그 이전에 활동한 주의 순위는 받을 수 없다.
 * TODO: 최근 활동 주의 순위를 조회하는 서버 API(Jira 카드 참고)가 생기면 [getPendingResult] 의 조회처를 교체한다.
 */
class LeagueRepositoryImpl @Inject constructor(
    private val leagueApiService: LeagueApiService,
    private val resultDataStore: LeagueResultDataStore,
    authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : BaseRepository(authApiService, tokenLocalDataSource), LeagueRepository {

    override suspend fun getLeagueRanking(): ApiResultV2<LeagueRanking> =
        safeApiVer2(
            apiCall = { leagueApiService.getLeagueRanking() },
            mapper = { response -> response.requireNotNullOrError().toDomain() },
        )

    /**
     * 서버가 내려준 '직전 주' 결과 중 아직 확인하지 않은 것만 돌려준다.
     * 직전 주에 랭킹에 참여한 경우(순위가 있음)에만 결과가 있고, 아래는 `Success(data = null)` 이다.
     * - 직전 주 스낵이 0개라 순위가 없음(한 주 이상 쉬고 진입한 경우 포함 — 더 이전 주의 순위는 서버에서 받을 수 없다)
     * - 현재 계정이 이미 확인한 주(마지막 확인 주 이하)
     */
    override suspend fun getPendingResult(): ApiResultV2<LeagueResult?> =
        when (val response = safeApiVer2(
            apiCall = { leagueApiService.getLatestLeagueResult() },
            mapper = { it.requireNotNullOrError().toDomain() },
        )) {
            is ApiResultV2.Success -> {
                val lastSeen = resultDataStore.getLastSeenWeekStart(tokenLocalDataSource.getUserId())
                val unseen = response.data?.takeIf { lastSeen == null || it.weekStart.isAfter(lastSeen) }
                ApiResultV2.Success(message = response.message, data = unseen)
            }

            else -> response
        }

    /** 현재 계정이 [weekStart] 결과를 확인했음을 기기에 기록한다. 저장에 실패하면 `UnknownError` 로 돌려준다. */
    override suspend fun markResultSeen(weekStart: LocalDate): ApiResultV2<Unit> =
        if (resultDataStore.markSeen(tokenLocalDataSource.getUserId(), weekStart)) {
            ApiResultV2.Success(message = null, data = Unit)
        } else {
            ApiResultV2.UnknownError(message = "리그 결과 확인 기록을 저장하지 못했습니다.")
        }
}
