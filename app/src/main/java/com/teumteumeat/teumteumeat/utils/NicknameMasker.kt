package com.teumteumeat.teumteumeat.utils

/** 마스킹 후 가운데에 남기는 `*` 의 최대 개수. */
private const val MAX_MIDDLE_ASTERISKS = 3

/**
 * 리그 랭킹 닉네임 마스킹 — 서버(GET /api/v1/league)가 내려주는 규칙과 동일하다.
 * - 2자: 첫 글자만 노출 (예: "크*")
 * - 3자 이상: 첫/마지막 글자만 노출하고 가운데는 `*` 로 가리되 최대 3개로 고정
 *   (3자 "크*크", 4자 "크**크", 5자 이상 "크***크")
 *
 * 서버가 이미 마스킹한 값에 다시 적용해도 결과가 같다(멱등). 폭이 좁은 포디움 카드에서 표시 길이를
 * 최대 5자로 보장하는 용도다. 1자 이하는 규칙에 없는 값(서버도 가리지 않음)이라 그대로 반환한다.
 */
fun String.toMaskedNickname(): String = when {
    length <= 1 -> this
    length == 2 -> "${first()}*"
    else -> "${first()}${"*".repeat((length - 2).coerceAtMost(MAX_MIDDLE_ASTERISKS))}${last()}"
}
