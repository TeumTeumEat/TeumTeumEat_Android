package com.teumteumeat.teumteumeat.ui.screen.c4_notice_list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.teumteumeat.teumteumeat.TestApplication
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [NoticeListScreen] UI 테스트 (Robolectric, 에뮬레이터 불필요).
 *
 * ViewModel 없이 [UiStateNoticeList]만 주입해 "상태 → 화면" 매핑과 "사용자 입력 → 콜백" 연결을 검증한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class)
class NoticeListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val noticeClicks = mutableListOf<Long>()
    private var loadMoreCalls = 0
    private var retryCalls = 0

    private fun setScreen(uiState: UiStateNoticeList) {
        composeRule.setContent {
            TeumTeumEatTheme {
                NoticeListScreen(
                    uiState = uiState,
                    onBackClick = {},
                    onNoticeClick = { noticeClicks += it },
                    onLoadMore = { loadMoreCalls++ },
                    onRetryClick = { retryCalls++ },
                )
            }
        }
    }

    private fun items(count: Int) = (count downTo 1).map { id ->
        NoticeItemUiModel(
            noticeId = id.toLong(),
            title = "공지 제목 $id",
            dateText = "10월 ${id}일",
            isNew = false
        )
    }

    // ── 상태별 화면 ──────────────────────────────────────────────────────────

    @Test
    fun `첫 페이지 로딩 중에는 로딩 문구가 표시된다`() {
        setScreen(UiStateNoticeList(isInitialLoading = true))

        composeRule.onNodeWithText("소식 불러오는 중").assertIsDisplayed()
    }

    @Test
    fun `공지가 없으면 빈 화면 문구가 표시된다`() {
        setScreen(UiStateNoticeList(isInitialLoading = false, hasNext = false))

        composeRule.onNodeWithText("등록된 공지사항이 없어요").assertIsDisplayed()
    }

    @Test
    fun `첫 페이지 실패 시 서버 메시지와 재시도 버튼이 표시되고 누르면 재시도 콜백이 호출된다`() {
        setScreen(
            UiStateNoticeList(isInitialLoading = false, errorMessage = "점검 중입니다.")
        )

        composeRule.onNodeWithText("점검 중입니다.").assertIsDisplayed()
        composeRule.onNodeWithText("다시 시도하기").performClick()

        assertEquals(1, retryCalls)
    }

    @Test
    fun `목록이 있으면 제목과 날짜가 표시된다`() {
        setScreen(UiStateNoticeList(isInitialLoading = false, notices = items(3), hasNext = false))

        composeRule.onNodeWithText("공지 제목 3").assertIsDisplayed()
        composeRule.onNodeWithText("10월 3일").assertIsDisplayed()
        composeRule.onNodeWithText("공지 제목 1").assertIsDisplayed()
    }

    @Test
    fun `이미 불러온 목록이 있으면 에러 메시지가 있어도 목록을 유지해서 보여준다`() {
        setScreen(
            UiStateNoticeList(
                isInitialLoading = false,
                notices = items(2),
                errorMessage = "무시되어야 하는 메시지"
            )
        )

        composeRule.onNodeWithText("공지 제목 2").assertIsDisplayed()
    }

    // ── 사용자 입력 ──────────────────────────────────────────────────────────

    @Test
    fun `공지를 누르면 해당 공지 ID로 클릭 콜백이 호출된다`() {
        setScreen(UiStateNoticeList(isInitialLoading = false, notices = items(3), hasNext = false))

        composeRule.onNodeWithText("공지 제목 2").performClick()

        assertEquals(listOf(2L), noticeClicks)
    }

    // ── 무한 스크롤 트리거 ───────────────────────────────────────────────────

    @Test
    fun `목록이 화면보다 짧아 하단 근처이면 다음 페이지를 요청한다`() {
        setScreen(UiStateNoticeList(isInitialLoading = false, notices = items(3), hasNext = true))
        composeRule.waitForIdle()

        assertTrue("짧은 목록은 즉시 다음 페이지를 요청해야 한다", loadMoreCalls >= 1)
    }

    @Test
    fun `긴 목록은 하단에 도달하기 전에는 요청하지 않고 하단으로 스크롤하면 요청한다`() {
        setScreen(UiStateNoticeList(isInitialLoading = false, notices = items(50), hasNext = true))
        composeRule.waitForIdle()
        assertEquals("상단에서는 요청하지 않아야 한다", 0, loadMoreCalls)

        composeRule.onNode(hasScrollAction()).performScrollToIndex(49)
        composeRule.waitForIdle()

        assertTrue("하단 도달 시 다음 페이지를 요청해야 한다", loadMoreCalls >= 1)
    }
}
