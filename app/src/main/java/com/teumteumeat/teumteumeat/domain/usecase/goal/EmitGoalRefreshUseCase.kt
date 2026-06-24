package com.teumteumeat.teumteumeat.domain.usecase.goal

import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import javax.inject.Inject

class EmitGoalRefreshUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke() = goalRepository.emitRefreshSignal()
}
