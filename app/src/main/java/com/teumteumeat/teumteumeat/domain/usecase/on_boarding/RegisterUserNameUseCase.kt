package com.teumteumeat.teumteumeat.domain.usecase.on_boarding

import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.FieldErrorDetail
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import javax.inject.Inject

class RegisterUserNameUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String): ApiResult<String, List<FieldErrorDetail>> {
        return userRepository.updateUserName(name)
    }
}
