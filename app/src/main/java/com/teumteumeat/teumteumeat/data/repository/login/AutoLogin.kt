package com.teumteumeat.teumteumeat.data.repository.login

sealed class AutoLogin {
    /** ✅ 자동 로그인 성공 */
    object Success : AutoLogin()

    /** ❌ 일반 실패 (토스트만 보여주면 됨) */
    data class Fail(
        val message: String
    ) : AutoLogin()

    /** 🔒 세션 만료 → 반드시 로그인 화면으로 이동 */
    data class SessionExpired(
        val message: String
    ) : AutoLogin()

    data class NetWorkError(
        val message: String
    ) : AutoLogin()
}
