package com.teumteumeat.teumteumeat.ui.component.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.component.CheckToggleButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun MyPageArrowRow(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}, // 기본값
) {
    MyPageRow(
        modifier = modifier
            .clickable { onClick() },
        title = title,
        rightContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    )
}

@Composable
fun MyPageAccountSection(
    title: String,
    modifier: Modifier = Modifier,
    providerName: String,
    email: String,
    providerIcon: @Composable () -> Unit,
) {
    val theme = MaterialTheme.extendedColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.backgroundW100)
    ) {

        // 🔹 섹션 타이틀
        MyPageRow(
            title = title
        )


        // 🔹 계정 정보 카드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .background(
                    color = theme.btnGray100,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {

            Column {

                // 로그인 타입
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    providerIcon()

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = providerName,
                        style = MaterialTheme.appTypography.bodyMedium14,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 이메일
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "이메일",
                        style = MaterialTheme.appTypography.lableMedium12_h14,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = email,
                        style = MaterialTheme.appTypography.captionRegular12,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun MyPageRow(
    modifier: Modifier = Modifier,
    title: String,
    rightContent: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.appTypography.bodyMedium16.copy(
        lineHeight = 22.sp
    ),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.extendedColors.backgroundW100)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 20.dp)
                .background(MaterialTheme.extendedColors.backgroundW100),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = textStyle,
                color = Color.Black
            )

            rightContent?.invoke()
        }
    }
}

@Composable
fun MyPageTitleRow(
    title: String,
) {
    MyPageRow(
        title = title
    )
}

@Composable
fun MyPageToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    MyPageRow(
        modifier = modifier,
        title = title,
        rightContent = {
            CheckToggleButton(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    )
}

@Composable
fun MyPageNavigateBox(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}, // ✅ 기본값
) {

    val theme = MaterialTheme.extendedColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 20.dp)
    ) {
        MyPageRow(
            title = title,
            rightContent = {
                Row(
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = onClick
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "전체 보기",
                        style = MaterialTheme.appTypography.lableMedium12_h14,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }

        )

        content()
    }
}

@Composable
fun SelectedTopicSection(
    modifier: Modifier = Modifier,
    title: String = "선택된 주제",
    topic: String,
    description: String,
    goalWeek: String,
    difficulty: String,
    isSelGoalExpired: Boolean, // 이 상태값에 따라 오버레이 노출
) {
    val theme = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth() // 너비 가득 채우기 추가
    ) {
        // 1. 기본 컨텐츠 카드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = theme.primary, // 선택된 주제이므로 항상 파란색 테두리
                    shape = shape
                )
                .background(
                    color = theme.primaryContainer,
                    shape = shape
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.appTypography.subtitleSemiBold16,
                        color = theme.primary
                    )

                    if (!isSelGoalExpired){
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TagChip(text = goalWeek)
                            TagChip(text = difficulty)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = topic,
                    style = MaterialTheme.appTypography.bodyMedium16.copy(
                        lineHeight = 22.sp
                    )
                )

                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.appTypography.lableMedium12_h14,
                        color = Color.Gray
                    )
                }
            }
        }

        // 2. 만료 시 반투명 검정 오버레이 (GoalCard와 동일한 로직)
        if (isSelGoalExpired) {
            Box(
                modifier = Modifier
                    .matchParentSize() // 부모 Box 크기에 맞춤
                    .clip(shape)
                    .background(color = Color.Black.copy(alpha = 0.4f)) // 40% 투명도
                    .border(
                        width = 2.dp,
                        color = theme.primary, // 오버레이 위에도 파란색 테두리 유지
                        shape = shape
                    )
            ) {
                // 상단 우측 '만료된 주제' 표시
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(horizontal = 16.dp)
                        .padding(top = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cancel,
                        contentDescription = null,
                        tint = theme.textOnError,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "만료된 주제",
                        style = MaterialTheme.appTypography.captionRegular14.copy(
                            color = theme.textOnError
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val theme = MaterialTheme.extendedColors

    Box(
        modifier = modifier
            .background(
                color = theme.primary,
                shape = RoundedCornerShape(999.dp) // 완전한 pill 형태
            )
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.appTypography.lableMedium12_h14,
            color = Color.White
        )
    }
}


@Preview(
    name = "MyPageScreen - Gallery",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
fun MyPageScreenGalleryPreview() {
    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // 🔹 Title Row
            MyPageTitleRow(title = "Title")

            Divider()

            // 🔹 Toggle OFF 상태
            MyPageToggleRow(
                title = "알림 받기",
                checked = false,
                onCheckedChange = {},
            )

            Divider()

            // 🔹 Toggle ON 상태
            MyPageToggleRow(
                title = "알림 받기",
                checked = true,
                onCheckedChange = {},
            )

            Divider()

            // 🔹 Navigate Row
            MyPageNavigateBox(
                title = "Title",
                onClick = {},
            )
        }
    }
}

