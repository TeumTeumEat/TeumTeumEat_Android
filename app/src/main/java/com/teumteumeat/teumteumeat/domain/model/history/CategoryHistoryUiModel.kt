package com.teumteumeat.teumteumeat.domain.model.history

import com.teumteumeat.teumteumeat.domain.model.common.DomainGoalType_v1
import java.time.LocalDateTime

data class CategoryHistoryUiModel(
    val categoryName: String,
    val histories: List<LearningHistoryUiModel>
)

data class LearningHistoryUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val date: LocalDateTime,
    val dateText: String,
    val domainGoalTypeV1: DomainGoalType_v1,
)