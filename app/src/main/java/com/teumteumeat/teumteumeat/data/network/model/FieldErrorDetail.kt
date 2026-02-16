package com.teumteumeat.teumteumeat.data.network.model

data class FieldErrorDetail(
    val field: String,
    val message: String
){
    fun toDomain(): FieldErrorDetail {
        return FieldErrorDetail(
            field = field,
            message = message
        )
    }
}
