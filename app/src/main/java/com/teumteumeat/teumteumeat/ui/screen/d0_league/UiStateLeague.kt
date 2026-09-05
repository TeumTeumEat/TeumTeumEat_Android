package com.teumteumeat.teumteumeat.ui.screen.d0_league

/**
 * 리그 랭킹에 표시되는 한 유저의 항목.
 * 순위(rank)는 동점자 처리(공동 순위) 시 다음 순위를 건너뛰는 방식(4, 5, 5, 7...)을 따른다.
 */
data class LeagueRankerUiModel(
    val rank: Int,
    val nickname: String,
    val snackCount: Int,
    val isMe: Boolean = false,
)

data class UiStateLeague(
    val isLoading: Boolean = false,
    val leagueTitle: String = "주간 리그 OPEN!",
    val maxSnackCount: Int = 77,

    // 상위 3명(포디움) + 4위 이하 랭킹 리스트
    val rankers: List<LeagueRankerUiModel> = emptyList(),

    val myRanker: LeagueRankerUiModel? = null,
    val myTodaySnackCount: Int = 0,

    // 리그 리셋까지 남은 시간(초)
    val resetRemainingSeconds: Long = 0L,

    val errorMessage: String = "",
) {
    val podiumRankers: List<LeagueRankerUiModel>
        get() = rankers.take(3)

    val restRankers: List<LeagueRankerUiModel>
        get() = rankers.drop(3)
}
