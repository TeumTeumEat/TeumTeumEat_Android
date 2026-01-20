package com.teumteumeat.teumteumeat.data.network.model_request

import com.google.gson.annotations.SerializedName

data class UpdateUserSettingRequest(
    @SerializedName("pushEnabled")
    val pushEnabled: Boolean
)
