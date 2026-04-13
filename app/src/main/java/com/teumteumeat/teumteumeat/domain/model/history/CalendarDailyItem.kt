package com.teumteumeat.teumteumeat.domain.model.history

import com.teumteumeat.teumteumeat.domain.model.common.DomainGoalType_v1
import java.time.LocalDateTime


/**
 * 캘린더 특정 일자에 대한 학습/퀴즈 진행 내역 아이템
 */
data class CalendarDailyItem(
    val id: Long = 0,
    val type: DomainGoalType_v1 = DomainGoalType_v1.DOCUMENT,
    val title: String = "",
    val summarySnippet: String = "",
    val lastStudiedAt: LocalDateTime = LocalDateTime.now()
)

