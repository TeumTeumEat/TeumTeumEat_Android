package com.teumteumeat.teumteumeat.domain.usecase.on_boarding

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.AccountInfoResponse
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import javax.inject.Inject

class GetAccountInfoUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): ApiResultV2<AccountInfoResponse> =
        userRepository.getAccountInfo()
}
