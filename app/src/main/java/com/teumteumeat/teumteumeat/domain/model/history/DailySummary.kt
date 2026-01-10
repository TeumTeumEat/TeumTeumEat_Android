package com.teumteumeat.teumteumeat.domain.model.history

import java.time.LocalDateTime

data class DailySummary(
    val title: String,
    val summary: String,
    val createdAt: LocalDateTime
)
