package com.teumteumeat.teumteumeat.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Base64

class JwtSubjectTest {

    private fun jwt(payloadJson: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        fun enc(s: String) = encoder.encodeToString(s.toByteArray())
        return "${enc("""{"alg":"HS256"}""")}.${enc(payloadJson)}.signature"
    }

    @Test
    fun `payload 의 sub 를 문자열로 꺼낸다`() {
        assertEquals("123", jwt("""{"sub":"123","role":"USER","exp":1}""").toJwtSubjectOrNull())
    }

    @Test
    fun `sub 가 숫자여도 문자열로 꺼낸다`() {
        assertEquals("123", jwt("""{"sub":123}""").toJwtSubjectOrNull())
    }

    @Test
    fun `sub 가 없거나 비어 있으면 null 이다`() {
        assertNull(jwt("""{"role":"USER"}""").toJwtSubjectOrNull())
        assertNull(jwt("""{"sub":""}""").toJwtSubjectOrNull())
        assertNull(jwt("""{"sub":null}""").toJwtSubjectOrNull())
        assertNull(jwt("""{"sub":{"id":1}}""").toJwtSubjectOrNull())
    }

    @Test
    fun `JWT 형식이 아니거나 깨진 토큰이면 예외 없이 null 이다`() {
        assertNull("not-a-jwt".toJwtSubjectOrNull())
        assertNull("".toJwtSubjectOrNull())
        assertNull("a.b".toJwtSubjectOrNull())
        assertNull("a.@@@.c".toJwtSubjectOrNull())                  // Base64 가 아님
        assertNull("a.${Base64.getUrlEncoder().encodeToString("not json".toByteArray())}.c".toJwtSubjectOrNull())
    }
}
