package com.teumteumeat.teumteumeat.data.repository.summary

import com.teumteumeat.teumteumeat.data.remote.sse.SseClient
import com.teumteumeat.teumteumeat.data.remote.sse.SseEvent as DataSseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
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
    fun `Opened 와 Closed 이벤트는 무시됨`() = runTest {
        givenSseEvents(
            DataSseEvent.Opened,
            DataSseEvent.Message(id = null, type = "message", data = "청크"),
            DataSseEvent.Closed
        )

        val results = repository.streamDailySummary(1L).toList()

        assertEquals(listOf(SseEvent.Chunk("청크")), results)
    }

    @Test
    fun `Failure 이벤트는 재시도 중 무시됨 — 최종 실패는 catch로 처리`() = runTest {
        givenSseEvents(
            DataSseEvent.Message(id = null, type = "CONNECT", data = ""),
            DataSseEvent.Failure(cause = null, httpCode = 503, httpMessage = "Service Unavailable"),
            DataSseEvent.Message(id = null, type = "title", data = "제목")
        )

        val results = repository.streamDailySummary(1L).toList()

        // Failure는 필터링되고 이후 이벤트는 정상 방출
        assertEquals(
            listOf(SseEvent.Connected, SseEvent.TitleReceived("제목")),
            results
        )
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