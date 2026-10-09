package com.teumteumeat.teumteumeat.data.repository.league

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.league.LeagueApiService
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
import javax.inject.Singleton

/**
 * 리그 Repository 실제 구현.
 * 주간 랭킹(GET /api/v1/league)만 서버와 연동돼 있고, 지난주 결과 조회·확인 기록은 아직 [fake]에 위임한다.
 * TODO: GET /api/v1/league/results/latest · /league/me 연동 시 위임을 제거하고 FakeLeagueRepositoryImpl 을 삭제할 것
 */
@Singleton
class LeagueRepositoryImpl @Inject constructor(
    private val leagueApiService: LeagueApiService,
    authApiService: AuthApiService,
    tokenLocalDataSource: TokenLocalDataSource,
    private val fake: FakeLeagueRepositoryImpl,
) : BaseRepository(authApiService, tokenLocalDataSource), LeagueRepository {

    override suspend fun getLeagueRanking(): ApiResultV2<LeagueRanking> =
        safeApiVer2(
            apiCall = { leagueApiService.getLeagueRanking() },
            mapper = { response -> response.requireNotNullOrError().toDomain() },
        )

    override suspend fun getPendingResult(): ApiResultV2<LeagueResult?> = fake.getPendingResult()

    override suspend fun markResultSeen(weekStart: LocalDate): ApiResultV2<Unit> = fake.markResultSeen(weekStart)
}
