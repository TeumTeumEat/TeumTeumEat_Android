package com.teumteumeat.teumteumeat.utils

/**
 * 닉네임 마스킹 공통 규칙.
 * - 1자: 전체 마스킹 (예: "이" → "*")
 * - 2자: 첫 글자만 노출 (예: "이민" → "이*")
 * - 3자 이상: 첫 글자·마지막 글자만 노출, 중간은 `*`로 마스킹.
 *   [maxMiddleAsterisks]가 주어지면 중간 `*` 개수를 그 값 이하로 제한한다(원본 길이가 길어도 결과 길이가
 *   `2 + maxMiddleAsterisks`를 넘지 않음). null이면 원본 길이만큼(= `length - 2`) 그대로 마스킹한다.
 */
private fun String.maskedByLength(maxMiddleAsterisks: Int? = null): String = when (length) {
    0 -> this
    1 -> "*"
    2 -> "${first()}*"
    else -> {
        val middleAsterisks = (length - 2).let { count ->
            if (maxMiddleAsterisks != null) count.coerceAtMost(maxMiddleAsterisks) else count
        }
        "${first()}${"*".repeat(middleAsterisks)}${last()}"
    }
}

/**
 * 리그 랭킹 등 공개 영역에 노출되는 닉네임을 마스킹한다.
 * - 1자: 전체 마스킹 (예: "이" → "*")
 * - 2자: 첫 글자만 노출 (예: "이민" → "이*")
 * - 3자 이상: 첫 글자·마지막 글자만 노출, 중간은 모두 마스킹 (예: "이영민" → "이*민")
 */
fun String.toMaskedNickname(): String = maskedByLength()

/**
 * [LeagueTop3Item]처럼 폭이 좁은 카드에서 닉네임이 영역을 넘치지 않도록, [toMaskedNickname]과
 * 동일한 마스킹 규칙(2자 → "이*", 3자 이상 → 첫/마지막 글자만 노출)을 적용하되 중간 `*` 개수를
 * 최대 `maxLength - 2`개로 제한해 결과 길이가 [maxLength]를 넘지 않도록 한다
 * (예: 5자 제한 시 "이영민수영민수이"(8자) → "이***이").
 * 이미 [toMaskedNickname]으로 마스킹된 값이든 원본 값이든 첫/마지막 글자가 동일하게 보존되므로
 * 어느 쪽에 적용해도 결과는 같다.
 */
fun String.capNicknameDisplayLength(maxLength: Int = 5): String {
    require(maxLength >= 3) { "maxLength는 최소 3 이상이어야 첫/마지막 글자와 마스킹 글자를 표현할 수 있습니다." }
    return maskedByLength(maxMiddleAsterisks = maxLength - 2)
}
