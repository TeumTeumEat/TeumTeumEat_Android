package com.teumteumeat.teumteumeat.data.network.interceptor

import com.teumteumeat.teumteumeat.util.FakeLeagueTimeOffsetRepository
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 리그 API 요청에만, 디버그 빌드이고 오프셋이 있을 때만 `X-League-Time-Offset` 이 실리는지 검증한다. */
class LeagueTimeOffsetInterceptorTest {

    private val response = mockk<Response>()

    /** [url] 요청을 인터셉터에 통과시키고, 다음 체인으로 넘어간 요청을 돌려준다. */
    private fun proceed(url: String, offset: Long?, isEnabled: Boolean = true): Request {
        val interceptor = LeagueTimeOffsetInterceptor(FakeLeagueTimeOffsetRepository(offset), isEnabled)
        val original = Request.Builder().url(url).build()
        var forwarded: Request? = null
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns original
        every { chain.proceed(any()) } answers {
            forwarded = firstArg()
            response
        }

        interceptor.intercept(chain)

        return requireNotNull(forwarded)
    }

    private fun Request.offsetHeader() = header(LeagueTimeOffsetInterceptor.HEADER_NAME)

    @Test
    fun `리그 3종 API 모두에 오프셋 헤더를 싣는다`() {
        listOf(
            "https://api-dev.example.com/api/v1/league",
            "https://api-dev.example.com/api/v1/league/me",
            "https://api-dev.example.com/api/v1/league/results/latest",
        ).forEach { url ->
            assertEquals(url, "90", proceed(url, offset = 90L).offsetHeader())
        }
    }

    @Test
    fun `음수 오프셋도 그대로 전달한다`() {
        assertEquals("-30", proceed("https://api-dev.example.com/api/v1/league", offset = -30L).offsetHeader())
    }

    @Test
    fun `오프셋이 없으면 헤더를 싣지 않는다`() {
        assertNull(proceed("https://api-dev.example.com/api/v1/league", offset = null).offsetHeader())
    }

    @Test
    fun `리그가 아닌 API 에는 헤더를 싣지 않는다`() {
        assertNull(proceed("https://api-dev.example.com/api/v1/users/me", offset = 90L).offsetHeader())
        assertNull(proceed("https://api-dev.example.com/api/v1/leagues-other", offset = 90L).offsetHeader())
    }

    @Test
    fun `릴리즈 빌드에서는 오프셋이 있어도 헤더를 싣지 않는다`() {
        assertNull(proceed("https://api.example.com/api/v1/league", offset = 90L, isEnabled = false).offsetHeader())
    }
}
