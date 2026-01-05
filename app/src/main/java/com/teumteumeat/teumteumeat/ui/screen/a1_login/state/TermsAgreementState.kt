package com.teumteumeat.teumteumeat.ui.screen.a1_login.state

data class TermsAgreementState(
    val over14: Boolean = false,
    val termsOfService: Boolean = false,
    val privacyPolicy: Boolean = false,
) {
    val allRequiredAgreed: Boolean
        get() = over14 && termsOfService && privacyPolicy
}
