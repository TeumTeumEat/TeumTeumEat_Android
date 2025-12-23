package com.teumteumeat.teumteumeat.data.api.user

data class UpdateNameRequest(
    val name: String
){
    fun toDomain(): UpdateNameRequest {
        return UpdateNameRequest(
            name = name
        )
    }
}