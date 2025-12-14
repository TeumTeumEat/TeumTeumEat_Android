package com.teumteumeat.teumteumeat.ui.screen.a4_main.component

// Compose 기본
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

// 애니메이션
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween

// 레이아웃
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

// 클릭 (Ripple 제거 핵심)
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource

// UI 스타일
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape

// 리소스
import androidx.compose.ui.res.painterResource

// Modifier / 단위
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 색상
import androidx.compose.ui.graphics.Color

// Material3
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import com.teumteumeat.teumteumeat.ui.screen.a4_main.BottomNavItem


@Composable
fun CustomBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isHomeItem = item.route == BottomNavItem.Home.route

    // ⭐ 배경 원 크기 애니메이션
    val animatedSize by animateDpAsState(
        targetValue = if (isHomeItem && isSelected) 80.dp else 52.dp,
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        label = "home_icon_box_size"
    )

    // ⭐ 아이콘 크기 애니메이션
    val animatedIconSize by animateDpAsState(
        targetValue = if (isHomeItem && isSelected) 52.dp else 30.dp,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "icon_size"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clickable(
                indication = null, // ✅ Ripple 완전 제거
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(animatedSize)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.label,
                tint = if (isSelected)
                    Color.White
                else
                    Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(animatedIconSize)
            )
        }
    }
}
