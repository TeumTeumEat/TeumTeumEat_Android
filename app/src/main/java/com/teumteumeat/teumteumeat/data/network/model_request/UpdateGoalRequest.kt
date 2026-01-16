package com.teumteumeat.teumteumeat.data.network.model_request

import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty

/**
 * 목표 수정 요청 Body
 *
 * 서버 요구사항:
 * {
 *   "studyPeriod": "1주",
 *   "difficulty": "EASY",
 *   "prompt": "~~식으로 문제를 내줘."
 * }
 */
data class UpdateGoalRequest(
    val studyPeriod: String,
    val difficulty: Difficulty,
    val prompt: String?
)
