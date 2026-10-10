package com.teumteumeat.teumteumeat.ui.screen.d0_league

import com.teumteumeat.teumteumeat.util.FakeLeagueRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.league.GetPendingLeagueResultUseCase
import com.teumteumeat.teumteumeat.domain.usecase.league.MarkLeagueResultSeenUseCase
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.util.MainDispatcherRule
import com.teumteumeat.teumteumeat.utils.ElapsedTimeSource
import com.teumteumeat.teumteumeat.util.SchedulerClock
import com.teumteumeat.teumteumeat.util.clockAwareRankingUseCase
import com.teumteumeat.teumteumeat.util.sampleLeagueRanking
import com.teumteumeat.teumteumeat.util.rankingUseCaseReturning
import com.teumteumeat.teumteumeat.domain.usecase.league.GetLeagueRankingUseCase
import io.mockk.coEvery
import org.junit.Assert.assertTrue
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import io.mockk.mockk
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * '일요일 23:59:00 → 월요일 00:00:00 리그 초기화' 시나리오 자동화 테스트.
 * 시계([SchedulerClock])가 코루틴 가상 시간과 함께 흐르므로 실제로 기다리지 않고 경계를 통과시킬 수 있다.
 * (runTest 를 쓰지 않는 이유: 카운트다운 무한 루프가 runTest 종료를 막기 때문 — LeagueViewModelTest 와 동일)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LeagueResetBoundaryTest {

    private val dispatcher = UnconfinedTestDispatcher()

    // 코루틴 가상 시간과 함께 흐르는 경과 시간 — 기기 시각(clock)과 별개로 서버가 준 남은 시간을 센다.
    private val elapsedTimeSource = ElapsedTimeSource { dispatcher.scheduler.currentTime }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private val sundayBeforeReset = LocalDateTime.of(2026, 10, 4, 23, 59, 0)

    private fun createViewModel(start: LocalDateTime, myRank: Int? = 3): LeagueViewModel {
        val clock = SchedulerClock(start, dispatcher.scheduler)
        val repository = FakeLeagueRepository(clock, myRank)
        return LeagueViewModel(
            sessionManager = mockk<SessionManager>(relaxed = true),
            getLeagueRanking = clockAwareRankingUseCase(clock),
            getPendingLeagueResult = GetPendingLeagueResultUseCase(repository),
            markLeagueResultSeen = MarkLeagueResultSeenUseCase(repository),
            clock = clock,
            elapsedTimeSource = elapsedTimeSource,
        )
    }

    @Test
    fun `일요일 23시 59분 00초에 진입하면 초기화까지 60초가 표시된다`() {
        val viewModel = createViewModel(sundayBeforeReset)

        assertEquals(60L, viewModel.uiState.value.resetRemainingSeconds)
    }

    @Test
    fun `카운트다운은 1초마다 줄어들고 59초 시점에 1초가 남는다`() {
        val viewModel = createViewModel(sundayBeforeReset)

        dispatcher.scheduler.advanceTimeBy(59_000)
        dispatcher.scheduler.runCurrent()

        assertEquals(1L, viewModel.uiState.value.resetRemainingSeconds)
    }

    @Test
    fun `60초가 지나 월요일 00시 00분 00초가 되면 다음 주 초기화로 카운트다운이 다시 시작된다`() {
        val viewModel = createViewModel(sundayBeforeReset)

        dispatcher.scheduler.advanceTimeBy(60_000)
        dispatcher.scheduler.runCurrent()

        assertEquals(7 * 24 * 3600L, viewModel.uiState.value.resetRemainingSeconds)
    }

    @Test
    fun `리셋 후 재조회가 계속 실패하면 기존 목록을 유지하고 기기 시각 기준 7일 카운트다운으로 폴백한다`() {
        val clock = SchedulerClock(sundayBeforeReset, dispatcher.scheduler)
        val repository = FakeLeagueRepository(clock, myRank = 3)
        var calls = 0
        val failingAfterReset = mockk<GetLeagueRankingUseCase>()
        coEvery { failingAfterReset() } answers {
            if (calls++ == 0) ApiResultV2.Success(null, sampleLeagueRanking(weekStart = LocalDate.of(2026, 9, 28), remainingSeconds = 60))
            else ApiResultV2.NetworkError("offline")
        }
        val viewModel = LeagueViewModel(
            sessionManager = mockk<SessionManager>(relaxed = true),
            getLeagueRanking = failingAfterReset,
            getPendingLeagueResult = GetPendingLeagueResultUseCase(repository),
            markLeagueResultSeen = MarkLeagueResultSeenUseCase(repository),
            clock = clock,
            elapsedTimeSource = elapsedTimeSource,
        )

        dispatcher.scheduler.advanceTimeBy(70_000) // 00:00:00 재조회 3회(5초 간격) 실패 → 00:00:10 폴백
        dispatcher.scheduler.runCurrent()

        assertEquals(3, viewModel.uiState.value.rankers.size) // 기존 목록 유지
        assertEquals(UiScreenState.Success, viewModel.screenState.value) // 에러 화면으로 바뀌지 않는다
        assertEquals(7 * 24 * 3600L - 10, viewModel.uiState.value.resetRemainingSeconds)
        assertEquals(4, calls) // 최초 1회 + 재조회 3회
    }

    @Test
    fun `리셋 직후 서버가 아직 이전 주를 내려주면 5초 뒤 다시 조회해 새 주로 교체한다`() {
        val clock = SchedulerClock(sundayBeforeReset, dispatcher.scheduler)
        val repository = FakeLeagueRepository(clock, myRank = 3)
        val oldWeek = sampleLeagueRanking(weekStart = LocalDate.of(2026, 9, 28), remainingSeconds = 60)
        val newWeek = sampleLeagueRanking(weekStart = LocalDate.of(2026, 10, 5), remainingSeconds = 7 * 24 * 3600L - 5, rankers = emptyList(), me = null)
        val responses = ArrayDeque(listOf(oldWeek, oldWeek, newWeek)) // 진입, 리셋 직후(아직 이전 주), 5초 뒤(새 주)
        val useCase = mockk<GetLeagueRankingUseCase>()
        coEvery { useCase() } answers { ApiResultV2.Success(null, responses.removeFirst()) }
        val viewModel = LeagueViewModel(
            sessionManager = mockk<SessionManager>(relaxed = true),
            getLeagueRanking = useCase,
            getPendingLeagueResult = GetPendingLeagueResultUseCase(repository),
            markLeagueResultSeen = MarkLeagueResultSeenUseCase(repository),
            clock = clock,
            elapsedTimeSource = elapsedTimeSource,
        )

        dispatcher.scheduler.advanceTimeBy(65_000)
        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.rankers.isEmpty()) // 새 주(1위 미등록) 상태로 교체됨
        assertEquals(7 * 24 * 3600L - 5, viewModel.uiState.value.resetRemainingSeconds)
    }

    @Test
    fun `기기 시각이 멈춰 있거나 바뀌어도 카운트다운은 서버가 준 남은 시간만큼 줄어든다`() {
        val fixedClock = java.time.Clock.fixed(
            sundayBeforeReset.toInstant(java.time.ZoneOffset.UTC), java.time.ZoneOffset.UTC,
        ) // 기기 시각은 흐르지 않는다
        val repository = FakeLeagueRepository(fixedClock, myRank = null)
        val viewModel = LeagueViewModel(
            sessionManager = mockk<SessionManager>(relaxed = true),
            getLeagueRanking = rankingUseCaseReturning(sampleLeagueRanking(remainingSeconds = 100)),
            getPendingLeagueResult = GetPendingLeagueResultUseCase(repository),
            markLeagueResultSeen = MarkLeagueResultSeenUseCase(repository),
            clock = fixedClock,
            elapsedTimeSource = elapsedTimeSource,
        )
        assertEquals(100L, viewModel.uiState.value.resetRemainingSeconds)

        dispatcher.scheduler.advanceTimeBy(40_000)
        dispatcher.scheduler.runCurrent()

        assertEquals(60L, viewModel.uiState.value.resetRemainingSeconds)
    }

    @Test
    fun `초기화 직전(10월 4일 일요일 23시 59분)에 진입하면 제목은 결과 주(9월 21일 시작) 기준 9월 4주차다`() {
        val viewModel = createViewModel(sundayBeforeReset)

        val modal = viewModel.uiState.value.resultModal
        assertNotNull(modal)
        assertEquals("9월 4주차", modal!!.weekLabel)
    }

    @Test
    fun `초기화 직후(10월 5일 월요일 00시 00분)에 진입하면 제목은 결과 주(9월 28일 시작) 기준 9월 5주차로 바뀐다`() {
        val viewModel = createViewModel(LocalDateTime.of(2026, 10, 5, 0, 0, 0))

        val modal = viewModel.uiState.value.resultModal
        assertNotNull(modal)
        assertEquals("9월 5주차", modal!!.weekLabel)
    }

    @Test
    fun `같은 주 안에서는 닫은 뒤 다시 진입해도 뜨지 않고 초기화가 지나야 새 주차 모달이 뜬다`() {
        val clock = SchedulerClock(sundayBeforeReset, dispatcher.scheduler)
        val repository = FakeLeagueRepository(clock, myRank = 3) // 공유 Repository: '확인함' 상태가 유지된다
        fun enter() = LeagueViewModel(
            sessionManager = mockk<SessionManager>(relaxed = true),
            getLeagueRanking = clockAwareRankingUseCase(clock),
            getPendingLeagueResult = GetPendingLeagueResultUseCase(repository),
            markLeagueResultSeen = MarkLeagueResultSeenUseCase(repository),
            clock = clock,
            elapsedTimeSource = elapsedTimeSource,
        )

        val first = enter()                       // 일요일 23:59:00 — 직전 주(9/21 주) 결과
        assertNotNull(first.uiState.value.resultModal)
        first.onResultModalDismiss()

        dispatcher.scheduler.advanceTimeBy(30_000) // 23:59:30 — 아직 초기화 전
        dispatcher.scheduler.runCurrent()
        assertNull(enter().uiState.value.resultModal)

        dispatcher.scheduler.advanceTimeBy(30_000) // 월요일 00:00:00 — 초기화 완료
        dispatcher.scheduler.runCurrent()
        assertNotNull(enter().uiState.value.resultModal) // 새로 끝난 주(9/28 주)의 결과
    }

    @Test
    fun `리그 화면을 켜 둔 채 리셋 시각을 넘기면 새로 끝난 주의 결과 모달이 뜬다`() {
        val viewModel = createViewModel(sundayBeforeReset)
        assertEquals("9월 4주차", viewModel.uiState.value.resultModal!!.weekLabel) // 진입 시 직전 주 결과
        viewModel.onResultModalDismiss()
        assertNull(viewModel.uiState.value.resultModal)

        dispatcher.scheduler.advanceTimeBy(60_000) // 월요일 00:00:00 — 새 주 랭킹 재조회 성공
        dispatcher.scheduler.runCurrent()

        val modal = viewModel.uiState.value.resultModal
        assertNotNull(modal)
        assertEquals("9월 5주차", modal!!.weekLabel)
        assertEquals(LocalDate.of(2026, 9, 28), modal.weekStart)
    }

    @Test
    fun `리셋을 넘겼지만 서버가 새 주를 내려주지 않으면 결과 모달을 띄우지 않는다`() {
        val clock = SchedulerClock(sundayBeforeReset, dispatcher.scheduler)
        val repository = FakeLeagueRepository(clock, myRank = 3)
        val staleServer = rankingUseCaseReturning(sampleLeagueRanking(weekStart = LocalDate.of(2026, 9, 28), remainingSeconds = 60))
        val viewModel = LeagueViewModel(
            sessionManager = mockk<SessionManager>(relaxed = true),
            getLeagueRanking = staleServer, // 리셋 후에도 계속 이전 주를 내려준다
            getPendingLeagueResult = GetPendingLeagueResultUseCase(repository),
            markLeagueResultSeen = MarkLeagueResultSeenUseCase(repository),
            clock = clock,
            elapsedTimeSource = elapsedTimeSource,
        )
        viewModel.onResultModalDismiss()

        dispatcher.scheduler.advanceTimeBy(70_000) // 재조회 3회 실패 → 폴백
        dispatcher.scheduler.runCurrent()

        assertNull(viewModel.uiState.value.resultModal)
    }

    @Test
    fun `리셋 후 재조회에서 서버가 이미 닫은 주의 결과를 또 내려줘도 모달을 다시 띄우지 않는다`() {
        val clock = SchedulerClock(sundayBeforeReset, dispatcher.scheduler)
        val repository = FakeLeagueRepository(clock, myRank = 3)
        // 확인 기록과 무관하게 항상 같은 주 결과를 내려주는 서버(저장 실패 등으로 '확인함'이 남지 않은 상황)
        val sameWeekResult = LeagueResult(weekStart = LocalDate.of(2026, 9, 21), rank = 3, snackCount = 10)
        val alwaysSameWeek = mockk<GetPendingLeagueResultUseCase>()
        coEvery { alwaysSameWeek() } returns ApiResultV2.Success(null, sameWeekResult)
        val viewModel = LeagueViewModel(
            sessionManager = mockk<SessionManager>(relaxed = true),
            getLeagueRanking = clockAwareRankingUseCase(clock),
            getPendingLeagueResult = alwaysSameWeek,
            markLeagueResultSeen = MarkLeagueResultSeenUseCase(repository),
            clock = clock,
            elapsedTimeSource = elapsedTimeSource,
        )
        assertNotNull(viewModel.uiState.value.resultModal)
        viewModel.onResultModalDismiss()

        dispatcher.scheduler.advanceTimeBy(60_000) // 리셋 → 새 주 재조회 성공 → 결과 재조회
        dispatcher.scheduler.runCurrent()

        assertNull(viewModel.uiState.value.resultModal) // 이번 세션에서 닫은 주는 다시 뜨지 않는다
    }
}
