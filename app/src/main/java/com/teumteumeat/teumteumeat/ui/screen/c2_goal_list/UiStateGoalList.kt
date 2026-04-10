package com.teumteumeat.teumteumeat.ui.screen.c2_goal_list

import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.utils.Utils.TypeUtils.toUiText
import java.time.LocalDate


data class UiStateGoalList(
    val isChanged: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val currentGoalId: Long? = null,
    val goals: List<GoalCardUiModel> = emptyList(),

    // ⭐ 주제 변경 확인 오버레이
    val showChangeGoalOverlay: Boolean = false,
    val pendingGoalId: Int? = null // 변경하려는 목표 ID
)

data class GoalCardUiModel(
    val goalId: Int,

    // 배지
    val weekText: String,        // "4주"
    val difficulty: Difficulty,   // ⭐ 추가
    val difficultyText: String,  // "난이도 상"

    val showDifficulty: Boolean,

    // 메인 텍스트
    val title: String,
    val description: String,

    /** 목표의 만료여부는 목표가 완료되었는지로 구분됨 **/
    val isCompleted: Boolean,

    // 상태
    val isSelected: Boolean,
    val isExpired: Boolean,
)

fun GetGoalResponse.toUiModel(
    currentGoalId: Long?
): GoalCardUiModel {

    val (title, description) =
        when (type) {
            GoalTypeUiState.CATEGORY -> {
                (category?.path ?: "미설정") to
                        (prompt ?: "")
            }

            GoalTypeUiState.DOCUMENT -> {
                (fileName ?: "문서") to
                        (prompt ?: "")
            }

            GoalTypeUiState.NONE -> {
                "잘못된 목표" to ""
            }
        }



    val start = startDate.toLocalDate()
    val end = endDate.toLocalDate()
    val today = LocalDate.now()
    val isExpired = today.isAfter(end)

    // ⭐ 만료된 목표는 선택 해제
    val isSelected = goalId.toLong() == currentGoalId

    return GoalCardUiModel(
        goalId = goalId,
        weekText = studyPeriod,
        difficultyText = difficulty.toUiText(),
        difficulty = difficulty,
        showDifficulty = true,
        title = title,
        description = description,
        isSelected = isSelected,
        isCompleted = isCompleted,
        isExpired = isExpired,
    )
}


private fun String.toLocalDate(): LocalDate =
    LocalDate.parse(this) // yyyy-MM-dd 전제

