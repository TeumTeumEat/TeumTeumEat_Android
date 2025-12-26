package com.teumteumeat.teumteumeat.data.network.model

sealed class DomainError {

    data object None : DomainError()

    data class Message(
        val message: String
    ) : DomainError()

    data class FieldErrors(
        val errors: List<FieldErrorDetail>
    ) : DomainError()
}

