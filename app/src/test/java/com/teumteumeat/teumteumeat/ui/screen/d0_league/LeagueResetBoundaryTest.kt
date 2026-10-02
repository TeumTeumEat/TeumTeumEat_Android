package com.teumteumeat.teumteumeat.ui.screen.d0_league

import com.teumteumeat.teumteumeat.data.repository.league.FakeLeagueRepositoryImpl
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.league.GetPendingLeagueResultUseCase
import com.teumteumeat.teumteumeat.domain.usecase.league.MarkLeagueResultSeenUseCase
import com.teumteumeat.teumteumeat.util.MainDispatcherRule
import com.teumteumeat.teumteumeat.util.SchedulerClock
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.repository.league.LeagueRepository
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import io.mockk.mockk
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

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private val sundayBeforeReset = LocalDateTime.of(2026, 10, 4, 23, 59, 0)

    private fun createViewModel(start: LocalDateTime, myRank: Int? = 3): LeagueViewModel {
        val clock = SchedulerClock(start, dispatcher.scheduler)
        val repository = FakeLeagueRepositoryImpl(clock, myRank)
        return LeagueViewModel(
            sessionManager = mockk<SessionManager>(relaxed = true),
            getPendingLeagueResult = GetPendingLeagueResultUseCase(repository),
            markLeagueResultSeen = MarkLeagueResultSeenUseCase(repository),
            clock = clock,
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
    fun `초기화 직전(10월 4일 일요일 23시 59분)에 진입하면 제목은 10월 1주차다`() {
        val viewModel = createViewModel(sundayBeforeReset)

        val modal = viewModel.uiState.value.resultModal
        assertNotNull(modal)
        assertEquals("10월 1주차", modal!!.weekLabel)
    }

    @Test
    fun `초기화 직후(10월 5일 월요일 00시 00분)에 진입하면 제목은 10월 2주차로 바뀐다`() {
        val viewModel = createViewModel(LocalDateTime.of(2026, 10, 5, 0, 0, 0))

        val modal = viewModel.uiState.value.resultModal
        assertNotNull(modal)
        assertEquals("10월 2주차", modal!!.weekLabel)
    }

    // ---- 리그가 초기화되지 않았으면 결과 모달이 실행되지 않는지 ----

    /** 서버가 아직 진행 중인 주(9/28~10/4)의 결과를 내려주는 상황을 흉내 내는 Repository. */
    private class CurrentWeekResultRepository : LeagueRepository {
        override suspend fun getPendingResult(): ApiResultV2<LeagueResult?> =
            ApiResultV2.Success(null, LeagueResult(weekStart = LocalDate.of(2026, 9, 28), rank = 1, snackCount = 12))

        override suspend fun markResultSeen(weekStart: LocalDate): ApiResultV2<Unit> = ApiResultV2.Success(null, Unit)
    }

    private fun createViewModelWithCurrentWeekResult(start: LocalDateTime): LeagueViewModel {
        val clock = SchedulerClock(start, dispatcher.scheduler)
        val repository = CurrentWeekResultRepository()
        return LeagueViewModel(
            sessionManager = mockk<SessionManager>(relaxed = true),
            getPendingLeagueResult = GetPendingLeagueResultUseCase(repository),
            markLeagueResultSeen = MarkLeagueResultSeenUseCase(repository),
            clock = clock,
        )
    }

    @Test
    fun `리그가 초기화되기 전(일요일 23시 59분)에는 진행 중인 주의 결과 모달이 뜨지 않는다`() {
        val viewModel = createViewModelWithCurrentWeekResult(sundayBeforeReset)

        assertNull(viewModel.uiState.value.resultModal)
    }

    @Test
    fun `리그가 초기화된 직후(월요일 00시 00분)에는 같은 결과로 모달이 뜬다`() {
        val viewModel = createViewModelWithCurrentWeekResult(LocalDateTime.of(2026, 10, 5, 0, 0, 0))

        assertNotNull(viewModel.uiState.value.resultModal)
    }

    @Test
    fun `같은 주 안에서는 닫은 뒤 다시 진입해도 뜨지 않고 초기화가 지나야 새 주차 모달이 뜬다`() {
        val clock = SchedulerClock(sundayBeforeReset, dispatcher.scheduler)
        val repository = FakeLeagueRepositoryImpl(clock, myRank = 3) // 공유 Repository: '확인함' 상태가 유지된다
        fun enter() = LeagueViewModel(
            sessionManager = mockk<SessionManager>(relaxed = true),
            getPendingLeagueResult = GetPendingLeagueResultUseCase(repository),
            markLeagueResultSeen = MarkLeagueResultSeenUseCase(repository),
            clock = clock,
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
}
