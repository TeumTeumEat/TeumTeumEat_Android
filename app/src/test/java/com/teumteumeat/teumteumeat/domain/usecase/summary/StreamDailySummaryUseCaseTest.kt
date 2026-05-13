package com.teumteumeat.teumteumeat.domain.usecase.summary

import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseHttpException
import com.teumteumeat.teumteumeat.domain.repository.summary.SummaryStreamRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class StreamDailySummaryUseCaseTest {

    private val mockRepository = mockk<SummaryStreamRepository>()
    private lateinit var useCase: StreamDailySummaryUseCase

    @Before
    fun setUp() {
        useCase = StreamDailySummaryUseCase(mockRepository)
    }

    // ── 1. 정상 이벤트 pass-through ────────────────────────────────────────────

    @Test
    fun `정상 이벤트는 변환 없이 그대로 방출`() = runTest {
        every { mockRepository.streamDailySummary(1L) } returns flowOf(
            SseEvent.Connected,
            SseEvent.Chunk("청크"),
            SseEvent.TitleReceived("제목")
        )

        val results = useCase(1L).toList()

        assertEquals(
            listOf(SseEvent.Connected, SseEvent.Chunk("청크"), SseEvent.TitleReceived("제목")),
            results
        )
    }

    // ── 2. HTTP 상태 코드 매핑 ─────────────────────────────────────────────────

    @Test
    fun `StreamError(SseHttpException 400) → 잘못된 요청입니다`() = runTest {
        every { mockRepository.streamDailySummary(1L) } returns flowOf(
            SseEvent.StreamError(SseHttpException(400))
        )

        val results = useCase(1L).toList()

        val error = results.single() as SseEvent.StreamError
        assertEquals("잘못된 요청입니다.", error.throwable.message)
    }

    @Test
    fun `StreamError(SseHttpException 401) → 인증이 필요합니다`() = runTest {
        every { mockRepository.streamDailySummary(1L) } returns flowOf(
            SseEvent.StreamError(SseHttpException(401))
        )

        val results = useCase(1L).toList()

        val error = results.single() as SseEvent.StreamError
        assertEquals("인증이 필요합니다.", error.throwable.message)
    }

    @Test
    fun `StreamError(SseHttpException 403) → 접근 권한이 없습니다`() = runTest {
        every { mockRepository.streamDailySummary(1L) } returns flowOf(
            SseEvent.StreamError(SseHttpException(403))
        )

        val results = useCase(1L).toList()

        val error = results.single() as SseEvent.StreamError
        assertEquals("접근 권한이 없습니다.", error.throwable.message)
    }

    @Test
    fun `StreamError(SseHttpException 404) → 카테고리를 찾을 수 없습니다`() = runTest {
        every { mockRepository.streamDailySummary(1L) } returns flowOf(
            SseEvent.StreamError(SseHttpException(404))
        )

        val results = useCase(1L).toList()

        val error = results.single() as SseEvent.StreamError
        assertEquals("카테고리를 찾을 수 없습니다.", error.throwable.message)
    }

    @Test
    fun `StreamError(SseHttpException 500) → 알 수 없는 오류가 발생했습니다`() = runTest {
        every { mockRepository.streamDailySummary(1L) } returns flowOf(
            SseEvent.StreamError(SseHttpException(500))
        )

        val results = useCase(1L).toList()

        val error = results.single() as SseEvent.StreamError
        assertEquals("알 수 없는 오류가 발생했습니다.", error.throwable.message)
    }

    @Test
    fun `StreamError(IOException) → 네트워크 연결을 확인해주세요`() = runTest {
        every { mockRepository.streamDailySummary(1L) } returns flowOf(
            SseEvent.StreamError(IOException("연결 타임아웃"))
        )

        val results = useCase(1L).toList()

        val error = results.single() as SseEvent.StreamError
        assertEquals("네트워크 연결을 확인해주세요.", error.throwable.message)
    }

    @Test
    fun `StreamError(RuntimeException) → 알 수 없는 오류가 발생했습니다`() = runTest {
        every { mockRepository.streamDailySummary(1L) } returns flowOf(
            SseEvent.StreamError(RuntimeException("예상치 못한 오류"))
        )

        val results = useCase(1L).toList()

        val error = results.single() as SseEvent.StreamError
        assertEquals("알 수 없는 오류가 발생했습니다.", error.throwable.message)
    }

    // ── 3. cause 보존 확인 ─────────────────────────────────────────────────────

    @Test
    fun `StreamError 변환 후 원본 cause 가 보존됨`() = runTest {
        val originalCause = SseHttpException(401)
        every { mockRepository.streamDailySummary(1L) } returns flowOf(
            SseEvent.StreamError(originalCause)
        )

        val results = useCase(1L).toList()

        val error = results.single() as SseEvent.StreamError
        assertEquals(originalCause, error.throwable.cause)
    }

    // ── 4. 복합 시나리오 ───────────────────────────────────────────────────────

    @Test
    fun `정상 이벤트 후 StreamError 수신 시 정상 이벤트는 그대로, StreamError는 변환됨`() = runTest {
        every { mockRepository.streamDailySummary(1L) } returns flowOf(
            SseEvent.Connected,
            SseEvent.Chunk("본문"),
            SseEvent.StreamError(SseHttpException(403))
        )

        val results = useCase(1L).toList()

        assertEquals(3, results.size)
        assertEquals(SseEvent.Connected, results[0])
        assertEquals(SseEvent.Chunk("본문"), results[1])
        val error = results[2] as SseEvent.StreamError
        assertTrue(error.throwable is Exception)
        assertEquals("접근 권한이 없습니다.", error.throwable.message)
    }
}