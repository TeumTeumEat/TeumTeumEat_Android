package com.teumteumeat.teumteumeat.ui.screen.d0_league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResultTier
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.league.GetPendingLeagueResultUseCase
import com.teumteumeat.teumteumeat.domain.usecase.league.MarkLeagueResultSeenUseCase
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    private val getPending = mockk<GetPendingLeagueResultUseCase>()
    private val markSeen = mockk<MarkLeagueResultSeenUseCase>()

    private fun createViewModel() = LeagueViewModel(
        sessionManager = mockk<SessionManager>(relaxed = true),
        getPendingLeagueResult = getPending,
        markLeagueResultSeen = markSeen,
        clock = fixedClock,
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
        assertEquals("10월 1주차", modal.weekLabel) // 현재 일자(10/2) 기준
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
    fun `내 순위를 바꾸면 순위별 구간의 결과 모달이 채워진다`() {
        // 1위 금트로피, 2~10위 은트로피, 11위~ 순위권 밖 연출
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
}
