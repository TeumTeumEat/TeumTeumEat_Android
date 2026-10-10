package com.teumteumeat.teumteumeat.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.util.Base64

/**
 * JWT(`header.payload.signature`)의 payload 에서 `sub` 클레임을 꺼낸다. 서명은 검증하지 않는다(앱은 값을 읽기만 한다).
 * JWT 형식이 아니거나, 디코딩/파싱에 실패하거나, `sub` 가 없거나 비어 있으면 null.
 *
 * 서버가 발급하는 토큰 형식(`sub` = 유저 고유 id)에 클라이언트가 의존한다. 형식이 바뀌면 null 이 되므로 호출부는 폴백을 둬야 한다.
 */
fun String.toJwtSubjectOrNull(): String? {
    val parts = split(".")
    if (parts.size != 3) return null
    return try {
        val payload = String(Base64.getUrlDecoder().decode(parts[1]), Charsets.UTF_8)
        val claims = JsonParser.parseString(payload) as? JsonObject
        claims?.get("sub")?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
    } catch (e: IllegalArgumentException) { // Base64 디코딩 실패
        null
    } catch (e: JsonParseException) {
        null
    }
}
