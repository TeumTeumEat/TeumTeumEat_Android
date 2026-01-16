package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun AddGoalSuccessScreen(
    onStartClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.systemBars) // ✅ SafeArea,
    ) {

        // 🔹 상단 ~ 중앙 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            // 🗨️ 말풍선 이미지
            Image(
                painter = painterResource(id = R.drawable.chat_complete_goal),
                contentDescription = "목표 등록 성공 안내 말풍선",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🐧 캐릭터 이미지
            Image(
                painter = painterResource(id = R.drawable.onboarding_ch_pdf),
                contentDescription = "성공 캐릭터",
                modifier = Modifier.size(260.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

        }

        // 🔵 하단 위치
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
        ) {
            Spacer(Modifier.height(32.dp))
            BaseFillButton(
                text = "시작하기",
                textStyle = MaterialTheme.appTypography.btnBold20_h24.copy(
                    Color.White
                ),
                isEnabled = true,
                onClick = onStartClick,
            )
        }

    }
}


@Preview(
    showBackground = true,
    device = Devices.PIXEL_4
)
@Composable
fun AddGoalSuccessScreenPreview() {
    TeumTeumEatTheme {
        AddGoalSuccessScreen(
            onStartClick = {}
        )
    }
}