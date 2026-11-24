package com.teumteumeat.teumteumeat.data.repository.notice

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.notice.NoticeApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_response.auth.ReissueTokenResponse
import com.teumteumeat.teumteumeat.data.network.model_response.notice.NoticeDetailResponse
import com.teumteumeat.teumteumeat.data.network.model_response.notice.NoticeListResponse
import com.teumteumeat.teumteumeat.data.network.model_response.notice.NoticeResponse
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * [NoticeRepositoryImpl] 단위 테스트.
 *
 * API 서비스만 모킹하고 [BaseRepository.safeApiVer2]는 실제 코드를 사용한다.
 * "서버 응답/예외 → [ApiResultV2] 5가지 결과"로 정확히 변환되는지를 검증한다.
 */
class NoticeRepositoryImplTest {

    private val noticeApiService = mockk<NoticeApiService>()
    private val authApiService = mockk<AuthApiService>()
    private val tokenLocalDataSource = mockk<TokenLocalDataSource>(relaxed = true)
    private lateinit var repository: NoticeRepositoryImpl

    @Before
    fun setUp() {
        repository = NoticeRepositoryImpl(noticeApiService, authApiService, tokenLocalDataSource)
    }

    // ── 응답 조립 헬퍼 ───────────────────────────────────────────────────────

    private fun okResponse(data: NoticeListResponse?) =
        ApiResponse<NoticeListResponse?, Any?>(
            code = "OK",
            message = "요청이 성공적으로 처리되었습니다.",
            details = null,
            data = data
        )

    private val sampleData = NoticeListResponse(
        notices = listOf(
            NoticeResponse(
                noticeId = 2L,
                title = "리그 오픈",
                createdDate = "2026-10-02T09:30:00",
                detail = NoticeDetailResponse(
                    fullScreenImageUrl = "https://cdn.example.com/2.png",
                    externalLink = "https://example.com/event",
                    deepLink = null,
                    actionButtonText = "참여하기"
                )
            ),
            NoticeResponse(
                noticeId = 1L,
                title = "서비스 오픈",
                createdDate = "2026-10-01T12:00:00",
                detail = null
            )
        ),
        hasNext = true
    )

    private fun httpError(code: Int, body: String = ""): HttpException =
        HttpException(Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())))

    // ── 성공 ─────────────────────────────────────────────────────────────────

    @Test
    fun `성공 응답은 도메인 NoticePage로 변환된다`() = runTest {
        coEvery { noticeApiService.getNotices(null, 20) } returns okResponse(sampleData)

        val result = repository.getNotices(lastNoticeId = null, size = 20)

        assertTrue(result is ApiResultV2.Success)
        val page = (result as ApiResultV2.Success).data
        assertEquals(listOf(2L, 1L), page.notices.map { it.noticeId })
        assertEquals("리그 오픈", page.notices[0].title)
        assertEquals("https://example.com/event", page.notices[0].detail?.externalLink)
        assertTrue(page.hasNext)
        assertEquals(1L, page.nextCursor)
    }

    @Test
    fun `lastNoticeId와 size가 API 서비스에 그대로 전달된다`() = runTest {
        coEvery { noticeApiService.getNotices(any(), any()) } returns okResponse(sampleData)

        repository.getNotices(lastNoticeId = 42L, size = 30)

        coVerify(exactly = 1) { noticeApiService.getNotices(42L, 30) }
    }

    // ── 서버가 준 실패 ───────────────────────────────────────────────────────

    @Test
    fun `성공 코드인데 data가 null이면 UnknownError가 된다`() = runTest {
        coEvery { noticeApiService.getNotices(any(), any()) } returns okResponse(null)

        val result = repository.getNotices(null, 20)

        assertTrue(result is ApiResultV2.UnknownError)
        assertEquals("서버 응답값이 null 입니다.", (result as ApiResultV2.UnknownError).message)
    }

    @Test
    fun `성공 코드가 아닌 응답은 서버 code와 message를 담은 ServerError가 된다`() = runTest {
        coEvery { noticeApiService.getNotices(any(), any()) } returns
            ApiResponse<NoticeListResponse?, Any?>(
                code = "NOTICE-001",
                message = "공지를 조회할 수 없습니다.",
                details = null,
                data = null
            )

        val result = repository.getNotices(null, 20)

        assertTrue(result is ApiResultV2.ServerError)
        result as ApiResultV2.ServerError
        assertEquals("NOTICE-001", result.code)
        assertEquals("공지를 조회할 수 없습니다.", result.message)
        assertEquals(DomainError.Message("공지를 조회할 수 없습니다."), result.errorType)
    }

    @Test
    fun `HTTP 오류 본문에 서버 code가 있으면 그 code로 ServerError가 된다`() = runTest {
        coEvery { noticeApiService.getNotices(any(), any()) } throws
            httpError(500, """{"code":"COMMON-500","message":"서버 점검 중입니다."}""")

        val result = repository.getNotices(null, 20)

        assertTrue(result is ApiResultV2.ServerError)
        result as ApiResultV2.ServerError
        assertEquals("COMMON-500", result.code)
        assertEquals("서버 점검 중입니다.", result.message)
    }

    @Test
    fun `HTTP 오류인데 본문이 비어 있으면 HTTP 상태 코드로 ServerError가 된다`() = runTest {
        coEvery { noticeApiService.getNotices(any(), any()) } throws httpError(503)

        val result = repository.getNotices(null, 20)

        assertTrue(result is ApiResultV2.ServerError)
        assertEquals("503", (result as ApiResultV2.ServerError).code)
    }

    // ── 네트워크 ─────────────────────────────────────────────────────────────

    @Test
    fun `IOException은 NetworkError가 된다`() = runTest {
        coEvery { noticeApiService.getNotices(any(), any()) } throws IOException("연결 끊김")

        val result = repository.getNotices(null, 20)

        assertTrue(result is ApiResultV2.NetworkError)
        assertEquals("네트워크 연결을 확인해주세요.", (result as ApiResultV2.NetworkError).message)
    }

    @Test
    fun `타임아웃도 NetworkError가 된다`() = runTest {
        coEvery { noticeApiService.getNotices(any(), any()) } throws SocketTimeoutException("timeout")

        assertTrue(repository.getNotices(null, 20) is ApiResultV2.NetworkError)
    }

    @Test
    fun `예상하지 못한 예외는 UnknownError가 된다`() = runTest {
        coEvery { noticeApiService.getNotices(any(), any()) } throws IllegalStateException("boom")

        val result = repository.getNotices(null, 20)

        assertTrue(result is ApiResultV2.UnknownError)
        assertEquals("boom", (result as ApiResultV2.UnknownError).message)
    }

    // ── 인증 만료 ────────────────────────────────────────────────────────────

    @Test
    fun `401이고 refresh 토큰이 없으면 SessionExpired RETRY가 된다`() = runTest {
        every { tokenLocalDataSource.getRefreshToken() } returns null
        coEvery { noticeApiService.getNotices(any(), any()) } throws httpError(401)

        val result = repository.getNotices(null, 20)

        assertTrue(result is ApiResultV2.SessionExpired)
        assertEquals(
            BaseRepository.SessionErrorCode.RETRY,
            (result as ApiResultV2.SessionExpired).code
        )
    }

    @Test
    fun `401 후 토큰 재발급에 성공하면 같은 커서로 다시 요청해 성공한다`() = runTest {
        every { tokenLocalDataSource.getRefreshToken() } returns "refresh-token"
        coEvery { authApiService.reissueAccessTokenV2(any()) } returns
            ApiResponse(
                code = "OK",
                message = null,
                details = null,
                data = ReissueTokenResponse(accessToken = "new-access", refreshToken = "new-refresh")
            )
        coEvery { noticeApiService.getNotices(42L, 20) } throws httpError(401) andThen okResponse(sampleData)

        val result = repository.getNotices(lastNoticeId = 42L, size = 20)

        assertTrue(result is ApiResultV2.Success)
        coVerify(exactly = 2) { noticeApiService.getNotices(42L, 20) }
    }

    @Test
    fun `401 후 토큰 재발급에 실패하면 SessionExpired FAIL이 된다`() = runTest {
        every { tokenLocalDataSource.getRefreshToken() } returns "refresh-token"
        coEvery { authApiService.reissueAccessTokenV2(any()) } throws httpError(401)
        coEvery { noticeApiService.getNotices(any(), any()) } throws httpError(401)

        val result = repository.getNotices(null, 20)

        assertTrue(result is ApiResultV2.SessionExpired)
        assertEquals(
            BaseRepository.SessionErrorCode.FAIL,
            (result as ApiResultV2.SessionExpired).code
        )
    }
}
