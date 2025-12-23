package com.teumteumeat.teumteumeat.domain.usecase.on_boarding

import com.teumteumeat.teumteumeat.data.api.user.CommuteTimeRequest
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toServerTime
import javax.inject.Inject

class UpdateCommuteTimeUseCase @Inject constructor(
    private val repository: UserRepository
) {

    suspend operator fun invoke(
        start: TimeState,
        end: TimeState,
        usageTime: Int
    ): ApiResult<Unit, Unit> {

        val request = CommuteTimeRequest(
            startTime = start.toServerTime(),
            endTime = end.toServerTime(),
            usageTime = usageTime
        )

        return repository.updateCommuteTime(request)
    }
}
