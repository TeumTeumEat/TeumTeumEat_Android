package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.teumteumeat.teumteumeat.TestApplication
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.document.PdfDocumentSummary
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.quiz.UserQuizStatus
import com.teumteumeat.teumteumeat.domain.repository.pff_document.PdfDocumentRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.document.StreamPdfSummaryUseCase
import com.teumteumeat.teumteumeat.domain.usecase.summary.StreamDailySummaryUseCase
import com.teumteumeat.teumteumeat.data.network.model_response.UserQuiz
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizType
import com.teumteumeat.teumteumeat.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PDF(DOCUMENT) 요약 SSE 완료 시 퀴즈 프리페치(quizRepository.getUserQuizzes) 호출을 검증한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE, application = TestApplication::class)
class SummaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var pdfDocumentRepository: PdfDocumentRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var quizRepository: QuizRepository
    private lateinit var streamDailySummaryUseCase: StreamDailySummaryUseCase
    private lateinit var streamPdfSummaryUseCase: StreamPdfSummaryUseCase
    private lateinit var viewModel: SummaryViewModel

    private val goalId = 10L
    private val documentId = 20L

    private val dummyQuizStatus = UserQuizStatus(
        hasSolvedToday = false,
        isFirstTime = true,
        hasCreatedToday = false,
        isQuizGuideSeen = false,
        availableQuizCount = 0,
        dailyAdRewardCount = 0,
        canIssueCoupon = false,
        targetQuizSetCount = 0,
        completedQuizSetCount = 0,
        isCompleted = false,
    )

    private val dummyPdfSummary = PdfDocumentSummary(
        fileName = "test.pdf",
        updatedAt = "2026-07-08T00:00:00",
        summary = "요약 내용",
        status = "completed",
    )

    private val dummyUserQuiz = UserQuiz(
        quizId = 1,
        question = "질문",
        options = listOf("A", "B"),
        type = QuizType.OX,
    )

    @Before
    fun setUp() {
        pdfDocumentRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        quizRepository = mockk(relaxed = true)
        streamDailySummaryUseCase = mockk(relaxed = true)
        streamPdfSummaryUseCase = mockk(relaxed = true)

        coEvery { quizRepository.getUserQuizStatus() } returns
                ApiResultV2.Success(message = null, data = dummyQuizStatus)
        coEvery { pdfDocumentRepository.getPdfDocumentSummary(any(), any()) } returns
                ApiResultV2.Success(message = null, data = dummyPdfSummary)
        every { streamPdfSummaryUseCase(any(), any()) } returns flowOf(
            SseEvent.Connected,
            SseEvent.Chunk("청크"),
            SseEvent.TitleReceived("제목"),
        )

        viewModel = SummaryViewModel(
            pdfDocumentRepository = pdfDocumentRepository,
            categoryRepository = categoryRepository,
            quizRepository = quizRepository,
            streamDailySummaryUseCase = streamDailySummaryUseCase,
            streamPdfSummaryUseCase = streamPdfSummaryUseCase,
            application = ApplicationProvider.getApplicationContext<Application>(),
            sessionManager = mockk<SessionManager>(relaxed = true),
        )
    }

    @Test
    fun `PDF 요약 SSE 완료 시 quizRepository getUserQuizzes가 documentId·DOCUMENT타입으로 호출된다`() = runTest {
        // Given
        coEvery { quizRepository.getUserQuizzes(any(), any()) } returns
                ApiResultV2.Success(message = null, data = listOf(dummyUserQuiz))

        // When
        viewModel.initSummary(
            goalId = goalId,
            goalType = DomainGoalType.DOCUMENT,
            documentId = documentId,
            categoryId = null,
        )

        // Then
        coVerify {
            quizRepository.getUserQuizzes(documentId.toInt(), GoalTypeUiState.DOCUMENT)
        }
    }

    @Test
    fun `퀴즈 목록이 비어있으면 0,5초 후 재시도해 다시 조회한다`() = runTest {
        // Given: 처음엔 빈 목록(아직 생성 중), 두 번째 조회부터 정상 목록 반환
        coEvery { quizRepository.getUserQuizzes(any(), any()) } returnsMany listOf(
            ApiResultV2.Success(message = null, data = emptyList()),
            ApiResultV2.Success(message = null, data = listOf(dummyUserQuiz)),
        )

        // When
        viewModel.initSummary(
            goalId = goalId,
            goalType = DomainGoalType.DOCUMENT,
            documentId = documentId,
            categoryId = null,
        )
        advanceUntilIdle() // 0.5초 delay 이후의 재시도 코루틴까지 실행 완료

        // Then: 빈 목록 응답 후 재시도하여 총 2번 호출되고, 버튼(isQuizLoading)은 최종적으로 풀린다
        coVerify(exactly = 2) {
            quizRepository.getUserQuizzes(documentId.toInt(), GoalTypeUiState.DOCUMENT)
        }
        assertFalse(viewModel.uiState.value.isQuizLoading)
    }

    @Test
    fun `퀴즈 프리페치가 실패해도 isQuizLoading은 false로 복귀한다`() = runTest {
        // Given
        coEvery { quizRepository.getUserQuizzes(any(), any()) } returns
                ApiResultV2.NetworkError(message = "네트워크 오류")

        // When
        viewModel.initSummary(
            goalId = goalId,
            goalType = DomainGoalType.DOCUMENT,
            documentId = documentId,
            categoryId = null,
        )

        // Then
        assertFalse(viewModel.uiState.value.isQuizLoading)
    }
}
