package com.teumteumeat.teumteumeat.ui.component.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginUiState
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun TermsAgreementBottomSheetContent(
    uiState: LoginUiState,
    onOver14Checked: (Boolean) -> Unit,
    onTermsOfServiceChecked: (Boolean) -> Unit,
    onPrivacyPolicyChecked: (Boolean) -> Unit,
    onAllChecked: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onGoServiceAgreeWebView: () -> Unit,
    onGoPrivacyPolicyWebView: () -> Unit,
) {
    val agreement = uiState.termsAgreement

    val textPointBlue = MaterialTheme.extendedColors.textPointBlue
    val textGoastColor = MaterialTheme.extendedColors.textGhost

    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            AgreementItem(
                checked = agreement.over14,
                text = "만 14세 이상 가입 동의 (필수)",
                onCheckedChange = onOver14Checked,
                content = {
                    Text(
                        modifier = Modifier.clickable(
                            onClick = {
                                onOver14Checked(!agreement.over14)
                            }
                        ),
                        text = buildAnnotatedString {
                            withStyle(MaterialTheme.appTypography.bodyMedium16.toSpanStyle()) {
                                append("만 14세 이상 가입 동의 ")
                            }

                            withStyle(
                                MaterialTheme.appTypography.bodyMedium14_20
                                    .toSpanStyle()
                                    .copy(color = MaterialTheme.extendedColors.textGhost)
                            ) {
                                append("(필수)")
                            }
                        },
                    )
                }
            )

            AgreementItem(
                checked = agreement.termsOfService,
                text = "이용약관 (필수)",
                isLink = true,
                onCheckedChange = onTermsOfServiceChecked,
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.clickable(
                                onClick = onGoServiceAgreeWebView
                            ),
                            text = "이용약관 ",
                            style = MaterialTheme.appTypography.bodyMedium16.copy(
                                color = textPointBlue
                            )
                        )
                        Text("(필수)",
                            style = MaterialTheme.appTypography.bodyMedium14.copy(
                            color = MaterialTheme.extendedColors.textGhost
                        ))
                    }
                }
            )

            AgreementItem(
                checked = agreement.privacyPolicy,
                isLink = true,
                onCheckedChange = onPrivacyPolicyChecked,
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.clickable(
                                onClick = {
                                    onGoPrivacyPolicyWebView()
                                }
                            ),
                            text = "개인정보처리방침 ",
                            style = MaterialTheme.appTypography.bodyMedium16.copy(
                                color = textPointBlue
                            )
                        )
                        Text("(필수)",
                            style = MaterialTheme.appTypography.bodyMedium14.copy(
                                color = MaterialTheme.extendedColors.textGhost
                            ))
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            AgreementItem(
                checked = agreement.allRequiredAgreed,
                onCheckedChange = onAllChecked,
                content = {
                    Text("전체동의",
                        style = MaterialTheme.appTypography.bodyMedium14_20
                    )
                }
            )


        }
    }
}

