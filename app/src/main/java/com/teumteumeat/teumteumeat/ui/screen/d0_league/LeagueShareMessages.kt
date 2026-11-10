package com.teumteumeat.teumteumeat.ui.screen.d0_league

import com.teumteumeat.teumteumeat.domain.model.league.LeagueResultTier
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants

/** 리그 공유 시트에 채워 넣는 문구. 링크는 항상 문구 뒤 줄바꿈 다음, 맨 마지막 줄에 붙는다. */
object LeagueShareMessages {

    private const val GENERAL = "퀴즈로 공부도 하고 친구랑 경쟁도 할래? 지금 바로 틈틈잇을 사용해 봐!"
    private const val RESULT_FIRST = "이번 주 틈틈잇 간식 리그 1등 했어! 👑 나 이길 자신 있으면 지금 바로 도전해 봐!"
    private const val RESULT_TOP_10 = "이번 주 틈틈잇 간식 리그 TOP 10 들었어! 🍿 다음 주엔 같이 순위 올려 볼래?"
    private const val RESULT_OUT_OF_RANK = "틈틈이 퀴즈 풀면서 간식 모으는 중🍿 다음 주 리그는 나랑 같이 달려 볼래?"

    /** 리그 화면 우상단 공유 버튼 */
    fun general(): String = withLink(GENERAL)

    /** 결과 모달의 '공유하기' — [tier] 는 2~10위가 [LeagueResultTier.SECOND] */
    fun forResult(tier: LeagueResultTier): String = withLink(
        when (tier) {
            LeagueResultTier.FIRST -> RESULT_FIRST
            LeagueResultTier.SECOND -> RESULT_TOP_10
            LeagueResultTier.OUT_OF_RANK -> RESULT_OUT_OF_RANK
        }
    )

    private fun withLink(message: String): String = "$message\n${DeepLinkConstants.LEAGUE_SHARE_URL}"
}
