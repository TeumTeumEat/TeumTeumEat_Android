package com.teumteumeat.teumteumeat.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/** 서버 마스킹 규칙(2자 "크*", 3~5자 가운데 *, 6자 이상 "크***크")과 동일하게 동작하는지 검증한다. */
class NicknameMaskerTest {

    @Test
    fun `2자는 첫 글자만 노출한다`() = assertEquals("크*", "크크".toMaskedNickname())

    @Test
    fun `3자는 가운데 한 글자를 가린다`() = assertEquals("크*크", "크크크".toMaskedNickname())

    @Test
    fun `4자는 가운데 두 글자를 가린다`() = assertEquals("크**크", "크크크크".toMaskedNickname())

    @Test
    fun `5자는 가운데 세 글자를 가린다`() = assertEquals("크***크", "크크크크크".toMaskedNickname())

    @Test
    fun `6자 이상은 길이와 관계없이 별 3개로 고정된다`() {
        assertEquals("크***크", "크크크크크크".toMaskedNickname())
        assertEquals("크***크", "크크크크크크크크크크".toMaskedNickname()) // 닉네임 최대 길이 10자
    }

    @Test
    fun `이미 서버에서 마스킹된 값에 다시 적용해도 그대로다`() {
        listOf("크*", "크*크", "크**크", "크***크", "병*").forEach { assertEquals(it, it.toMaskedNickname()) }
    }

    @Test
    fun `1자 이하와 빈 자리 표시는 그대로 둔다`() {
        assertEquals("크", "크".toMaskedNickname())
        assertEquals("-", "-".toMaskedNickname())
        assertEquals("", "".toMaskedNickname())
    }
}
