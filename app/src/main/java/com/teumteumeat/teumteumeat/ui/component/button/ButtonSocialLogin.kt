package com.teumteumeat.teumteumeat.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun KakaoLoginButton(
    @DrawableRes logoRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val kakaoYellow = Color(0xFFFEE500)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(kakaoYellow)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🔹 카카오 로고
            SocialLoginLogo(
                logoRes = logoRes,
                contentDescription = "Kakao Logo",
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 🔹 텍스트
            Text(
                text = "카카오계정 로그인",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF121212)
            )
        }
    }
}


@Composable
fun SocialLoginLogo(
    @DrawableRes logoRes: Int,
    contentDescription: String,
) {
    Image(
        painter = painterResource(id = logoRes),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit
    )
}