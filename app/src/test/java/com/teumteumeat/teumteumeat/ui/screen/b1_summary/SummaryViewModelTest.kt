package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.teumteumeat.teumteumeat.TestApplication
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.document.PdfDocumentSummary
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.quiz.UserQuizStatus
import com.teumteumeat.teumteumeat.domain.repository.pff_document.PdfDocumentRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.document.StreamPdfSummaryUseCase
import com.teumteumeat.teumteumeat.domain.usecase.summary.StreamDailySummaryUseCase
import com.teumteumeat.teumteumeat.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PDF(DOCUMENT) 요약 SSE 완료 시 퀴즈 프리페치(createDocumentQuiz) 호출을 검증한다.
 */
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
    fun `PDF 요약 SSE 완료 시 createDocumentQuiz가 goalId·documentId로 호출된다`() = runTest {
        // Given
        coEvery { pdfDocumentRepository.createDocumentQuiz(any(), any()) } returns
                ApiResultV2.Success(message = null, data = Unit)

        // When
        viewModel.initSummary(
            goalId = goalId,
            goalType = DomainGoalType.DOCUMENT,
            documentId = documentId,
            categoryId = null,
        )

        // Then
        coVerify {
            pdfDocumentRepository.createDocumentQuiz(goalId.toInt(), documentId.toInt())
        }
    }

    @Test
    fun `퀴즈 프리페치가 실패해도 isQuizLoading은 false로 복귀한다`() = runTest {
        // Given
        coEvery { pdfDocumentRepository.createDocumentQuiz(any(), any()) } returns
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
