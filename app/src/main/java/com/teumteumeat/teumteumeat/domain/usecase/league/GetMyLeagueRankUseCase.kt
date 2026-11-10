package com.teumteumeat.teumteumeat.domain.usecase.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.league.LeagueMe
import com.teumteumeat.teumteumeat.domain.repository.league.LeagueRepository
import javax.inject.Inject

/**
 * 이번 주 리그의 내 순위·닉네임·주간/오늘 스낵 수를 모두 조회한다(랭킹 목록 제외).
 * 리그 화면 밖(홈, FCM "순위가 따라잡혔어요" 알림 등)에서 쓰며, 어떤 값을 쓸지 정해지기 전이라 서버가 주는 값을 전부 돌려준다.
 * 화면 상태나 로그인 화면 이동은 처리하지 않고 결과만 돌려주므로 후속 처리는 호출부가 정한다.
 * 단, 401 을 받으면 토큰 재발급을 시도하고 실패하면(네트워크 오류 포함) 저장된 토큰이 삭제된다 — 백그라운드에서 호출할 때 유의할 것.
 */
class GetMyLeagueRankUseCase @Inject constructor(
    private val leagueRepository: LeagueRepository,
) {
    suspend operator fun invoke(): ApiResultV2<LeagueMe> = leagueRepository.getMyLeagueRank()
}
