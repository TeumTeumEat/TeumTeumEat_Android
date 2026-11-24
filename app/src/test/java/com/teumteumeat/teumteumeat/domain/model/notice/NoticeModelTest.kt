package com.teumteumeat.teumteumeat.domain.model.notice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 도메인 모델이 가진 규칙(액션 버튼 노출 조건, 다음 페이지 커서) 단위 테스트.
 * 이 두 규칙은 UI·ViewModel이 그대로 신뢰하므로 모델 단에서 고정해 둔다.
 */
class NoticeModelTest {

    private fun detail(external: String?, deep: String?) = NoticeDetail(
        fullScreenImageUrl = "https://cdn.example.com/full.png",
        externalLink = external,
        deepLink = deep,
        actionButtonText = "자세히 보기"
    )

    private fun notice(id: Long) = Notice(
        noticeId = id,
        title = "공지 $id",
        createdDate = null,
        detail = null
    )

    // ── NoticeDetail.hasAction ───────────────────────────────────────────────

    @Test
    fun `externalLink와 deepLink가 모두 null이면 액션 버튼이 없다`() {
        assertFalse(detail(external = null, deep = null).hasAction)
    }

    @Test
    fun `externalLink만 있으면 액션 버튼이 있다`() {
        assertTrue(detail(external = "https://example.com", deep = null).hasAction)
    }

    @Test
    fun `deepLink만 있으면 액션 버튼이 있다`() {
        assertTrue(detail(external = null, deep = "https://admin.example.com/1").hasAction)
    }

    @Test
    fun `두 링크가 모두 있어도 액션 버튼이 있다`() {
        assertTrue(detail(external = "https://example.com", deep = "https://admin.example.com/1").hasAction)
    }

    // ── NoticePage.nextCursor ────────────────────────────────────────────────

    @Test
    fun `nextCursor는 목록의 마지막 공지 ID다`() {
        val page = NoticePage(
            notices = listOf(notice(42), notice(41), notice(40)),
            hasNext = true
        )

        assertEquals(40L, page.nextCursor)
    }

    @Test
    fun `목록이 비어 있으면 nextCursor는 null이다`() {
        assertNull(NoticePage(notices = emptyList(), hasNext = false).nextCursor)
    }
}
