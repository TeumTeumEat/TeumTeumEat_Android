package com.teumteumeat.teumteumeat.domain.usecase.on_boarding

import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.FieldErrorDetail
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.model.on_boarding.NameUpdateError
import javax.inject.Inject

class RegisterUserNameUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String): ApiResult<String, NameUpdateError> {

        return when (val result = userRepository.updateUserName(name)) {

            is ApiResult.Success -> result

            is ApiResult.ServerError -> {
                val error = when {
                    result.details.isNullOrEmpty() ->
                        NameUpdateError.Message(result.message)

                    else ->
                        NameUpdateError.Validation(
                            result.details
                                .filter { it.field == "name" }
                                .map { it.message }
                        )
                }

                ApiResult.ServerError(
                    code = result.code,
                    message = result.message,
                    details = error
                )
            }

            // 🔽 여기서 공통 에러를 ServerError로 흡수
            // 🔑 공통 에러는 여기서 흡수
            is ApiResult.SessionExpired ->
                ApiResult.ServerError(
                    code = "SESSION",
                    message = result.message,
                    details = NameUpdateError.CommonMessage(result.message)
                )

            is ApiResult.NetworkError ->
                ApiResult.ServerError(
                    code = "NETWORK",
                    message = result.message,
                    details = NameUpdateError.CommonMessage(result.message)
                )

            is ApiResult.UnknownError ->
                ApiResult.ServerError(
                    code = "UNKNOWN",
                    message = result.message,
                    details = NameUpdateError.CommonMessage(result.message)
                )
        }
    }
}
