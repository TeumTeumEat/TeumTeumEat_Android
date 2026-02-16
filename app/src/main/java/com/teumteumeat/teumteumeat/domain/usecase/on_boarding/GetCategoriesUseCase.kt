package com.teumteumeat.teumteumeat.domain.usecase.on_boarding

import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): ApiResult<List<Category>, Any?> {
        return userRepository.getCategories()
    }
}
