package com.teumteumeat.teumteumeat.domain.model.common

/**
 * 앱 전반에서 사용하는 공용 콘텐츠 유형
 *
 * - CATEGORY : 카테고리 기반 퀴즈 / 학습
 * - DOCUMENT : 문서 기반 학습
 *
 * ❗ NONE은 UI 초기 상태 전용으로만 사용
 */
enum class GoalTypeUiState {
    DOCUMENT,
    CATEGORY,
    NONE;

    companion object {
        fun fromString(value: String?): GoalTypeUiState {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: NONE
        }
    }
}