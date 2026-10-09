package com.teumteumeat.teumteumeat.domain.repository.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import java.time.LocalDate

interface LeagueRepository {

    /** 이번 주 주간 리그 랭킹(상위 10명 + 내 순위/스낵 수 + 리셋까지 남은 시간)을 조회한다. */
    suspend fun getLeagueRanking(): ApiResultV2<LeagueRanking>

    /**
     * 결과 안내 모달에 보여줄 '직전 주' 리그의 내 최종 순위를 반환한다. 보여줄 결과가 없으면 `Success(data = null)`.
     *
     * 서버(GET /api/v1/league/results/latest)는 **직전 주(지난주 월 00:00 ~ 이번 주 월 00:00)만** 조회할 수 있다.
     * 따라서 아래 경우에는 결과가 없다(`null`).
     * - 직전 주에 스낵이 0개라 순위가 없는 경우. 예: 리그에 참여한 뒤 한 주를 쉬고 그다음 주에 진입하면,
     *   마지막으로 활동한 주의 순위는 조회할 수 없어 모달을 띄우지 않는다.
     * - 현재 계정이 그 주의 결과를 이미 확인한 경우(주당 1회 노출).
     */
    suspend fun getPendingResult(): ApiResultV2<LeagueResult?>

    /** [weekStart] 주차의 결과를 확인했음을 기록해 다음 진입부터 다시 내려오지 않게 한다. */
    suspend fun markResultSeen(weekStart: LocalDate): ApiResultV2<Unit>
}
