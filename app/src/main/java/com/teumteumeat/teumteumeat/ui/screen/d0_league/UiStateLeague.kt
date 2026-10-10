package com.teumteumeat.teumteumeat.ui.screen.d0_league

import com.teumteumeat.teumteumeat.domain.model.league.LeagueResultTier
import java.time.LocalDate

// 리그 랭킹 화면에는 상위 10위까지만 노출한다.
private const val MAX_DISPLAYED_RANK = 10

// 포디움(1~3위)을 제외한 리스트 영역(4~10위)의 행 수. 데이터가 부족하면 빈 행으로 채워 보여준다.
private const val REST_ROW_COUNT = MAX_DISPLAYED_RANK - 3

/**
 * 리그 랭킹에 표시되는 한 유저의 항목.
 * 순위(rank)는 동점자 처리(공동 순위) 시 다음 순위를 건너뛰는 방식(4, 5, 5, 7...)을 따른다.
 * [nickname]은 랭킹 리스트(포디움 + 4위 이하)라는 공개 영역에 노출되므로 서버가 마스킹해 내려준 값을 담는다.
 * (폭이 좁은 포디움 카드만 표시 직전에 같은 규칙의 [com.teumteumeat.teumteumeat.utils.toMaskedNickname]을 적용한다)
 * 풋터에 항상 표시할 본인 닉네임([myRanker]가 null이어도 보여줘야 함)은 [UiStateLeague.myNickname]을 사용한다.
 */
data class LeagueRankerUiModel(
    val rank: Int,
    val nickname: String,
    val snackCount: Int,
    val isMe: Boolean = false,
    // 아직 해당 순위에 유저가 등록되지 않은 자리 — 닉네임/스낵 수를 "-"로 표시한다.
    val isEmpty: Boolean = false,
) {
    val snackCountText: String
        get() = if (isEmpty) "-" else "$snackCount"

    companion object {
        fun empty(rank: Int) = LeagueRankerUiModel(rank = rank, nickname = "-", snackCount = 0, isEmpty = true)
    }
}

/**
 * 리그 종료 후 첫 진입 시 한 번 보여주는 결과 안내 모달의 표시 정보.
 * [weekStart]는 '확인함' 기록용 주차 식별자, [weekLabel]은 결과 주의 시작일로 만든 "9월 5주차" 형태의 제목용 라벨이다.
 */
data class LeagueResultModalUi(
    val tier: LeagueResultTier,
    val rank: Int,
    val snackCount: Int,
    val weekLabel: String,
    val weekStart: LocalDate,
)

data class UiStateLeague(
    val isLoading: Boolean = false,
    val leagueTitle: String = "주간 리그 OPEN!",

    // 상위 3명(포디움) + 4위 이하 랭킹 리스트 (원본 전체) — 화면에는 [podiumRankers]/[restRankers]로
    // 상위 10위까지만 노출된다.
    val rankers: List<LeagueRankerUiModel> = emptyList(),

    // 내 랭킹 카드에 항상 표시할 닉네임 — 이번 주 랭킹에 아직 등록되지 않아 [myRanker]가 null이어도 보여준다.
    val myNickname: String = "",
    val myRanker: LeagueRankerUiModel? = null,
    val myTodaySnackCount: Int = 0,

    // 리그 리셋까지 남은 시간(초)
    val resetRemainingSeconds: Long = 0L,

    // 리그 결과 안내 모달 — null 이면 노출하지 않는다.
    val resultModal: LeagueResultModalUi? = null,

    // 리그 소개 안내 모달 — 헤더의 안내(i) 버튼을 눌렀을 때만 true.
    val isGuideVisible: Boolean = false,

    val errorMessage: String = "",
) {
    // 10위 밖의 랭커는 화면에 노출하지 않는다. [rankers]는 원본 그대로 두고 표시용으로만 자른다.
    private val displayedRankers: List<LeagueRankerUiModel>
        get() = rankers.filter { it.rank <= MAX_DISPLAYED_RANK }

    val podiumRankers: List<LeagueRankerUiModel>
        get() = displayedRankers.take(3)

    val restRankers: List<LeagueRankerUiModel>
        get() = displayedRankers.drop(3)

    // 4~10위 리스트 — 등록된 유저가 부족한 순위는 빈 행(isEmpty)으로 채워 데이터가 들어올 자리를 보여준다.
    val restRowsWithPlaceholder: List<LeagueRankerUiModel>
        get() {
            val rest = restRankers
            val lastRank = rest.lastOrNull()?.rank ?: 3
            val padding = (1..(REST_ROW_COUNT - rest.size).coerceAtLeast(0))
                .map { lastRank + it }
                .filter { it <= MAX_DISPLAYED_RANK }
                .map { LeagueRankerUiModel.empty(it) }
            return rest + padding
        }

    // 주간 랭킹 초기화 직후처럼 1위가 아직 등록되지 않았는지 판단하는 기준 — 1등 자리에 미등록 카드를 보여준다.
    val hasFirstPlace: Boolean
        get() = displayedRankers.any { it.rank == 1 }
}
