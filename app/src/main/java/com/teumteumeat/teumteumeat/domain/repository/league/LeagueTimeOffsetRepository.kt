package com.teumteumeat.teumteumeat.domain.repository.league

import kotlinx.coroutines.flow.StateFlow

/**
 * [dev/local 전용] 리그 계산 기준 시각 오프셋(`X-League-Time-Offset`, 초)을 보관한다.
 * 리그 API 요청의 서버 계산 기준 시각에 더해져 리셋 직전/직후 상황을 테스트하는 데 쓰며, 운영에서는 서버가 무시한다.
 * 앱을 다시 시작하면 초기화되도록 메모리에만 둔다.
 */
interface LeagueTimeOffsetRepository {

    /** 현재 적용 중인 오프셋(초). null 이면 헤더를 보내지 않는다. */
    val offsetSeconds: StateFlow<Long?>

    /** 오프셋을 [seconds] 로 바꾼다. null 이면 해제한다. */
    fun setOffsetSeconds(seconds: Long?)
}
