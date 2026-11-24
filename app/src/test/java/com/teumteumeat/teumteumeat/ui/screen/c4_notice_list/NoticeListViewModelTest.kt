package com.teumteumeat.teumteumeat.ui.screen.c4_notice_list

import app.cash.turbine.test
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.model.notice.Notice
import com.teumteumeat.teumteumeat.domain.model.notice.NoticeDetail
import com.teumteumeat.teumteumeat.domain.model.notice.NoticePage
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.notice.GetNoticeListUseCase
import com.teumteumeat.teumteumeat.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * [NoticeListViewModel] 단위 테스트.
 *
 * UseCase·SessionManager를 모킹하고, 커서 페이징 상태 머신(첫 페이지 / 다음 페이지 / 실패 / 세션 만료)을 검증한다.
 * 비동기 결과는 [CompletableDeferred] 게이트로 멈춰 세워 "로딩 중" 상태를 결정적으로 관찰한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoticeListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getNoticeListUseCase: GetNoticeListUseCase
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        getNoticeListUseCase = mockk()
        sessionManager = mockk(relaxed = true)
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private fun notice(
        id: Long,
        createdDate: LocalDateTime? = LocalDateTime.of(2026, 10, 1, 12, 0),
        detail: NoticeDetail? = null
    ) = Notice(noticeId = id, title = "공지 $id", createdDate = createdDate, detail = detail)

    private fun page(vararg ids: Long, hasNext: Boolean) =
        NoticePage(notices = ids.map { notice(it) }, hasNext = hasNext)

    private fun success(page: NoticePage) = ApiResultV2.Success(message = null, data = page)

    private fun serverError(message: String = "서버 오류") = ApiResultV2.ServerError(
        code = "COMMON-500",
        message = message,
        errorType = DomainError.None
    )

    private fun sessionExpired() = ApiResultV2.SessionExpired(
        code = BaseRepository.SessionErrorCode.FAIL,
        message = "로그인이 만료되었습니다."
    )

    /** init에서 첫 페이지 요청이 실행되므로, 반드시 UseCase를 stub한 뒤 호출한다. */
    private fun createViewModel() = NoticeListViewModel(getNoticeListUseCase, sessionManager)

    // ── 1. 로딩 상태 ─────────────────────────────────────────────────────────

    @Test
    fun `진입 직후 첫 페이지 응답 전에는 로딩 상태다`() = runTest {
        val gate = CompletableDeferred<ApiResultV2<NoticePage>>()
        coEvery { getNoticeListUseCase(null, any()) } coAnswers { gate.await() }

        val viewModel = createViewModel()

        val loading = viewModel.uiState.value
        assertTrue(loading.isInitialLoading)
        assertTrue(loading.notices.isEmpty())
        assertNull(loading.errorMessage)

        gate.complete(success(page(2, 1, hasNext = false)))

        assertFalse(viewModel.uiState.value.isInitialLoading)
    }

    @Test
    fun `첫 페이지는 커서 없이 기본 크기 20으로 한 번만 요청한다`() = runTest {
        coEvery { getNoticeListUseCase(any(), any()) } returns success(page(1, hasNext = false))

        createViewModel()

        coVerify(exactly = 1) { getNoticeListUseCase(null, 20) }
    }

    // ── 2. 첫 페이지 성공 ────────────────────────────────────────────────────

    @Test
    fun `첫 페이지 성공 시 목록과 커서와 hasNext가 UiState에 반영된다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns success(page(3, 2, 1, hasNext = true))

        val state = createViewModel().uiState.value

        assertEquals(listOf(3L, 2L, 1L), state.notices.map { it.noticeId })
        assertEquals(1L, state.lastNoticeId)
        assertTrue(state.hasNext)
        assertFalse(state.isInitialLoading)
        assertFalse(state.isLoadingMore)
        assertNull(state.errorMessage)
    }

    @Test
    fun `공지는 제목과 M월 d일 형식의 날짜 텍스트로 변환되고 isNew는 false다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns success(
            NoticePage(
                notices = listOf(notice(1, createdDate = LocalDateTime.of(2026, 3, 5, 9, 0))),
                hasNext = false
            )
        )

        val item = createViewModel().uiState.value.notices.single()

        assertEquals("공지 1", item.title)
        assertEquals("3월 5일", item.dateText)
        assertFalse(item.isNew) // 읽음 상태 로컬 처리 전까지는 항상 false
    }

    @Test
    fun `createdDate가 없으면 날짜 텍스트는 빈 문자열이다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns success(
            NoticePage(notices = listOf(notice(1, createdDate = null)), hasNext = false)
        )

        assertEquals("", createViewModel().uiState.value.notices.single().dateText)
    }

    @Test
    fun `서버 detail은 UI 모델에 그대로 전달된다`() = runTest {
        val detail = NoticeDetail(
            fullScreenImageUrl = "https://cdn.example.com/full.png",
            externalLink = "https://example.com/event",
            deepLink = null,
            actionButtonText = "참여하기"
        )
        coEvery { getNoticeListUseCase(null, any()) } returns success(
            NoticePage(notices = listOf(notice(1, detail = detail)), hasNext = false)
        )

        assertEquals(detail, createViewModel().uiState.value.notices.single().detail)
    }

    @Test
    fun `등록된 공지가 없으면 빈 목록이고 더 불러오지 않는다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns
            success(NoticePage(notices = emptyList(), hasNext = false))

        val state = createViewModel().uiState.value

        assertTrue(state.notices.isEmpty())
        assertNull(state.lastNoticeId)
        assertFalse(state.hasNext)
        assertFalse(state.isInitialLoading)
        assertNull(state.errorMessage)
    }

    // ── 3. 첫 페이지 실패 / 세션 만료 / 재시도 ───────────────────────────────

    @Test
    fun `첫 페이지 서버 오류 시 에러 메시지가 표시되고 로딩이 끝난다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns serverError("점검 중입니다.")

        val state = createViewModel().uiState.value

        assertEquals("점검 중입니다.", state.errorMessage)
        assertTrue(state.notices.isEmpty())
        assertFalse(state.isInitialLoading)
    }

    @Test
    fun `첫 페이지 네트워크 오류 시 네트워크 메시지가 표시된다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns
            ApiResultV2.NetworkError("네트워크 연결을 확인해주세요.")

        assertEquals("네트워크 연결을 확인해주세요.", createViewModel().uiState.value.errorMessage)
    }

    @Test
    fun `첫 페이지에서 세션이 만료되면 세션 만료 처리를 호출하고 에러 화면은 띄우지 않는다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns sessionExpired()

        val viewModel = createViewModel()

        coVerify(exactly = 1) { sessionManager.expireSession() }
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `실패 후 재시도하면 에러가 사라지고 목록이 표시된다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returnsMany listOf(
            serverError(),
            success(page(2, 1, hasNext = false))
        )
        val viewModel = createViewModel()
        assertEquals("서버 오류", viewModel.uiState.value.errorMessage)

        viewModel.loadFirstPage()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertEquals(listOf(2L, 1L), state.notices.map { it.noticeId })
        assertFalse(state.isInitialLoading)
    }

    // ── 4. 다음 페이지 ───────────────────────────────────────────────────────

    @Test
    fun `다음 페이지는 마지막 공지 ID를 커서로 요청하고 목록 뒤에 이어 붙인다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns success(page(3, 2, hasNext = true))
        coEvery { getNoticeListUseCase(2L, any()) } returns success(page(1, hasNext = false))
        val viewModel = createViewModel()

        viewModel.loadNextPage()

        val state = viewModel.uiState.value
        assertEquals(listOf(3L, 2L, 1L), state.notices.map { it.noticeId })
        assertEquals(1L, state.lastNoticeId)
        assertFalse(state.hasNext)
        assertFalse(state.isLoadingMore)
        coVerify(exactly = 1) { getNoticeListUseCase(2L, 20) }
    }

    @Test
    fun `다음 페이지 로딩 중에는 isLoadingMore가 true였다가 끝나면 false가 된다`() = runTest {
        val gate = CompletableDeferred<ApiResultV2<NoticePage>>()
        coEvery { getNoticeListUseCase(null, any()) } returns success(page(2, hasNext = true))
        coEvery { getNoticeListUseCase(2L, any()) } coAnswers { gate.await() }
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // 첫 페이지가 반영된 현재 상태

            viewModel.loadNextPage()
            assertTrue(awaitItem().isLoadingMore)

            gate.complete(success(page(1, hasNext = false)))
            val done = awaitItem()
            assertFalse(done.isLoadingMore)
            assertEquals(listOf(2L, 1L), done.notices.map { it.noticeId })

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasNext가 false이면 다음 페이지를 요청하지 않는다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns success(page(2, 1, hasNext = false))
        val viewModel = createViewModel()

        viewModel.loadNextPage()

        coVerify(exactly = 1) { getNoticeListUseCase(any(), any()) }
    }

    @Test
    fun `다음 페이지 로딩 중 중복 호출은 무시된다`() = runTest {
        val gate = CompletableDeferred<ApiResultV2<NoticePage>>()
        coEvery { getNoticeListUseCase(null, any()) } returns success(page(2, hasNext = true))
        coEvery { getNoticeListUseCase(2L, any()) } coAnswers { gate.await() }
        val viewModel = createViewModel()

        viewModel.loadNextPage()
        viewModel.loadNextPage()
        viewModel.loadNextPage()
        gate.complete(success(page(1, hasNext = false)))

        coVerify(exactly = 1) { getNoticeListUseCase(2L, any()) }
        assertEquals(listOf(2L, 1L), viewModel.uiState.value.notices.map { it.noticeId })
    }

    @Test
    fun `첫 페이지 로딩 중에는 다음 페이지 요청이 무시된다`() = runTest {
        val gate = CompletableDeferred<ApiResultV2<NoticePage>>()
        coEvery { getNoticeListUseCase(null, any()) } coAnswers { gate.await() }
        val viewModel = createViewModel()

        viewModel.loadNextPage()

        coVerify(exactly = 1) { getNoticeListUseCase(any(), any()) }
        gate.complete(success(page(1, hasNext = false)))
    }

    @Test
    fun `첫 페이지가 실패해 커서가 없으면 다음 페이지 요청이 무시된다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns serverError()
        val viewModel = createViewModel()

        viewModel.loadNextPage()

        // hasNext 기본값이 true여도 커서가 없으면 잘못된 "첫 페이지 재요청"을 하지 않는다.
        coVerify(exactly = 1) { getNoticeListUseCase(any(), any()) }
    }

    @Test
    fun `다음 페이지 실패 시 기존 목록은 유지되고 에러 화면 없이 로딩만 해제된다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns success(page(3, 2, hasNext = true))
        coEvery { getNoticeListUseCase(2L, any()) } returns
            ApiResultV2.NetworkError("네트워크 연결을 확인해주세요.")
        val viewModel = createViewModel()

        viewModel.loadNextPage()

        val state = viewModel.uiState.value
        assertEquals(listOf(3L, 2L), state.notices.map { it.noticeId })
        assertEquals(2L, state.lastNoticeId) // 커서 유지 → 다시 스크롤하면 같은 페이지를 재시도
        assertTrue(state.hasNext)
        assertFalse(state.isLoadingMore)
        assertNull(state.errorMessage)
    }

    @Test
    fun `다음 페이지 실패 후 다시 요청하면 같은 커서로 재시도되어 성공한다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns success(page(3, 2, hasNext = true))
        coEvery { getNoticeListUseCase(2L, any()) } returnsMany listOf(
            serverError(),
            success(page(1, hasNext = false))
        )
        val viewModel = createViewModel()

        viewModel.loadNextPage() // 실패
        viewModel.loadNextPage() // 재시도

        assertEquals(listOf(3L, 2L, 1L), viewModel.uiState.value.notices.map { it.noticeId })
        coVerify(exactly = 2) { getNoticeListUseCase(2L, any()) }
    }

    @Test
    fun `hasNext가 true인데 빈 페이지가 오면 더 이상 요청하지 않도록 hasNext를 닫는다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns success(page(2, hasNext = true))
        coEvery { getNoticeListUseCase(2L, any()) } returns
            success(NoticePage(notices = emptyList(), hasNext = true))
        val viewModel = createViewModel()

        viewModel.loadNextPage()
        viewModel.loadNextPage() // 닫혔다면 무시되어야 한다

        val state = viewModel.uiState.value
        assertFalse(state.hasNext)
        assertEquals(2L, state.lastNoticeId)
        coVerify(exactly = 1) { getNoticeListUseCase(2L, any()) }
    }

    @Test
    fun `다음 페이지에서 세션이 만료되면 로딩을 해제하고 세션 만료 처리를 호출한다`() = runTest {
        coEvery { getNoticeListUseCase(null, any()) } returns success(page(2, hasNext = true))
        coEvery { getNoticeListUseCase(2L, any()) } returns sessionExpired()
        val viewModel = createViewModel()

        viewModel.loadNextPage()

        assertFalse(viewModel.uiState.value.isLoadingMore)
        coVerify(exactly = 1) { sessionManager.expireSession() }
    }
}
