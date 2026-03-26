/*
package com.teumteumeat.teumteumeat

import android.content.Context
import android.content.Intent
import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.goal.GoalCategory
import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import com.teumteumeat.teumteumeat.domain.quiz.UserQuizStatus
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.HomeViewModel
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE, application = TestApplication::class)
class HomeIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context

    // Mocks
    private val goalRepository = mockk<GoalRepository>(relaxed = true)
    private val quizRepository = mockk<QuizRepository>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        
        // Mock 데이터 설정: 리프래시 시그널은 빈 Flow로 설정하여 NPE 방지
        every { goalRepository.refreshSignal } returns MutableSharedFlow()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `날짜_변경_인텐트가_발생하면_홈화면_상태가_성공적으로_갱신되어야_한다`() = runTest {
        // 1. Given: 도메인 모델에 맞는 상세 목 데이터 생성
        val mockCategory = GoalCategory(
            categoryId = 101L,
            name = "안드로이드",
            path = "/it/android"
        )

        val mockGoal = UserGoal(
            goalId = 1L,
            type = DomainGoalType.CATEGORY,
            startDate = LocalDate.now().minusDays(7), // 7일 전 시작
            endDate = LocalDate.now().plusDays(7),    // 7일 후 종료
            isExpired = false,
            studyPeriod = "2주",
            difficulty = Difficulty.MEDIUM,
            fileName = null,
            documentId = null,
            prompt = "열심히 공부합시다",
            category = mockCategory,
            isCompleted = false,
        )

        val mockQuizStatus = UserQuizStatus(
            hasSolvedToday = false,  // 오늘 아직 안 풀었음
            isFirstTime = false,     // 기존 유저
            hasCreatedToday = true,
            isQuizGuideSeen = true,
            isCompleted = false,
            availableQuizCount = 0,
            targetQuizSetCount = 0,
            completedQuizSetCount = 0,
        )

        // Repository가 각각의 성공 응답을 반환하도록 설정
        coEvery { goalRepository.getUserGoal() } returns ApiResultV2.Success(data = mockGoal, message = "")
        coEvery { quizRepository.getUserQuizStatus() } returns ApiResultV2.Success(data = mockQuizStatus, message = "")

        val viewModel = HomeViewModel(
            goalRepository,
            quizRepository,
            mockk(relaxed = true),
            context,
            adManager = ,
            networkConnection = TODO(),
        )

        // 2. When: 시스템 날짜 변경 브로드캐스트 전송
        val intent = Intent(Intent.ACTION_DATE_CHANGED)
        context.sendBroadcast(intent)

        // 3. 비동기 작업 완료 대기
        shadowOf(Looper.getMainLooper()).idle() // Looper에 쌓인 브로드캐스트 처리
        advanceUntilIdle() // ViewModel 내부 코루틴(loadHomeState) 실행 완료

        // 4. Then: 최종 UI 상태 검증 (통합 테스트의 목적)
        // loadHomeState가 성공적으로 끝났다면 screenState가 Success여야 함
        assertEquals(UiScreenState.Success, viewModel.screenState.value)
        
        // Repository 호출 횟수 검증 (최초 init시 1번 + 날짜 변경시 1번 = 총 2번)
        coVerify(exactly = 2) { goalRepository.getUserGoal() }
    }
}*/
