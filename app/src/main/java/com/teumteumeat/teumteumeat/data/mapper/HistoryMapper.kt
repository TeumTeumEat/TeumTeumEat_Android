package com.teumteumeat.teumteumeat.data.mapper

import com.teumteumeat.teumteumeat.domain.model.history.DailySummary
import com.teumteumeat.teumteumeat.data.history.response.HistorySummaryResponse
import java.time.LocalDateTime

fun HistorySummaryResponse.toPdfDocumentSummary(): DailySummary {
    return DailySummary(
        title = title,
        summary = summary,
        createdAt = LocalDateTime.parse(createdAt)
    )
}