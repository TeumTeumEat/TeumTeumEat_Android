package com.teumteumeat.teumteumeat.domain.repository.date

import kotlinx.coroutines.flow.Flow

interface DateChangeRepository {
    /** 날짜 변경 이벤트를 흐름(Flow)으로 제공 */
    fun observeDateChange(): Flow<Unit>
}