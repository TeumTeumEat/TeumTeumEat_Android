package com.teumteumeat.teumteumeat.domain.usecase.on_boarding

import com.teumteumeat.teumteumeat.data.api.user.CommuteTimeRequest
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.utils.Utils.UiUtils.isValidTime
import javax.inject.Inject

class UpdateCommuteTimeUseCase @Inject constructor(
    private val repository: UserRepository,
) {

    suspend operator fun invoke(
        startTime: String,
        endTime: String,
        usageTime: Int
    ): ApiResultV2<Unit> {

        // 비즈니스 규칙
        // 1️⃣ 출근 시간 검증
        if (!isValidTime(startTime)) {
            return ApiResultV2.ServerError(
                code = "INVALID_WORK_IN_TIME",
                message = "시간 형식 오류",
                errorType = DomainError.Message("출근 시간 형식이 올바르지 않습니다.")
            )
        }


        // 2️⃣ 퇴근 시간 검증
        if (!isValidTime(endTime)) {
            return ApiResultV2.ServerError(
                code = "INVALID_WORK_OUT_TIME",
                message = "시간 형식 오류",
                errorType = DomainError.Message("퇴근 시간 형식이 올바르지 않습니다.")
            )
        }


        return repository.updateCommuteInfo(
            CommuteTimeRequest(
                startTime = startTime,
                endTime = endTime,
                usageTime = usageTime
            )
        )
    }
}
