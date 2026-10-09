package com.teumteumeat.teumteumeat.data.repository.league

import com.teumteumeat.teumteumeat.data.api.league.LeagueApiService
import com.teumteumeat.teumteumeat.data.datastore.LeagueResultDataStore
import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueLatestResultResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

/** 지난주 결과가 '주당 1회'만 내려가는지(서버 응답 + 기기에 기록한 마지막 확인 주) 검증한다. */
class LeagueRepositoryImplTest {

    private val api = mockk<LeagueApiService>()
    private val dataStore = mockk<LeagueResultDataStore>(relaxed = true)
    private val tokenSource = mockk<TokenLocalDataSource>(relaxed = true)
    private val repository = LeagueRepositoryImpl(api, dataStore, mockk(relaxed = true), tokenSource)
    private val userId = "42"

    @Before
    fun setUp() {
        every { tokenSource.getUserId() } returns userId
    }

    private val lastWeek = LocalDate.of(2026, 9, 28)

    private fun latest(rank: Int?, weekStart: LocalDate = lastWeek) = ApiResponse<LeagueLatestResultResponse, Any?>(
        code = "OK", message = "ok", details = null,
        data = LeagueLatestResultResponse(weekStartDate = weekStart.toString(), rank = rank, weeklySnackCount = 42),
    )

    private suspend fun pending() = (repository.getPendingResult() as ApiResultV2.Success).data

    @Test
    fun `확인한 기록이 없으면 지난주 결과를 내려준다`() = runTest {
        coEvery { api.getLatestLeagueResult() } returns latest(rank = 1)
        coEvery { dataStore.getLastSeenWeekStart(userId) } returns null

        val result = pending()

        assertEquals(1, result!!.rank)
        assertEquals(lastWeek, result.weekStart)
    }

    @Test
    fun `이미 확인한 주의 결과는 내려주지 않는다`() = runTest {
        coEvery { api.getLatestLeagueResult() } returns latest(rank = 1)
        coEvery { dataStore.getLastSeenWeekStart(userId) } returns lastWeek

        assertNull(pending())
    }

    @Test
    fun `마지막으로 확인한 주보다 새로운 주의 결과는 내려준다`() = runTest {
        coEvery { api.getLatestLeagueResult() } returns latest(rank = 3)
        coEvery { dataStore.getLastSeenWeekStart(userId) } returns lastWeek.minusWeeks(1)

        assertEquals(3, pending()!!.rank)
    }

    @Test
    fun `직전 주 스낵이 0개라 순위가 없으면 결과가 없다(리그에 참여한 뒤 한 주를 쉬고 진입한 경우 포함)`() = runTest {
        coEvery { api.getLatestLeagueResult() } returns latest(rank = null)
        coEvery { dataStore.getLastSeenWeekStart(userId) } returns null

        assertNull(pending())
    }

    @Test
    fun `네트워크 오류는 예외를 던지지 않고 오류 결과로 감싼다`() = runTest {
        coEvery { api.getLatestLeagueResult() } throws IOException("offline")

        assertTrue(repository.getPendingResult() is ApiResultV2.NetworkError)
    }

    @Test
    fun `확인 처리하면 해당 주 시작일을 기기에 기록한다`() = runTest {
        coEvery { dataStore.markSeen(any(), any()) } returns true

        val result = repository.markResultSeen(lastWeek)

        assertTrue(result is ApiResultV2.Success)
        coVerify { dataStore.markSeen(userId, lastWeek) }
    }

    @Test
    fun `확인 기록 저장에 실패하면 예외 대신 UnknownError 를 돌려준다`() = runTest {
        coEvery { dataStore.markSeen(userId, lastWeek) } returns false

        val result = repository.markResultSeen(lastWeek)

        assertTrue(result is ApiResultV2.UnknownError)
    }
}
