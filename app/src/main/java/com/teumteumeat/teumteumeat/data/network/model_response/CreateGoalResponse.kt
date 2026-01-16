package com.teumteumeat.teumteumeat.data.network.model_response

import com.google.gson.annotations.SerializedName

data class CreateGoalResponse(
    @SerializedName("id")
    val goalId: Int,
)
