package com.teumteumeat.teumteumeat.data.repository.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import com.teumteumeat.teumteumeat.domain.model.league.lastFinishedLeagueWeekStart
import com.teumteumeat.teumteumeat.domain.repository.league.LeagueRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// TODO: 실기기 확인용 임시 값 — 내가 지난 주 리그에서 거둔 순위. 커밋/PR 전에 null 로 되돌리거나 실제 API 연동으로 교체할 것.
//
// ■ 이 값을 바꿔서 확인할 수 있는 케이스 (값을 바꾼 뒤 앱을 완전히 종료하고 리그 화면에 다시 진입)
//
//  null  → '결과 없음': 모달이 뜨지 않고 기존 리그 화면만 보인다.
//          (리그 참여 이력이 없거나, 이미 확인한 결과만 있는 상태를 흉내 낸다)
//  1     → 1위: 금트로피 로티 + "1위" + 간식왕 문구 (등장 후 1.5초 구간 반복)
//  2~10  → 2~10위: 은트로피 로티 + "n위" + 상위권 문구 (등장 후 1.5초 구간 반복)
//  11 이상 → 순위권 밖(11위~): 젤리 캐릭터 '아쉬움' 로티 (순위 "n위"는 표시하지 않음, 1회 재생 후 정지)
//
// ■ 모달이 다시 뜨는 조건 (시간/상태 관련)
//
//  - 같은 프로세스에서 모달을 닫으면 '확인함'이 기록되어 다시 진입해도 뜨지 않는다.
//  - 앱을 완전히 종료하면 '확인함'(메모리 저장)이 사라져 같은 결과가 다시 뜬다. (Fake 한정 — 실제 연동 시 서버/DataStore 로 대체)
//  - 기기 시각이 일요일 23:59 → 월요일 00:00 을 넘으면 '가장 최근에 끝난 주'가 바뀌어 새 주차 결과로 다시 뜬다.
//    (에뮬레이터에서 시간을 바꿔 확인 가능, 제목은 현재 일자 기준 월/주차)
//  - 아직 끝나지 않은 주(리셋 전)의 결과는 모달 대상이 아니다. (LeagueViewModel 의 isLeagueWeekFinished 가드)
private val FAKE_MY_RANK: Int? = null

/**
 * 서버 리그 결과 API 스펙이 확정되기 전까지 쓰는 Fake 구현.
 * '확인함' 상태는 프로세스 메모리에만 기록하므로 앱을 다시 실행하면 결과가 다시 내려온다.
 * TODO: 스펙 확정 시 BaseRepository.safeApiVer2 기반 LeagueRepositoryImpl 로 교체하고 RepositoryModule 바인딩 변경
 */
@Singleton
class FakeLeagueRepositoryImpl(
    private val clock: Clock,
    private val myRank: Int?,
) : LeagueRepository {

    // Hilt 주입용 — 실기기 확인용 임시 순위([FAKE_MY_RANK])를 사용한다. 테스트에서는 주 생성자로 순위를 직접 지정한다.
    @Inject
    constructor(clock: Clock) : this(clock, FAKE_MY_RANK)

    private val seenWeeks = mutableSetOf<LocalDate>()

    // 주간 랭킹은 LeagueRepositoryImpl 이 서버와 직접 연동한다. Fake 는 결과 모달 확인용으로만 쓴다.
    // TODO: GET /league/results/latest 연동 후 이 Fake 와 함께 삭제
    override suspend fun getLeagueRanking(): ApiResultV2<LeagueRanking> =
        ApiResultV2.UnknownError(message = "FakeLeagueRepositoryImpl 은 주간 랭킹 조회를 지원하지 않습니다.")

    override suspend fun getPendingResult(): ApiResultV2<LeagueResult?> {
        // 가장 최근에 종료된 리그 주(월요일 시작)의 결과 — 월요일 00:00 초기화 시점에 주차가 바뀐다.
        val lastWeekStart = lastFinishedLeagueWeekStart(LocalDate.now(clock))

        val result = myRank
            ?.let { rank -> LeagueResult(lastWeekStart, rank = rank, snackCount = (13 - rank).coerceAtLeast(1)) }
            ?.takeIf { it.weekStart !in seenWeeks }

        return ApiResultV2.Success(message = null, data = result)
    }

    override suspend fun markResultSeen(weekStart: LocalDate): ApiResultV2<Unit> {
        seenWeeks += weekStart
        return ApiResultV2.Success(message = null, data = Unit)
    }
}
