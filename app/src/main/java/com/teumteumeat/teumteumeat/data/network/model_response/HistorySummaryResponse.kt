package com.teumteumeat.teumteumeat.data.network.model_response

import com.teumteumeat.teumteumeat.domain.model.history.DailySummary
import java.time.LocalDateTime

data class HistorySummaryResponse(
    val title: String,
    val summary: String,
    val createdAt: String
)

fun HistorySummaryResponse.toDomain(): DailySummary {
    return DailySummary(
        title = title,
        summary = summary,
        createdAt = LocalDateTime.parse(createdAt)
    )
}
