package com.teumteumeat.teumteumeat.domain.usecase.notice

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.domain.model.notice.NoticePage
import com.teumteumeat.teumteumeat.domain.repository.notice.NoticeRepository
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/**
 * [GetNoticeListUseCase] 단위 테스트.
 * Repository를 모킹해 "무엇을 어떤 인자로 호출하는가"와 "결과를 그대로 전달하는가"만 검증한다.
 */
class GetNoticeListUseCaseTest {

    private val repository = mockk<NoticeRepository>()
    private lateinit var useCase: GetNoticeListUseCase

    private val emptyPage = NoticePage(notices = emptyList(), hasNext = false)

    @Before
    fun setUp() {
        coEvery { repository.getNotices(any(), any()) } returns
            ApiResultV2.Success(message = null, data = emptyPage)
        useCase = GetNoticeListUseCase(repository)
    }

    @Test
    fun `인자를 생략하면 커서 없이 기본 20건으로 첫 페이지를 요청한다`() = runTest {
        useCase()

        coVerify(exactly = 1) { repository.getNotices(lastNoticeId = null, size = 20) }
    }

    @Test
    fun `커서는 변환 없이 그대로 전달된다`() = runTest {
        useCase(lastNoticeId = 42L)

        coVerify(exactly = 1) { repository.getNotices(lastNoticeId = 42L, size = 20) }
    }

    @Test
    fun `size가 허용 범위 안이면 그대로 전달된다`() = runTest {
        assertSizeSent(requested = 1, expected = 1)
        assertSizeSent(requested = 20, expected = 20)
        assertSizeSent(requested = 50, expected = 50)
    }

    @Test
    fun `size가 허용 범위 밖이면 가까운 경계값 1~50으로 보정되어 전달된다`() = runTest {
        assertSizeSent(requested = 0, expected = 1)
        assertSizeSent(requested = -5, expected = 1)
        assertSizeSent(requested = 51, expected = 50)
        assertSizeSent(requested = 1_000, expected = 50)
    }

    /** [requested] 크기로 호출했을 때 Repository에 [expected]가 한 번 전달되는지 확인한다. */
    private suspend fun assertSizeSent(requested: Int, expected: Int) {
        clearMocks(repository, answers = false)

        useCase(size = requested)

        coVerify(exactly = 1) { repository.getNotices(null, expected) }
    }

    @Test
    fun `Repository 성공 결과를 그대로 반환한다`() = runTest {
        val success = ApiResultV2.Success(message = "ok", data = emptyPage)
        coEvery { repository.getNotices(any(), any()) } returns success

        assertSame(success, useCase())
    }

    @Test
    fun `Repository 실패 결과도 가공하지 않고 반환한다`() = runTest {
        val error = ApiResultV2.ServerError(
            code = "NOTICE-001",
            message = "공지를 불러오지 못했어요",
            errorType = DomainError.None
        )
        coEvery { repository.getNotices(any(), any()) } returns error

        assertSame(error, useCase())
    }
}
