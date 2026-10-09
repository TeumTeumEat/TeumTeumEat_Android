package com.teumteumeat.teumteumeat.domain.repository.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import java.time.LocalDate

interface LeagueRepository {

    /** 이번 주 주간 리그 랭킹(상위 10명 + 내 순위/스낵 수 + 리셋까지 남은 시간)을 조회한다. */
    suspend fun getLeagueRanking(): ApiResultV2<LeagueRanking>

    /**
     * 아직 확인하지 않은 가장 오래된 종료 리그 결과를 반환한다. 없으면 `Success(data = null)`.
     * 주차를 건너뛰고 진입한 경우에도 확인하지 않은 가장 오래된 주차의 결과가 먼저 내려오는 것을 전제로 한다.
     */
    suspend fun getPendingResult(): ApiResultV2<LeagueResult?>

    /** [weekStart] 주차의 결과를 확인했음을 기록해 다음 진입부터 다시 내려오지 않게 한다. */
    suspend fun markResultSeen(weekStart: LocalDate): ApiResultV2<Unit>
}
