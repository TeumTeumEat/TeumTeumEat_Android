package com.teumteumeat.teumteumeat.ui.screen.c4_notice_list

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.notice.NoticeApiService
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.notice.NoticeRepositoryImpl
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.notice.GetNoticeListUseCase
import com.teumteumeat.teumteumeat.util.MainDispatcherRule
import com.teumteumeat.teumteumeat.util.NoticeJsonFixtures
import com.teumteumeat.teumteumeat.util.NoticeJsonFixtures.jsonResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.util.concurrent.TimeUnit

/**
 * 공지 목록 기능(Functional) 테스트.
 *
 * ViewModel → UseCase → Repository → Retrofit → (MockWebServer) 까지 **실제 구현을 그대로 연결**하고
 * 서버 응답만 흉내 낸다. 계층 사이의 연결(인자 전달, 예외→상태 변환, 커서 계산)이 실제 HTTP 요청으로
 * 이어지는지를 사용자 시나리오 단위로 검증한다. 각 계층의 세부 분기는 단위 테스트가 담당한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoticeListFunctionalTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var server: MockWebServer
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val tokenLocalDataSource = mockk<TokenLocalDataSource>(relaxed = true)

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    /** 서버 응답은 ViewModel 생성(=첫 요청 발생) 전에 enqueue해야 한다. */
    private fun createViewModel(): NoticeListViewModel {
        // - 자동 재시도를 끄는 이유: 연결 실패 시 OkHttp가 재시도하면 다음 enqueue 응답을 소비해 실패를 관찰할 수 없다.
        // - 연결 풀을 비우는 이유: 서버가 끊은 연결이 풀에 남아 다음 요청이 그 죽은 연결을 재사용하면
        //   (재시도도 꺼져 있으므로) 서버에 닿기도 전에 실패해 테스트가 비결정적이 된다.
        val client = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .callTimeout(5, TimeUnit.SECONDS)
            .build()
        // 호스트를 "localhost" 대신 서버가 실제로 바인딩한 IP로 고정한다.
        // "localhost"는 IPv6([::1])와 IPv4로 동시에 해석되는데, 한 번 실패한 경로는 뒤로 밀려
        // 자동 재시도가 꺼진 상태에서는 다음 요청이 서버가 열려 있지 않은 [::1]로 먼저 접속해 실패한다.
        val baseUrl = server.url("/").newBuilder()
            .host(InetAddress.getByName(server.hostName).hostAddress!!)
            .build()
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoticeApiService::class.java)
        val repository = NoticeRepositoryImpl(api, mockk<AuthApiService>(), tokenLocalDataSource)
        return NoticeListViewModel(GetNoticeListUseCase(repository), sessionManager)
    }

    /** 네트워크 I/O는 별도 스레드에서 끝나므로 조건이 만족될 때까지 실제 시간으로 기다린다. */
    private suspend fun NoticeListViewModel.awaitState(
        condition: (UiStateNoticeList) -> Boolean
    ): UiStateNoticeList = withTimeout(5_000) { uiState.first(condition) }

    // ── 1. 무한 스크롤 전체 흐름 ─────────────────────────────────────────────

    @Test
    fun `첫 페이지에서 마지막 페이지까지 커서로 이어 불러온다`() = runBlocking {
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(true, 5L, 4L, 3L)))
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false, 2L, 1L)))
        val viewModel = createViewModel()

        val first = viewModel.awaitState { !it.isInitialLoading }
        assertEquals(listOf(5L, 4L, 3L), first.notices.map { it.noticeId })
        assertTrue(first.hasNext)

        viewModel.loadNextPage()
        val last = viewModel.awaitState { it.notices.size == 5 && !it.isLoadingMore }
        assertEquals(listOf(5L, 4L, 3L, 2L, 1L), last.notices.map { it.noticeId })
        assertFalse(last.hasNext)

        // 실제로 서버에 나간 요청: 첫 요청은 커서 없음, 두 번째는 첫 페이지의 마지막 ID(3)
        val firstRequest = server.takeRequest(1, TimeUnit.SECONDS)!!.requestUrl!!
        assertNull(firstRequest.queryParameter("lastNoticeId"))
        assertEquals("20", firstRequest.queryParameter("size"))
        val secondRequest = server.takeRequest(1, TimeUnit.SECONDS)!!.requestUrl!!
        assertEquals("3", secondRequest.queryParameter("lastNoticeId"))
        assertEquals("20", secondRequest.queryParameter("size"))
    }

    @Test
    fun `마지막 페이지 이후에는 스크롤해도 서버에 추가 요청을 보내지 않는다`() = runBlocking {
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false, 2L, 1L)))
        val viewModel = createViewModel()
        viewModel.awaitState { !it.isInitialLoading }
        server.takeRequest(1, TimeUnit.SECONDS) // 첫 요청 소비

        repeat(3) { viewModel.loadNextPage() }

        assertNull(server.takeRequest(300, TimeUnit.MILLISECONDS))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `등록된 공지가 없으면 빈 목록이 표시된다`() = runBlocking {
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false)))
        val viewModel = createViewModel()

        val state = viewModel.awaitState { !it.isInitialLoading }

        assertTrue(state.notices.isEmpty())
        assertNull(state.errorMessage)
        assertFalse(state.hasNext)
    }

    @Test
    fun `서버 응답 값이 일부 불완전해도 목록은 표시된다`() = runBlocking {
        server.enqueue(
            jsonResponse(
                NoticeJsonFixtures.pageOf(
                    hasNext = false,
                    notices = listOf(
                        NoticeJsonFixtures.notice(2L, createdDate = "잘못된 날짜"),
                        """{"noticeId":1,"title":"정상 공지","createdDate":"2026-03-05T09:00:00",
                           "detail":{"fullScreenImageUrl":"","externalLink":"","deepLink":"","actionButtonText":""}}"""
                    )
                )
            )
        )
        val viewModel = createViewModel()

        val state = viewModel.awaitState { !it.isInitialLoading }

        assertEquals(listOf(2L, 1L), state.notices.map { it.noticeId })
        assertEquals("", state.notices[0].dateText)          // 날짜 파싱 실패 → 빈 텍스트
        assertEquals("3월 5일", state.notices[1].dateText)
        assertNull(state.notices[1].detail?.externalLink)     // 빈 문자열 → null 정규화
    }

    // ── 2. 실패와 복구 ───────────────────────────────────────────────────────

    @Test
    fun `서버 오류 응답은 서버가 준 메시지로 에러 상태가 되고 재시도로 복구된다`() = runBlocking {
        server.enqueue(
            jsonResponse("""{"code":"COMMON-500","message":"점검 중입니다."}""", httpCode = 500)
        )
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false, 2L, 1L)))
        val viewModel = createViewModel()

        val failed = viewModel.awaitState { !it.isInitialLoading }
        assertEquals("점검 중입니다.", failed.errorMessage)
        assertTrue(failed.notices.isEmpty())

        viewModel.loadFirstPage()

        val recovered = viewModel.awaitState { !it.isInitialLoading }
        assertNull(recovered.errorMessage)
        assertEquals(listOf(2L, 1L), recovered.notices.map { it.noticeId })
    }

    @Test
    fun `연결이 끊기면 네트워크 오류 메시지가 표시되고 재시도로 복구된다`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false, 1L)))
        val viewModel = createViewModel()

        val failed = viewModel.awaitState { !it.isInitialLoading }
        assertEquals("네트워크 연결을 확인해주세요.", failed.errorMessage)

        viewModel.loadFirstPage()

        val recovered = viewModel.awaitState { !it.isInitialLoading }
        assertNull(recovered.errorMessage)
        assertEquals(listOf(1L), recovered.notices.map { it.noticeId })
    }

    @Test
    fun `다음 페이지 요청이 실패해도 기존 목록은 유지되고 다시 스크롤하면 같은 커서로 재시도된다`() = runBlocking {
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(true, 3L, 2L)))
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false, 1L)))
        val viewModel = createViewModel()
        viewModel.awaitState { !it.isInitialLoading }

        viewModel.loadNextPage() // 실패
        val afterFailure = viewModel.awaitState { !it.isLoadingMore && it.notices.size == 2 }
        assertNull(afterFailure.errorMessage)
        assertTrue(afterFailure.hasNext)

        viewModel.loadNextPage() // 재시도
        val recovered = viewModel.awaitState { it.notices.size == 3 && !it.isLoadingMore }
        assertEquals(listOf(3L, 2L, 1L), recovered.notices.map { it.noticeId })

        server.takeRequest(1, TimeUnit.SECONDS) // 첫 페이지
        val failedRequest = server.takeRequest(1, TimeUnit.SECONDS)!!.requestUrl!!
        val retryRequest = server.takeRequest(1, TimeUnit.SECONDS)!!.requestUrl!!
        assertEquals("2", failedRequest.queryParameter("lastNoticeId"))
        assertEquals("2", retryRequest.queryParameter("lastNoticeId"))
    }

    @Test
    fun `토큰이 만료되어 재발급도 불가능하면 세션 만료 처리가 호출된다`() = runBlocking {
        every { tokenLocalDataSource.getRefreshToken() } returns null
        val expired = CompletableDeferred<Unit>()
        coEvery { sessionManager.expireSession() } coAnswers { expired.complete(Unit); Unit }
        server.enqueue(MockResponse().setResponseCode(401))

        val viewModel = createViewModel()

        withTimeout(5_000) { expired.await() }
        assertNull(viewModel.uiState.value.errorMessage) // 에러 화면 대신 로그인 화면으로 이동한다
    }
}
