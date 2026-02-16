package com.teumteumeat.teumteumeat.domain.usecase.goal

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_request.UpdateGoalRequest
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import javax.inject.Inject

/**
 * 목표 수정 UseCase
 *
 * @param goalRepository 목표 관련 데이터 접근을 담당하는 Repository
 */
class UpdateGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {

    /**
     * 기존 목표를 수정한다.
     *
     * @param goalId 수정할 목표 ID
     * @param request 목표 수정 요청 Body (생성과 동일)
     * @return 수정된 목표 ID
     */
    suspend operator fun invoke(
        goalId: Long,
    ): ApiResultV2<Unit> {
        return goalRepository.updateGoal(
            goalId = goalId,
        )
    }
}
