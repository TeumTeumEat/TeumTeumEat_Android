package com.teumteumeat.teumteumeat.data.network.model


// 2️⃣ DomainError: 서버의 복잡한 details를 3가지 상황으로 압축
sealed class DomainError {
    /** details == null 이거나 일반 실패 (Code/Message만으로 충분할 때) */
    data object None : DomainError()

    /** 서버가 details를 단순 String 메시지로 줬을 때 (특수 팝업 등) */
    data class Message(
        val message: String
    ) : DomainError()

    /** details가 List 형태의 검증 오류일 때 (회원가입, 폼 입력 등) */
    data class Validation(
        val errors: List<FieldErrorDetail>
    ) : DomainError()
}

