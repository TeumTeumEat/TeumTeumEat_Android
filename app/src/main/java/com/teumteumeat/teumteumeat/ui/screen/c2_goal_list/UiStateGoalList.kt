package com.teumteumeat.teumteumeat.ui.screen.c2_goal_list

import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.utils.Utils.TypeUtils.toUiText


data class UiStateGoalList(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val goals: List<GoalCardUiModel> = emptyList()
)

data class GoalCardUiModel(
    val goalId: Int,

    // 배지
    val weekText: String,        // "4주"
    val difficultyText: String,  // "난이도 상"
    val showDifficulty: Boolean,

    // 메인 텍스트
    val title: String,
    val description: String,

    // 상태
    val isSelected: Boolean
)

fun GetGoalResponse.toUiModel(
    isSelected: Boolean
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


    return GoalCardUiModel(
        goalId = goalId ?: -1,
        weekText = studyPeriod,
        difficultyText = difficulty.toUiText(),
        showDifficulty = true,
        title = title,
        description = description,
        isSelected = isSelected
    )
}

