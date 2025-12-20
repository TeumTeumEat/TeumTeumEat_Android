package com.teumteumeat.teumteumeat.data.repository.login

sealed class AutoLogin {
    object Success : AutoLogin()
    data class Fail(val message: String) : AutoLogin()
}
