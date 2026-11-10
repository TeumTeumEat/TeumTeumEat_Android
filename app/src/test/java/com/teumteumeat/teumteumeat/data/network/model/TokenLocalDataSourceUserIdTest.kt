package com.teumteumeat.teumteumeat.data.network.model

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Base64

/** 저장된 액세스 토큰(JWT)에서 계정 id(`sub`)가 읽히고, 토큰이 바뀌면 함께 바뀌는지 검증한다. */
class TokenLocalDataSourceUserIdTest {

    private fun jwt(sub: String): String {
        val enc = Base64.getUrlEncoder().withoutPadding()
        fun e(s: String) = enc.encodeToString(s.toByteArray())
        return "${e("""{"alg":"HS256"}""")}.${e("""{"sub":"$sub","role":"USER"}""")}.sig"
    }

    /** SharedPreferences 에 [accessToken] 이 저장된 상태의 [TokenLocalDataSource]. */
    private fun dataSource(accessToken: String?): TokenLocalDataSource {
        val prefs = mockk<SharedPreferences>(relaxed = true)
        every { prefs.getString("access_token", null) } returns accessToken
        val context = mockk<Context>()
        every { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) } returns prefs
        return TokenLocalDataSource(context)
    }

    @Test
    fun `저장된 액세스 토큰의 sub 를 계정 id 로 돌려준다`() {
        assertEquals("42", dataSource(jwt("42")).getUserId())
    }

    @Test
    fun `토큰이 없으면 null 이다`() {
        assertNull(dataSource(null).getUserId())
    }

    @Test
    fun `JWT 가 아닌 토큰이면 null 이다`() {
        assertNull(dataSource("opaque-token").getUserId())
    }

    @Test
    fun `로그인으로 토큰을 저장하면 새 계정 id 로 바뀌고 clear 하면 null 이 된다`() {
        val source = dataSource(jwt("A"))
        assertEquals("A", source.getUserId())

        source.save(AuthToken(accessToken = jwt("B"), refreshToken = "r"))
        assertEquals("B", source.getUserId())

        source.clear()
        assertNull(source.getUserId())
    }
}
