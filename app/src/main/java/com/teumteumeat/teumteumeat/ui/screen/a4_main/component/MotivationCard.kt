package com.teumteumeat.teumteumeat.ui.screen.a4_main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

data class MotivationUiState(
    val streakCount: Int = 0,
    val title: String = "얼른 시작해 틈틈잇",
    val backgroundColor: Color = Color(0xFFEFF6FF),
    val leftIconRes: Int = R.drawable.icon_fire_fill,
    val characterImageRes: Int = R.drawable.char_streak_zero,
)

fun mapStreakToMotivationUiState(
    streak: Int,
    isStreakBroken: Boolean,
): MotivationUiState {

    return if(isStreakBroken){
        MotivationUiState(
            streakCount = 0,
            title = "얼른 시작해 틈틈잇",
            leftIconRes = R.drawable.icon_fire_fill,
            characterImageRes = R.drawable.char_streak_rezero,
            backgroundColor = Color(0xFFEFF6FF),
        )
    }else{
        when {
            streak <= 0 -> MotivationUiState(
                streakCount = 0,
                title = "얼른 시작해 틈틈잇",
                leftIconRes = R.drawable.icon_fire_fill,
                characterImageRes = R.drawable.char_streak_zero,
                backgroundColor = Color(0xFFEFF6FF),
            )

            streak < 7 -> MotivationUiState(
                streakCount = streak,
                title = "시작이 반이다",
                leftIconRes = R.drawable.icon_fire_fill,
                characterImageRes = R.drawable.char_streak_day,
                backgroundColor = Color(0xFFEFF6FF),
            )

            streak < 30 -> MotivationUiState(
                streakCount = streak,
                title = "일주일 연속 달성!",
                leftIconRes = R.drawable.icon_fire_fill,
                characterImageRes = R.drawable.char_streak_week,
                backgroundColor = Color(0xFFEFF6FF),
            )

            else -> MotivationUiState(
                streakCount = streak,
                title = "한 달 연속 달성!",
                leftIconRes = R.drawable.icon_fire_fill,
                characterImageRes = R.drawable.char_streak_month,
                backgroundColor = Color(0xFFEFF6FF),
            )
        }
    }
}


@Composable
fun MotivationCard(
    modifier: Modifier = Modifier,
    uiState: MotivationUiState,
) {
    val theme = MaterialTheme.extendedColors
    val contentColor = if(uiState.streakCount < 1) theme.textSecondary
        else theme.errorSecondary
    val typo = MaterialTheme.appTypography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = uiState.backgroundColor,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ⬅️ 왼쪽 영역 (streak + text)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {

                // 🔥 streak 영역 (128 * 66 기준)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = uiState.leftIconRes),
                        contentDescription = "",
                        tint = contentColor,
                        modifier = Modifier.size(50.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = uiState.streakCount.toString(),
                        style = typo.rodies_body_40.copy(contentColor)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 📝 텍스트 영역 (128 * 42 기준)
                Text(
                    text = uiState.title,
                    style = typo.subtitleSemiBold16
                )
            }

            // ➡️ 캐릭터 이미지 (높이가 카드 높이를 결정)
            Image(
                painter = painterResource(id = uiState.characterImageRes),
                contentDescription = null,
                modifier = Modifier.weight(1f),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MotivationStreakCardPreview_Zero() {
    MotivationCard(
        uiState = mapStreakToMotivationUiState(
            streak = 0,
            isStreakBroken = false
        )
    )
}

