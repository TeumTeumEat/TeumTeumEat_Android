package com.teumteumeat.teumteumeat.data.network.interceptor

import com.teumteumeat.teumteumeat.domain.repository.league.LeagueTimeOffsetRepository
import okhttp3.Interceptor
import okhttp3.Response

/**
 * [dev/local 전용] 리그 API(`/api/v1/league` 및 하위 경로) 요청에 `X-League-Time-Offset`(초) 헤더를 싣는다.
 * 서버가 리그 계산 기준 시각에 이 값을 더해 리셋 직전/직후 결과를 테스트할 수 있다. 운영 서버는 이 헤더를 무시한다.
 *
 * [isEnabled] 가 false(릴리즈 빌드)이거나 설정된 오프셋이 없으면 요청을 그대로 통과시킨다.
 */
class LeagueTimeOffsetInterceptor(
    private val leagueTimeOffsetRepository: LeagueTimeOffsetRepository,
    private val isEnabled: Boolean,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val offsetSeconds = leagueTimeOffsetRepository.offsetSeconds.value

        if (!isEnabled || offsetSeconds == null || !request.url.encodedPath.isLeaguePath()) {
            return chain.proceed(request)
        }
        return chain.proceed(request.newBuilder().header(HEADER_NAME, offsetSeconds.toString()).build())
    }

    private fun String.isLeaguePath() = this == LEAGUE_PATH || startsWith("$LEAGUE_PATH/")

    companion object {
        const val HEADER_NAME = "X-League-Time-Offset"
        private const val LEAGUE_PATH = "/api/v1/league"
    }
}
