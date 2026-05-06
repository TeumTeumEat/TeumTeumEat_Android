package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.appTypography

/**
 * 온보딩/목표 추가 플로우 공용 상단 진행 표시줄.
 *
 * - [currentPage] == 0 이면 전체 행을 숨김
 * - 뒤로 아이콘은 [currentPage] > 0 일 때만 클릭 가능 (투명도 애니메이션)
 *
 * @param currentPage 현재 페이지 (1-based)
 * @param totalPage   전체 페이지 수
 * @param onBack      뒤로 아이콘 클릭 콜백
 * @param modifier    외부 Modifier
 */
@Composable
fun FlowTopProgressBar(
    currentPage: Int,
    totalPage: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (currentPage <= 0) return

    Row(
        modifier = modifier.padding(vertical = 16.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SizeAnimationInvisible(
            isVisible = currentPage > 0,
            clickEnabled = currentPage > 0,
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_keboard_arrow_left),
                contentDescription = "이전 페이지",
                modifier = Modifier.clickable(
                    interactionSource = Utils.UiUtils.noRipple(),
                    indication = null,
                    onClick = onBack,
                ),
            )
        }

        CustomProgressBar(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            currentStep = currentPage,
            totalSteps = totalPage,
        )

        Text(
            text = "$currentPage/$totalPage",
            style = MaterialTheme.appTypography.captionRegular14,
            maxLines = 1,
            softWrap = false,
        )
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "진행바 - 1/5 페이지")
@Composable
private fun FlowTopProgressBar_Page1Preview() {
    TeumTeumEatTheme {
        FlowTopProgressBar(
            currentPage = 1,
            totalPage = 5,
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "진행바 - 3/5 페이지")
@Composable
private fun FlowTopProgressBar_Page3Preview() {
    TeumTeumEatTheme {
        FlowTopProgressBar(
            currentPage = 3,
            totalPage = 5,
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "진행바 - 5/5 마지막 페이지")
@Composable
private fun FlowTopProgressBar_LastPagePreview() {
    TeumTeumEatTheme {
        FlowTopProgressBar(
            currentPage = 5,
            totalPage = 5,
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "진행바 - 0페이지 (숨김)")
@Composable
private fun FlowTopProgressBar_Page0Preview() {
    TeumTeumEatTheme {
        FlowTopProgressBar(
            currentPage = 0,
            totalPage = 5,
            onBack = {},
        )
    }
}