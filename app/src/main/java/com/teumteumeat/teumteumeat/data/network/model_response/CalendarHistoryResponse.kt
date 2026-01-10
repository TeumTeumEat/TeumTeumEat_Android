package com.teumteumeat.teumteumeat.data.network.model_response

import com.google.gson.annotations.SerializedName

data class CalendarHistoryResponse(
    @SerializedName("stampedDates")
    val stampedDates: List<String>,   // yyyy-MM-dd

    @SerializedName("totalStamps")
    val totalStamps: Int,

    @SerializedName("monthlyStamps")
    val monthlyStamps: Int,

    @SerializedName("currentStreak")
    val currentStreak: Int
)
