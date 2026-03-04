package com.teumteumeat.teumteumeat


import android.content.Context
import android.content.Intent
import android.os.Looper
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.a4_main.AppResumeNotifier
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.HomeViewModel
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider
import com.teumteumeat.teumteumeat.utils.date_change_reciver.DateChangeReceiver
import io.mockk.every
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.robolectric.Shadows.shadowOf // shadowOf 임포트

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE, application = TestApplication::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Main 디스패처를 테스트용으로 교체
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // 테스트 종료 후 디스패처 복구
        Dispatchers.resetMain()
    }

    @Test
    fun `DATE_CHANGED 인텐트를 받으면 콜백 함수가 호출되어야 한다`() {
        // Given
        var isCalled = false
        val receiver = DateChangeReceiver { isCalled = true }
        val intent = Intent(Intent.ACTION_DATE_CHANGED)

        // When
        receiver.onReceive(mockk(), intent)

        // Then
        assertTrue(isCalled)
    }

    @Test
    fun `날짜 변경 트리거 함수가 실행되면 loadHomeState가 호출된다`() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val goalRepository = mockk<GoalRepository>(relaxed = true)
        every { goalRepository.refreshSignal } returns MutableSharedFlow()

        val viewModel = spyk(
            HomeViewModel(
                goalRepository,
                mockk(relaxed = true),
                mockk(relaxed = true),
                context
            )
        )

        // When: 시스템 인텐트 대신 함수를 직접 호출 (비즈니스 로직 단위 테스트)
        viewModel.onDateChangedTriggered()

        // Then
        verify { viewModel.loadHomeState() }
    }

}