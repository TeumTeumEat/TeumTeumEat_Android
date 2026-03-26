package com.teumteumeat.teumteumeat.data.network.model_request.user

import com.google.gson.annotations.SerializedName

data class GetUserSettingRequest(
    @SerializedName("pushEnabled")
    val pushEnabled: Boolean
)