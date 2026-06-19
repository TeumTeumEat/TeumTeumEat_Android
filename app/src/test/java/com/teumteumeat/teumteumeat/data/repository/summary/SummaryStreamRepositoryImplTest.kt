package com.teumteumeat.teumteumeat.data.repository.summary

import com.teumteumeat.teumteumeat.data.remote.sse.SseClient
import com.teumteumeat.teumteumeat.data.remote.sse.SseEvent as DataSseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseBusinessException
import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseHttpException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SummaryStreamRepositoryImplTest {

    private val mockSseClient = mockk<SseClient>()
    private lateinit var repository: SummaryStreamRepositoryImpl

    @Before
    fun setUp() {
        repository = SummaryStreamRepositoryImpl(mockSseClient)
    }

    private fun givenSseEvents(vararg events: DataSseEvent) {
        every { mockSseClient.connect(any()) } returns flowOf(*events)
    }

    // ── 1. 이벤트 타입별 매핑 ────────────────────────────────────────────────

    @Test
    fun `CONNECT 이벤트 수신 시 Connected 방출`() = runTest {
        givenSseEvents(
            DataSseEvent.Message(id = null, type = "CONNECT", data = "")
        )

        val results = repository.streamDailySummary(1L).toList()

        assertEquals(listOf(SseEvent.Connected), results)
    }

    @Test
    fun `message 이벤트 수신 시 Chunk 방출`() = runTest {
        givenSseEvents(
            DataSseEvent.Message(id = null, type = "message", data = "안녕하세요")
        )

        val results = repository.streamDailySummary(1L).toList()

        assertEquals(listOf(SseEvent.Chunk("안녕하세요")), results)
    }

    @Test
    fun `title 이벤트 수신 시 TitleReceived 방출 후 Flow 완료`() = runTest {
        givenSseEvents(
            DataSseEvent.Message(id = null, type = "CONNECT", data = ""),
            DataSseEvent.Message(id = null, type = "message", data = "요약 본문"),
            DataSseEvent.Message(id = null, type = "title",   data = "요약 제목"),
            // title 이후 이벤트는 수집되지 않아야 한다 (transformWhile 종료)
            DataSseEvent.Message(id = null, type = "message", data = "무시되어야 함")
        )

        val results = repository.streamDailySummary(1L).toList()

        assertEquals(
            listOf(
                SseEvent.Connected,
                SseEvent.Chunk("요약 본문"),
                SseEvent.TitleReceived("요약 제목")
            ),
            results
        )
    }

    // ── 2. 필터링 동작 ────────────────────────────────────────────────────────

    @Test
    fun `알 수 없는 event type 은 무시되어 방출되지 않음`() = runTest {
        givenSseEvents(
            DataSseEvent.Message(id = null, type = "unknown_event", data = "무시"),
            DataSseEvent.Message(id = null, type = "title",         data = "제목")
        )

        val results = repository.streamDailySummary(1L).toList()

        assertEquals(listOf(SseEvent.TitleReceived("제목")), results)
    }

    @Test
    fun `Opened 는 무시되고 title 수신 시 정상 완료`() = runTest {
        givenSseEvents(
            DataSseEvent.Opened,
            DataSseEvent.Message(id = null, type = "message", data = "청크"),
            DataSseEvent.Message(id = null, type = "title",   data = "제목"),
            DataSseEvent.Closed // title 이후 → transformWhile 종료로 미수집
        )

        val results = repository.streamDailySummary(1L).toList()

        assertEquals(
            listOf(SseEvent.Chunk("청크"), SseEvent.TitleReceived("제목")),
            results
        )
    }

    @Test
    fun `title 없이 Closed 수신 시 StreamError 방출`() = runTest {
        givenSseEvents(
            DataSseEvent.Message(id = null, type = "message", data = "청크"),
            DataSseEvent.Closed
        )

        val results = repository.streamDailySummary(1L).toList()

        assertEquals(2, results.size)
        assertEquals(SseEvent.Chunk("청크"), results[0])
        assertTrue(results[1] is SseEvent.StreamError)
    }

    @Test
    fun `Failure 이벤트 수신 시 StreamError 방출 후 Flow 완료`() = runTest {
        // SseClient가 재시도 소진 후 방출하는 종단 Failure → 즉시 StreamError로 종료
        givenSseEvents(
            DataSseEvent.Message(id = null, type = "CONNECT", data = ""),
            DataSseEvent.Failure(
                cause = SseHttpException(503),
                httpCode = 503,
                httpMessage = "Service Unavailable"
            ),
            DataSseEvent.Message(id = null, type = "title", data = "방출되면 안 됨")
        )

        val results = repository.streamDailySummary(1L).toList()

        assertEquals(2, results.size)
        assertEquals(SseEvent.Connected, results[0])
        assertTrue(results[1] is SseEvent.StreamError)
    }

    @Test
    fun `Failure 에 비즈니스 코드가 있으면 SseBusinessException 으로 방출`() = runTest {
        givenSseEvents(
            DataSseEvent.Failure(
                cause = SseHttpException(400, "GOAL-003", "목표 학습 횟수를 완료하였습니다."),
                httpCode = 400,
                httpMessage = "목표 학습 횟수를 완료하였습니다."
            )
        )

        val results = repository.streamDailySummary(1L).toList()

        val error = results.single() as SseEvent.StreamError
        val business = error.throwable as SseBusinessException
        assertEquals("GOAL-003", business.errorCode)
        assertEquals("목표 학습 횟수를 완료하였습니다.", business.message)
    }

    // ── 3. 오류 처리 ──────────────────────────────────────────────────────────

    @Test
    fun `SseClient 예외 전파 시 StreamError 방출 후 Flow 완료`() = runTest {
        val cause = RuntimeException("재연결 3회 소진")
        every { mockSseClient.connect(any()) } returns flow { throw cause }

        val results = repository.streamDailySummary(1L).toList()

        assertEquals(1, results.size)
        val error = results.first()
        assertTrue(error is SseEvent.StreamError)
        assertEquals("재연결 3회 소진", (error as SseEvent.StreamError).throwable.message)
    }

    @Test
    fun `StreamError 방출 후 추가 이벤트 없이 Flow 완료`() = runTest {
        every { mockSseClient.connect(any()) } returns flow {
            throw IllegalStateException("네트워크 오류")
        }

        val results = repository.streamDailySummary(1L).toList()

        assertEquals(1, results.size)
        assertTrue(results.first() is SseEvent.StreamError)
    }
}