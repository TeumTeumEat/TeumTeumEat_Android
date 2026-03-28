package com.teumteumeat.teumteumeat.domain.usecase.date

import com.teumteumeat.teumteumeat.domain.repository.date.DateChangeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDateChangeUseCase @Inject constructor(
    private val repository: DateChangeRepository
) {
    operator fun invoke(): Flow<Unit> = repository.observeDateChange()
}