package com.teumteumeat.teumteumeat.data.network.model

data class ApiResponse<T>(
    val code: String?,
    val message: String?,
    val details: String?,
    val data: T
)