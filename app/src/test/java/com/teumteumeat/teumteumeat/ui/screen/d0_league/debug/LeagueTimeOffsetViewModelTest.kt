package com.teumteumeat.teumteumeat.ui.screen.d0_league.debug

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.usecase.league.ClearLeagueResultSeenUseCase
import com.teumteumeat.teumteumeat.domain.usecase.league.ObserveLeagueTimeOffsetUseCase
import com.teumteumeat.teumteumeat.domain.usecase.league.SetLeagueTimeOffsetUseCase
import com.teumteumeat.teumteumeat.util.FakeLeagueTimeOffsetRepository
import com.teumteumeat.teumteumeat.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeagueTimeOffsetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeLeagueTimeOffsetRepository()
    private val clearSeen = mockk<ClearLeagueResultSeenUseCase>()

    private fun createViewModel() = LeagueTimeOffsetViewModel(
        observeLeagueTimeOffset = ObserveLeagueTimeOffsetUseCase(repository),
        setLeagueTimeOffset = SetLeagueTimeOffsetUseCase(repository),
        clearLeagueResultSeen = clearSeen,
    )

    @Test
    fun `리셋 3초 전 프리셋은 남은 시간에서 3초를 뺀 값을 오프셋으로 설정한다`() {
        val viewModel = createViewModel()

        viewModel.applyPresetBeforeReset(remainingSeconds = 100L)

        assertEquals(97L, repository.offsetSeconds.value)
    }

    @Test
    fun `프리셋은 이미 적용 중인 오프셋에 누적된다`() {
        repository.setOffsetSeconds(500L)
        val viewModel = createViewModel()

        // 오프셋 500초가 반영된 서버가 남은 시간 40초를 내려준 상황 → 37초를 더 당겨야 리셋 3초 전이다.
        viewModel.applyPresetBeforeReset(remainingSeconds = 40L)

        assertEquals(537L, repository.offsetSeconds.value)
    }

    @Test
    fun `프리셋을 적용하면 입력란에도 설정된 값이 채워진다`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() } // WhileSubscribed 구독

        viewModel.applyPresetBeforeReset(remainingSeconds = 100L)

        assertEquals("97", viewModel.uiState.value.input)
    }

    @Test
    fun `입력란의 숫자를 오프셋으로 적용한다`() {
        val viewModel = createViewModel()
        viewModel.onInputChange(" -30 ")

        assertTrue(viewModel.applyInput())

        assertEquals(-30L, repository.offsetSeconds.value)
    }

    @Test
    fun `숫자가 아닌 입력은 적용하지 않는다`() {
        val viewModel = createViewModel()
        viewModel.onInputChange("abc")

        assertFalse(viewModel.applyInput())

        assertNull(repository.offsetSeconds.value)
    }

    @Test
    fun `해제하면 오프셋과 입력란이 비워진다`() = runTest {
        repository.setOffsetSeconds(97L)
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() } // WhileSubscribed 구독
        viewModel.onInputChange("97")

        viewModel.clearOffset()

        assertNull(repository.offsetSeconds.value)
        assertEquals("", viewModel.uiState.value.input)
    }

    @Test
    fun `결과 확인 기록 초기화에 성공하면 true 로 알린다`() {
        coEvery { clearSeen() } returns ApiResultV2.Success(message = null, data = Unit)
        var result: Boolean? = null

        createViewModel().clearResultSeenRecord { result = it }

        assertEquals(true, result)
        coVerify(exactly = 1) { clearSeen() }
    }

    @Test
    fun `결과 확인 기록 초기화에 실패하면 false 로 알린다`() {
        coEvery { clearSeen() } returns ApiResultV2.UnknownError(message = "저장 실패")
        var result: Boolean? = null

        createViewModel().clearResultSeenRecord { result = it }

        assertEquals(false, result)
    }
}
