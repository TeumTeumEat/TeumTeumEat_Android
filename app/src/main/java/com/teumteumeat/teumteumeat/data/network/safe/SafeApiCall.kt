package com.teumteumeat.teumteumeat.data.network.safe

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse

suspend fun <T> safeApiCall(block: suspend () -> ApiResponse<T>): Result<ApiResponse<T>> =
    runCatching { block() }
