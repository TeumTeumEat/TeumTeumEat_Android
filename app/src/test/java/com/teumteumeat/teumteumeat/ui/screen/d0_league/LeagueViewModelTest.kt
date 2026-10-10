package com.teumteumeat.teumteumeat.ui.screen.d0_league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.model.league.LeagueMe
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanker
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResultTier
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.league.GetPendingLeagueResultUseCase
import com.teumteumeat.teumteumeat.domain.usecase.league.MarkLeagueResultSeenUseCase
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.util.MainDispatcherRule
import com.teumteumeat.teumteumeat.util.rankingUseCaseReturning
import com.teumteumeat.teumteumeat.util.sampleLeagueRanking
import io.mockk.coEvery
import kotlinx.coroutines.CompletableDeferred
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate

/**
 * 리그 결과 안내 모달 노출/닫기 로직 테스트.
 * MainDispatcherRule 이 Unconfined 디스패처를 쓰므로 runTest 없이 동기적으로 검증한다
 * (LeagueViewModel 의 카운트다운 무한 루프가 runTest 종료를 막지 않도록).
 */
class LeagueViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 고정 시계(2026-10-02) 기준으로 이미 종료된 주(9/21~9/27)의 결과
    private val weekStart = LocalDate.of(2026, 9, 21)

    // 현재 일자 고정 — 2026-10-02(금)
    private val fixedClock: Clock = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("Asia/Seoul"))
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val getRanking = rankingUseCaseReturning()
    private val getPending = mockk<GetPendingLeagueResultUseCase>()
    private val markSeen = mockk<MarkLeagueResultSeenUseCase>()

    private fun createViewModel() = LeagueViewModel(
        sessionManager = sessionManager,
        getLeagueRanking = getRanking,
        getPendingLeagueResult = getPending,
        markLeagueResultSeen = markSeen,
        clock = fixedClock,
        elapsedTimeSource = { 0L },
    )

    private fun pending(rank: Int = 1) = ApiResultV2.Success<LeagueResult?>(
        message = null,
        data = LeagueResult(weekStart = weekStart, rank = rank, snackCount = 12),
    )

    @Test
    fun `확인하지 않은 결과가 있으면 진입 시 모달 상태가 채워진다`() {
        coEvery { getPending() } returns pending(rank = 1)

        val viewModel = createViewModel()

        val modal = viewModel.uiState.value.resultModal
        assertNotNull(modal)
        assertEquals(LeagueResultTier.FIRST, modal!!.tier)
        assertEquals(1, modal.rank)
        assertEquals(12, modal.snackCount)
        assertEquals("9월 4주차", modal.weekLabel) // 서버가 준 결과 주 시작일(9/21) 기준
        assertEquals(weekStart, modal.weekStart)
    }

    @Test
    fun `결과가 없으면 모달을 띄우지 않는다`() {
        coEvery { getPending() } returns ApiResultV2.Success<LeagueResult?>(message = null, data = null)

        val viewModel = createViewModel()

        assertNull(viewModel.uiState.value.resultModal)
        assertEquals(UiScreenState.Success, viewModel.screenState.value)
    }

    @Test
    fun `조회에 실패하면 모달 없이 기존 화면을 유지한다`() {
        coEvery { getPending() } returns ApiResultV2.NetworkError("네트워크 에러")

        val viewModel = createViewModel()

        assertNull(viewModel.uiState.value.resultModal)
        assertEquals(UiScreenState.Success, viewModel.screenState.value)
    }

    @Test
    fun `모달을 닫으면 상태가 해제되고 확인함이 한 번만 기록된다`() {
        coEvery { getPending() } returns pending()
        coEvery { markSeen(weekStart) } returns ApiResultV2.Success(message = null, data = Unit)
        val viewModel = createViewModel()

        viewModel.onResultModalDismiss()
        viewModel.onResultModalDismiss() // 중복 닫기

        assertNull(viewModel.uiState.value.resultModal)
        coVerify(exactly = 1) { markSeen(weekStart) }
    }

    @Test
    fun `확인함 기록이 실패해도 같은 세션에서 다시 띄우지 않는다`() {
        coEvery { getPending() } returns pending()
        coEvery { markSeen(weekStart) } returns ApiResultV2.NetworkError("네트워크 에러")
        val viewModel = createViewModel()

        viewModel.onResultModalDismiss()
        viewModel.loadLeague() // 재시도 등으로 다시 불러와도

        assertNull(viewModel.uiState.value.resultModal)
    }

    @Test
    fun `안내 모달은 진입 시 노출되지 않고 안내 버튼을 눌러야 열린다`() {
        coEvery { getPending() } returns ApiResultV2.Success<LeagueResult?>(message = null, data = null)

        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.isGuideVisible)

        viewModel.onGuideClick()
        assertTrue(viewModel.uiState.value.isGuideVisible)

        viewModel.onGuideDismiss()
        assertFalse(viewModel.uiState.value.isGuideVisible)
    }

    @Test
    fun `리그를 다시 불러와도 안내 모달은 자동으로 열리지 않는다`() {
        coEvery { getPending() } returns ApiResultV2.Success<LeagueResult?>(message = null, data = null)
        val viewModel = createViewModel()

        viewModel.loadLeague()

        assertFalse(viewModel.uiState.value.isGuideVisible)
    }

    @Test
    fun `내 순위를 바꾸면 순위별 구간의 결과 모달이 채워진다`() {
        // 1~10위 금트로피(1위 FIRST / 2~10위 SECOND), 11위~ 순위권 밖 연출
        val cases = (1..13).associateWith { rank ->
            when {
                rank == 1 -> LeagueResultTier.FIRST
                rank <= 10 -> LeagueResultTier.SECOND
                else -> LeagueResultTier.OUT_OF_RANK
            }
        }
        cases.forEach { (rank, expectedTier) ->
            clearMocks(getPending, markSeen)
            coEvery { getPending() } returns pending(rank = rank)

            val modal = createViewModel().uiState.value.resultModal

            assertNotNull("rank=$rank", modal)
            assertEquals("rank=$rank", rank, modal!!.rank)
            assertEquals("rank=$rank", expectedTier, modal.tier)
        }
    }
    // ---- 주간 랭킹 조회 (GET /api/v1/league) ----

    @Test
    fun `랭킹 조회에 성공하면 랭커 내 순위 오늘 스낵 남은 시간이 서버 값으로 채워진다`() {
        coEvery { getPending() } returns ApiResultV2.Success<LeagueResult?>(message = null, data = null)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(UiScreenState.Success, viewModel.screenState.value)
        assertEquals(listOf(1, 2, 3), state.rankers.map { it.rank })
        assertEquals(listOf("크", "병*", "이*"), state.rankers.map { it.nickname }) // 서버 마스킹 값 그대로(재마스킹 없음)
        assertEquals("병*", state.myNickname)
        assertEquals(2, state.myRanker!!.rank)
        assertEquals(10, state.myRanker!!.snackCount)
        assertEquals(1, state.myTodaySnackCount)
        assertEquals(295_953L, state.resetRemainingSeconds)
    }

    @Test
    fun `이번 주 스낵이 0개라 me rank 가 null 이면 내 랭킹은 미등록 상태다`() {
        coEvery { getPending() } returns ApiResultV2.Success<LeagueResult?>(message = null, data = null)
        coEvery { getRanking() } returns ApiResultV2.Success(
            message = null,
            data = sampleLeagueRanking(
                rankers = emptyList(),
                me = LeagueMe(nickname = "병*", weeklySnackCount = 0, todaySnackCount = 0, rank = null),
            ),
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNull(state.myRanker)
        assertEquals("병*", state.myNickname)
        assertEquals(0, state.myTodaySnackCount)
        assertFalse(state.hasFirstPlace)
    }

    @Test
    fun `존재하지 않는 유저(USER-001) 404 면 에러 화면과 서버 메시지를 보여준다`() {
        coEvery { getRanking() } returns ApiResultV2.ServerError(
            code = "USER-001",
            message = "존재하지 않는 유저입니다.",
            errorType = DomainError.None,
        )

        val viewModel = createViewModel()

        val screenState = viewModel.screenState.value
        assertTrue(screenState is UiScreenState.Error)
        assertEquals("존재하지 않는 유저입니다.", (screenState as UiScreenState.Error).message)
        assertTrue(viewModel.uiState.value.rankers.isEmpty())
        coVerify(exactly = 0) { getPending() } // 랭킹을 못 받았으면 결과 모달도 조회하지 않는다
    }

    @Test
    fun `네트워크 오류도 에러 화면이 되고 다시 시도하면 성공 화면으로 바뀐다`() {
        coEvery { getPending() } returns ApiResultV2.Success<LeagueResult?>(message = null, data = null)
        coEvery { getRanking() } returns ApiResultV2.NetworkError("네트워크 연결을 확인해주세요.")
        val viewModel = createViewModel()
        assertEquals(UiScreenState.Error("네트워크 연결을 확인해주세요."), viewModel.screenState.value)

        coEvery { getRanking() } returns ApiResultV2.Success(message = null, data = sampleLeagueRanking())
        viewModel.loadLeague()

        assertEquals(UiScreenState.Success, viewModel.screenState.value)
        assertEquals(3, viewModel.uiState.value.rankers.size)
    }

    @Test
    fun `세션이 만료되면 세션 만료 처리를 호출한다`() {
        coEvery { getRanking() } returns ApiResultV2.SessionExpired(
            code = BaseRepository.SessionErrorCode.FAIL,
            message = "로그인이 만료되었습니다.",
        )

        createViewModel()

        coVerify(exactly = 1) { sessionManager.expireSession() }
    }
    // ---- 이미 떠 있는 리그 화면으로 재진입(onNewIntent) ----

    @Test
    fun `재진입하면 로딩 화면 없이 최신 랭킹으로 갱신된다`() {
        coEvery { getPending() } returns ApiResultV2.Success<LeagueResult?>(message = null, data = null)
        val viewModel = createViewModel()
        assertEquals(3, viewModel.uiState.value.rankers.size)
        val updated = sampleLeagueRanking(
            rankers = listOf(LeagueRanker(rank = 1, nickname = "병*", snackCount = 3, isMe = true)),
            me = LeagueMe(nickname = "병*", weeklySnackCount = 3, todaySnackCount = 2, rank = 1),
        )
        coEvery { getRanking() } returns ApiResultV2.Success(message = null, data = updated)

        viewModel.onReenter()

        assertEquals(UiScreenState.Success, viewModel.screenState.value) // 로딩/에러로 바뀌지 않는다
        assertEquals(1, viewModel.uiState.value.rankers.size)
        assertEquals(1, viewModel.uiState.value.myRanker!!.rank)
        assertEquals(2, viewModel.uiState.value.myTodaySnackCount)
        coVerify(exactly = 2) { getRanking() } // 최초 진입 + 재진입
    }

    @Test
    fun `재진입 재조회가 실패하면 기존 화면을 그대로 유지한다`() {
        coEvery { getPending() } returns ApiResultV2.Success<LeagueResult?>(message = null, data = null)
        val viewModel = createViewModel()
        coEvery { getRanking() } returns ApiResultV2.NetworkError("offline")

        viewModel.onReenter()

        assertEquals(UiScreenState.Success, viewModel.screenState.value)
        assertEquals(3, viewModel.uiState.value.rankers.size)
    }

    @Test
    fun `에러 화면 상태에서 재진입하면 처음부터 다시 불러온다`() {
        coEvery { getPending() } returns ApiResultV2.Success<LeagueResult?>(message = null, data = null)
        coEvery { getRanking() } returns ApiResultV2.NetworkError("offline")
        val viewModel = createViewModel()
        assertTrue(viewModel.screenState.value is UiScreenState.Error)

        coEvery { getRanking() } returns ApiResultV2.Success(message = null, data = sampleLeagueRanking())
        viewModel.onReenter()

        assertEquals(UiScreenState.Success, viewModel.screenState.value)
    }

    @Test
    fun `초기 로딩 중에 재진입하면 중복 요청을 보내지 않는다`() {
        val gate = CompletableDeferred<ApiResultV2<LeagueRanking>>()
        coEvery { getRanking() } coAnswers { gate.await() } // 응답이 오지 않아 로딩 상태가 유지된다
        val viewModel = createViewModel()
        assertEquals(UiScreenState.Loading, viewModel.screenState.value)

        viewModel.onReenter()

        coVerify(exactly = 1) { getRanking() }
        assertEquals(UiScreenState.Loading, viewModel.screenState.value)
    }
}
