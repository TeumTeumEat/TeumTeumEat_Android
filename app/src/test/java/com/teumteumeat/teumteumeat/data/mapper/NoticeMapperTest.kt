package com.teumteumeat.teumteumeat.data.mapper

import com.teumteumeat.teumteumeat.data.network.model_response.notice.NoticeDetailResponse
import com.teumteumeat.teumteumeat.data.network.model_response.notice.NoticeListResponse
import com.teumteumeat.teumteumeat.data.network.model_response.notice.NoticeResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * 공지 DTO → 도메인 변환([NoticeMapper]) 단위 테스트.
 * 서버 응답이 불완전해도 목록 전체가 깨지지 않는다는 방어 동작을 검증한다.
 */
class NoticeMapperTest {

    private fun response(
        id: Long = 1L,
        createdDate: String = "2026-10-01T12:00:00",
        detail: NoticeDetailResponse? = null
    ) = NoticeResponse(
        noticeId = id,
        title = "서비스 신규 오픈 안내",
        createdDate = createdDate,
        detail = detail
    )

    // ── createdDate ──────────────────────────────────────────────────────────

    @Test
    fun `타임존 없는 ISO 날짜는 LocalDateTime으로 변환된다`() {
        val notice = response(createdDate = "2026-10-01T12:00:00").toDomain()

        assertEquals(LocalDateTime.of(2026, 10, 1, 12, 0, 0), notice.createdDate)
    }

    @Test
    fun `소수초가 포함된 날짜도 변환된다`() {
        val notice = response(createdDate = "2026-10-01T12:00:00.123").toDomain()

        assertEquals(LocalDateTime.of(2026, 10, 1, 12, 0, 0, 123_000_000), notice.createdDate)
    }

    @Test
    fun `해석할 수 없는 날짜는 예외 없이 null이 되고 나머지 필드는 유지된다`() {
        listOf("2026/10/01", "", "not-a-date", "2026-10-01").forEach { invalid ->
            val notice = response(id = 7L, createdDate = invalid).toDomain()

            assertNull("입력=$invalid", notice.createdDate)
            assertEquals(7L, notice.noticeId)
            assertEquals("서비스 신규 오픈 안내", notice.title)
        }
    }

    // ── detail ───────────────────────────────────────────────────────────────

    @Test
    fun `detail이 없으면 도메인 detail도 null이다`() {
        assertNull(response(detail = null).toDomain().detail)
    }

    @Test
    fun `detail 값이 모두 있으면 그대로 매핑된다`() {
        val notice = response(
            detail = NoticeDetailResponse(
                fullScreenImageUrl = "https://cdn.example.com/full.png",
                externalLink = "https://example.com/event",
                deepLink = "https://admin.example.com/notice/1",
                actionButtonText = "이벤트 참여하기"
            )
        ).toDomain()

        val detail = notice.detail
        assertNotNull(detail)
        assertEquals("https://cdn.example.com/full.png", detail!!.fullScreenImageUrl)
        assertEquals("https://example.com/event", detail.externalLink)
        assertEquals("https://admin.example.com/notice/1", detail.deepLink)
        assertEquals("이벤트 참여하기", detail.actionButtonText)
    }

    @Test
    fun `빈 문자열과 공백 문자열은 null로 정규화된다`() {
        val detail = response(
            detail = NoticeDetailResponse(
                fullScreenImageUrl = "",
                externalLink = "",
                deepLink = "   ",
                actionButtonText = ""
            )
        ).toDomain().detail

        assertNotNull(detail)
        assertNull(detail!!.fullScreenImageUrl)
        assertNull(detail.externalLink)
        assertNull(detail.deepLink)
        assertNull(detail.actionButtonText)
    }

    @Test
    fun `링크가 빈 문자열로 오면 액션 버튼이 노출되지 않는다`() {
        val detail = response(
            detail = NoticeDetailResponse(
                fullScreenImageUrl = "https://cdn.example.com/full.png",
                externalLink = "",
                deepLink = "",
                actionButtonText = "자세히 보기"
            )
        ).toDomain().detail

        assertFalse(detail!!.hasAction)
    }

    @Test
    fun `링크가 하나라도 있으면 액션 버튼이 노출된다`() {
        val detail = response(
            detail = NoticeDetailResponse(
                fullScreenImageUrl = null,
                externalLink = "https://example.com/event",
                deepLink = null,
                actionButtonText = "자세히 보기"
            )
        ).toDomain().detail

        assertTrue(detail!!.hasAction)
    }

    // ── 목록 ─────────────────────────────────────────────────────────────────

    @Test
    fun `목록 응답은 순서와 hasNext를 그대로 유지한다`() {
        val page = NoticeListResponse(
            notices = listOf(response(id = 3L), response(id = 2L), response(id = 1L)),
            hasNext = true
        ).toDomain()

        assertEquals(listOf(3L, 2L, 1L), page.notices.map { it.noticeId })
        assertTrue(page.hasNext)
    }

    @Test
    fun `빈 목록 응답도 변환된다`() {
        val page = NoticeListResponse(notices = emptyList(), hasNext = false).toDomain()

        assertTrue(page.notices.isEmpty())
        assertFalse(page.hasNext)
    }
}
