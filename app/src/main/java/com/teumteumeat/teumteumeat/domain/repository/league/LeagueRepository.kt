package com.teumteumeat.teumteumeat.domain.repository.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.league.LeagueMe
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import java.time.LocalDate

interface LeagueRepository {

    /** 이번 주 주간 리그 랭킹(상위 10명 + 내 순위/스낵 수 + 리셋까지 남은 시간)을 조회한다. */
    suspend fun getLeagueRanking(): ApiResultV2<LeagueRanking>

    /**
     * 이번 주 내 순위·닉네임·주간/오늘 스낵 수를 모두 조회한다(랭킹 목록 없음). 이번 주 스낵이 0개면 `rank` 는 null.
     * 리그 밖 화면/FCM 순위 알림에서 어떤 값을 쓸지 아직 정해지지 않아 서버가 주는 값을 줄이지 않고 전부 돌려준다.
     *
     * 화면 상태를 바꾸거나 로그인 화면으로 이동시키지 않고 결과만 돌려준다(세션 만료 시 이동 같은 후속 처리는 호출부가 정한다).
     * 단, 401 을 받으면 BaseRepository 가 토큰 재발급을 시도하고, 재발급/재시도에 실패하면(네트워크 오류 포함)
     * 저장된 토큰을 삭제한 뒤 `SessionExpired` 를 돌려준다 — 백그라운드(FCM 등)에서 호출할 때 유의할 것.
     */
    suspend fun getMyLeagueRank(): ApiResultV2<LeagueMe>

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
