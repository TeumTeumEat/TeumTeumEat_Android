package com.teumteumeat.teumteumeat.domain.usecase.auth

import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val tokenLocalDataSource: TokenLocalDataSource
) {

    suspend operator fun invoke() {
        tokenLocalDataSource.clear()
    }
}