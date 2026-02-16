package com.teumteumeat.teumteumeat.data.network.model_request

import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty

/**
 * 목표 생성 / 수정 시 공통으로 사용하는 요청 Body
 */
data class CreateGoalRequest(
    val type: GoalTypeUiState, // 목표 타입 (예: DAILY, DOCUMENT 등)
    val studyPeriod: String,   // 학습 기간 (ex: "2026-01-01~2026-01-31")
    val difficulty: Difficulty, // 난이도
    val prompt: String?,       // 사용자 프롬프트 (선택값)
    val categoryId: Int?       // 카테고리 ID (DOCUMENT 타입이면 null)
)
