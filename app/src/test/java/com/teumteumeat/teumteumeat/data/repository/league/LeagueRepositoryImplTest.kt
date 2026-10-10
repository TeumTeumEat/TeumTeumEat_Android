package com.teumteumeat.teumteumeat.data.repository.league

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.league.LeagueApiService
import com.teumteumeat.teumteumeat.data.datastore.LeagueResultDataStore
import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueLatestResultResponse
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueMeDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
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
    private val authApi = mockk<AuthApiService>()
    private val repository = LeagueRepositoryImpl(api, dataStore, authApi, tokenSource)
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

    // ---- GET /api/v1/league/me ----

    private fun me(rank: Int?) = ApiResponse<LeagueMeDto, Any?>(
        code = "OK", message = "ok", details = null,
        data = LeagueMeDto(name = "김*민", weeklySnackCount = 12, todaySnackCount = 3, rank = rank),
    )

    @Test
    fun `내 순위 조회에 성공하면 순위와 스낵 수를 돌려준다`() = runTest {
        coEvery { api.getMyLeagueRank() } returns me(rank = 15)

        val result = repository.getMyLeagueRank() as ApiResultV2.Success

        assertEquals(15, result.data.rank)
        assertEquals("김*민", result.data.nickname)
        assertEquals(12, result.data.weeklySnackCount)
        assertEquals(3, result.data.todaySnackCount)
    }

    @Test
    fun `이번 주 스낵이 0개면 내 순위는 null 로 성공한다`() = runTest {
        coEvery { api.getMyLeagueRank() } returns me(rank = null)

        val result = repository.getMyLeagueRank() as ApiResultV2.Success

        assertNull(result.data.rank)
    }

    @Test
    fun `내 순위 조회가 404 USER-001 이면 서버 오류 코드로 감싸 돌려준다`() = runTest {
        val body = """{"code":"USER-001","message":"존재하지 않는 유저입니다."}""".toResponseBody("application/json".toMediaType())
        coEvery { api.getMyLeagueRank() } throws HttpException(Response.error<Any>(404, body))

        val result = repository.getMyLeagueRank()

        assertTrue(result is ApiResultV2.ServerError)
        assertEquals("USER-001", (result as ApiResultV2.ServerError).code)
    }

    @Test
    fun `내 순위 조회 중 네트워크 오류는 예외 대신 결과로 감싼다`() = runTest {
        coEvery { api.getMyLeagueRank() } throws IOException("offline")

        assertTrue(repository.getMyLeagueRank() is ApiResultV2.NetworkError)
    }

    // ---- 401(세션 만료) 처리: FCM 같은 백그라운드 호출 시 유의할 동작을 고정해 둔다 ----

    private fun unauthorized() = HttpException(
        Response.error<Any>(401, "".toResponseBody("application/json".toMediaType()))
    )

    @Test
    fun `내 순위 조회가 401 인데 저장된 리프레시 토큰이 없으면 SessionExpired 를 돌려주고 재발급은 시도하지 않는다`() = runTest {
        coEvery { api.getMyLeagueRank() } throws unauthorized()
        every { tokenSource.getRefreshToken() } returns null

        val result = repository.getMyLeagueRank()

        assertTrue(result is ApiResultV2.SessionExpired)
        coVerify(exactly = 0) { authApi.reissueAccessTokenV2(any()) }
    }

    @Test
    fun `내 순위 조회가 401 이고 토큰 재발급이 네트워크 오류로 실패하면 저장된 토큰을 삭제하고 SessionExpired 를 돌려준다`() = runTest {
        // BaseRepository 의 현재 동작을 고정한다 — 재발급 중 네트워크 오류도 토큰 삭제로 이어진다(백그라운드 호출 시 로그아웃 위험).
        coEvery { api.getMyLeagueRank() } throws unauthorized()
        every { tokenSource.getRefreshToken() } returns "refresh-token"
        coEvery { authApi.reissueAccessTokenV2(any()) } throws IOException("offline")

        val result = repository.getMyLeagueRank()

        assertTrue(result is ApiResultV2.SessionExpired)
        verify(exactly = 1) { tokenSource.clear() }
    }
}
