package com.teumteumeat.teumteumeat.domain.model.on_boarding

sealed class NameUpdateError {

    data object None : NameUpdateError()

    data class Message(
        val message: String
    ) : NameUpdateError()

    /** 기존 서버/네트워크/알 수 없는 오류 */
    data class CommonMessage(
        val message: String
    ) : NameUpdateError()

    data class Validation(
        val messages: List<String>
    ) : NameUpdateError()
}
