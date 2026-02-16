package com.teumteumeat.teumteumeat.ui.screen.a4_main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


/**
 * 메인화면 타이틀 바
 */
@Composable
fun MainTopBar(
    currentStreak: Int,
    stampCount: Int,
    onClickSetting: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 19.dp,
                horizontal = 24.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {

        // 🔹 좌측: 로고
        Image(
            painter = painterResource(R.drawable.logo_home),
            contentDescription = "home logo",
            contentScale = ContentScale.None,
            colorFilter = ColorFilter.tint(
                MaterialTheme.extendedColors.textPrimary // ✅ 항상 검정계열
            )
        )

        // 🔥 중앙 1: 연속 학습 스탬프
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.icon_fire_fill),
                contentDescription = "streak icon",
                contentScale = ContentScale.None,
                colorFilter = ColorFilter.tint(
                    if (currentStreak >= 1)
                        MaterialTheme.extendedColors.textPrimary //  검정색
                            // MaterialTheme.extendedColors.error   // 🔴 빨간 불꽃
                    else
                        MaterialTheme.extendedColors.textPrimary // 검정색
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = currentStreak.toString(),
                style = MaterialTheme.appTypography.titleBold20,
            )
        }

        // 📮 중앙 2: 스탬프 개수
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.icon_stamp),
                contentDescription = "stamp icon",
                contentScale = ContentScale.None,
                colorFilter = ColorFilter.tint(
                    MaterialTheme.extendedColors.textPrimary // ✅ 항상 검정계열
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = stampCount.toString(),
                style = MaterialTheme.appTypography.titleBold20,
            )
        }

        // ⚙️ 우측: 설정
        IconButton(
            onClick = onClickSetting,
            modifier = Modifier.size(30.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "setting",
            )
        }
    }
}

@Preview(
    name = "HomeTopBar Preview",
    showBackground = true,
)
@Composable
fun HomeTopBarPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {

                // 🔝 상단 여백 (상태바 영역 가정)
                Spacer(modifier = Modifier.height(24.dp))

                // 🏠 홈 타이틀 바
                MainTopBar(
                    currentStreak = 3,
                    stampCount = 12,
                    onClickSetting = {}
                )

                // 🔽 하단 여백 (아래 콘텐츠가 이어진다는 느낌)
                Spacer(modifier = Modifier.height(32.dp))

                // ⬇️ 더미 콘텐츠 (레이아웃 확인용)
                Text(
                    text = "하단 콘텐츠 영역",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(200.dp))
            }
        }
    }
}
