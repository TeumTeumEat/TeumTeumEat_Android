package com.teumteumeat.teumteumeat.data.repository.login

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.datastore.LeagueResultDataStore
import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/** 회원 탈퇴 시 해당 계정 id 의 리그 결과 확인 기록이 기기에서 삭제되는지 검증한다. */
class SocialLoginRepositoryImplWithdrawTest {

    private val authApi = mockk<AuthApiService>()
    private val token = mockk<TokenLocalDataSource>(relaxed = true)
    private val leagueStore = mockk<LeagueResultDataStore>(relaxed = true)
    private val repository = SocialLoginRepositoryImpl(mockk(relaxed = true), authApi, token, leagueStore)

    @Test
    fun `탈퇴에 성공하면 탈퇴한 계정의 기록을 지운다`() = runTest {
        every { token.getUserId() } returns "42"
        coEvery { authApi.withdrawUser() } returns ApiResponse(code = "OK", message = "ok", details = null, data = "")

        coEvery { leagueStore.clear("42") } returns true

        val result = repository.withdrawUser()

        assertTrue(result is ApiResultV2.Success)
        coVerify(exactly = 1) { leagueStore.clear("42") }
    }

    @Test
    fun `탈퇴에 실패하면 기록을 지우지 않는다`() = runTest {
        every { token.getUserId() } returns "42"
        coEvery { authApi.withdrawUser() } throws IOException("offline")

        val result = repository.withdrawUser()

        assertTrue(result is ApiResultV2.NetworkError)
        coVerify(exactly = 0) { leagueStore.clear(any()) }
    }

    @Test
    fun `기록 삭제에 실패해도 탈퇴 성공 결과는 그대로 돌려준다`() = runTest {
        every { token.getUserId() } returns "42"
        coEvery { authApi.withdrawUser() } returns ApiResponse(code = "OK", message = "ok", details = null, data = "")
        coEvery { leagueStore.clear("42") } returns false // 디스크 오류

        val result = repository.withdrawUser()

        assertTrue(result is ApiResultV2.Success)
        coVerify(exactly = 1) { leagueStore.clear("42") }
    }
}
