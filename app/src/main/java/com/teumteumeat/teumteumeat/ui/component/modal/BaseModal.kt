package com.teumteumeat.teumteumeat.ui.component.modal

import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.FillSecondaryButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun BaseModal(
    title: String,
    body: String? = null,
    primaryButtonText: String,
    secondaryButtonText: String? = null,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: (() -> Unit)? = null,
) {

    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 10.dp, // Blur ≈ 10
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.extendedColors.modalShadow,
                spotColor = MaterialTheme.extendedColors.modalShadow,
            )
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔹 Title
            Text(
                text = title,
                style = MaterialTheme.appTypography.titleSemiBold24
            )

            // 🔹 Body (선택)
            body?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.appTypography.bodyMedium14Reg
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Buttons
            if (secondaryButtonText != null && onSecondaryClick != null) {
                // 버튼 2개 케이스
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FillSecondaryButton(
                        text = secondaryButtonText,
                        modifier = Modifier.weight(1f),
                        onClick = onSecondaryClick,
                        conerRadius = 16.dp,
                    )

                    BaseFillButton(
                        text = primaryButtonText,
                        modifier = Modifier.weight(1f),
                        conerRadius = 16.dp,
                        onClick = onPrimaryClick,

                        )
                }
            } else {
                // 버튼 1개 케이스
                BaseFillButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = primaryButtonText,
                    onClick = onPrimaryClick,
                    conerRadius = 16.dp
                )
            }
        }
    }
}

@Preview(
    name = "BaseModal - Single Button",
    showBackground = true
)
@Composable
fun BaseModalSingleButtonPreview() {
    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BaseModal(
                title = "알림",
                body = "변경사항이 저장되었습니다.",
                primaryButtonText = "확인",
                onPrimaryClick = {}
            )

            BaseModal(
                title = "삭제 확인",
                body = "정말 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.",
                primaryButtonText = "삭제",
                secondaryButtonText = "취소",
                onPrimaryClick = {},
                onSecondaryClick = {}
            )

            BaseModal(
                title = "네트워크 오류",
                body = null,
                primaryButtonText = "다시 시도",
                onPrimaryClick = {}
            )
        }
    }
}




