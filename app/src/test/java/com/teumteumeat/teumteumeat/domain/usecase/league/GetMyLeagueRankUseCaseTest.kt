package com.teumteumeat.teumteumeat.domain.usecase.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.league.LeagueMe
import com.teumteumeat.teumteumeat.domain.repository.league.LeagueRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** UseCase 가 Repository 의 결과를 가공 없이 그대로(모든 값 포함) 돌려주는지 검증한다. */
class GetMyLeagueRankUseCaseTest {

    private val repository = mockk<LeagueRepository>()
    private val useCase = GetMyLeagueRankUseCase(repository)

    @Test
    fun `Repository 가 준 내 순위를 모든 값 그대로 돌려준다`() = runTest {
        val success = ApiResultV2.Success(
            message = null,
            data = LeagueMe(nickname = "김*민", weeklySnackCount = 12, todaySnackCount = 3, rank = 15),
        )
        coEvery { repository.getMyLeagueRank() } returns success

        val result = useCase()

        assertEquals(success, result)
        coVerify(exactly = 1) { repository.getMyLeagueRank() }
    }

    @Test
    fun `Repository 의 실패 결과도 가공 없이 그대로 돌려준다`() = runTest {
        val failure = ApiResultV2.NetworkError(message = "offline")
        coEvery { repository.getMyLeagueRank() } returns failure

        assertEquals(failure, useCase())
    }
}
