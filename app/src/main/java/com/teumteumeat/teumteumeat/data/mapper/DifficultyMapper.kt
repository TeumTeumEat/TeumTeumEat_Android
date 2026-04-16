package com.teumteumeat.teumteumeat.data.mapper

import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty

/**
 * 난이도(Difficulty) Enum 값을 UI에 표시할 한글 명칭으로 변환합니다.
 */
fun Difficulty.toLable(): String {
    return when (this) {
        Difficulty.EASY -> "하"
        Difficulty.MEDIUM -> "중"
        Difficulty.HARD -> "상"
        Difficulty.NONE -> "선택안함"
    }
}