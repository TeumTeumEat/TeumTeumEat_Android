package com.teumteumeat.teumteumeat.ui.component

// ui/component/RequestPromptOptionList.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.domain.model.RequestPromptOption
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

/**
 * 요청 프롬프트 선택 리스트
 *
 * @param options         표시할 선택지 목록
 * @param selectedId      현재 선택된 항목의 id (null = 미선택)
 * @param onSelect        항목 클릭 시 콜백 — 선택된 [RequestPromptOption] 전달
 * @param modifier        외부 Modifier
 * @param scrollbarTrackColor 스크롤바 트랙(배경) 색상
 * @param scrollbarThumbColor 스크롤바 썸(핸들) 색상
 */
@Composable
fun RequestPromptOptionList(
    options: List<RequestPromptOption>,
    selectedId: String?,
    onSelect: (RequestPromptOption) -> Unit,
    modifier: Modifier = Modifier,
    scrollbarTrackColor: Color = Color(0xFFEEEEEE),
    scrollbarThumbColor: Color = Color(0xFFBDBDBD),
) {
    // 모달이 처음 표시될 때의 selectedId 기준으로 선택 항목을 상단 고정.
    // 탭 중에 목록이 흔들리지 않도록 options가 바뀌지 않는 한 재정렬하지 않음.
    val initialSelectedId = remember { selectedId }
    val sortedOptions = remember(options) {
        if (initialSelectedId == null) options
        else {
            val selected = options.filter { it.id == initialSelectedId }
            val rest = options.filter { it.id != initialSelectedId }
            selected + rest
        }
    }

    val listState = rememberLazyListState()

    val rawScrollFraction by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            val visible = info.visibleItemsInfo
            if (visible.isEmpty() || total == 0 || visible.size >= total) 0f
            else visible.first().index.toFloat() / (total - visible.size).coerceAtLeast(1)
        }
    }
    val animatedScrollFraction by animateFloatAsState(
        targetValue = rawScrollFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "scrollbarFraction",
    )

    val thumbHeightFraction by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val viewport = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
            val visible = info.visibleItemsInfo
            if (viewport <= 0f || visible.isEmpty() || info.totalItemsCount == 0) return@derivedStateOf 1f
            val first = visible.first()
            val last = visible.last()
            val span = (last.offset + last.size - first.offset).toFloat()
            val count = (last.index - first.index + 1).toFloat()
            val avgItem = span / count
            val totalContent = avgItem * info.totalItemsCount
            (viewport / totalContent).coerceIn(0f, 1f)
        }
    }
    val animatedThumbFraction by animateFloatAsState(
        targetValue = thumbHeightFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "scrollbarThumbFraction",
    )

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .drawVerticalScrollbar(animatedScrollFraction, animatedThumbFraction, scrollbarTrackColor, scrollbarThumbColor)
            .padding(end = 30.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = sortedOptions,
            key = { it.id },
        ) { option ->
            RequestPromptOptionItem(
                option = option,
                isSelected = option.id == selectedId,
                onSelect = onSelect,
            )
        }
    }
}

private fun Modifier.drawVerticalScrollbar(
    animatedScrollFraction: Float,
    animatedThumbFraction: Float,
    trackColor: Color = Color(0xFFEEEEEE),
    thumbColor: Color = Color(0xFFBDBDBD),
): Modifier = drawWithContent {
    drawContent()
    if (animatedThumbFraction >= 1f) return@drawWithContent

    val thumbWidth = 4.dp.toPx()
    val trackWidth = 2.dp.toPx()
    val trackHeight = size.height
    val thumbHeight = (trackHeight * animatedThumbFraction).coerceAtLeast(32.dp.toPx())
    val rightEdge = size.width - 16.dp.toPx()
    val thumbCenterX = rightEdge - thumbWidth / 2f
    val thumbOffsetY = ((trackHeight - thumbHeight) * animatedScrollFraction)
        .coerceIn(0f, trackHeight - thumbHeight)

    drawRoundRect(
        color = trackColor,
        topLeft = Offset(thumbCenterX - trackWidth / 2f, 0f),
        size = Size(trackWidth, trackHeight),
        cornerRadius = CornerRadius(trackWidth / 2f),
    )
    drawRoundRect(
        color = thumbColor,
        topLeft = Offset(thumbCenterX - thumbWidth / 2f, thumbOffsetY),
        size = Size(thumbWidth, thumbHeight),
        cornerRadius = CornerRadius(thumbWidth / 2f),
    )
}

/**
 * 단일 요청 프롬프트 선택 항목
 *
 * @param option     표시할 선택지
 * @param isSelected 선택 여부 — true 시 파란 배경/텍스트 적용
 * @param onSelect   클릭 콜백
 */
@Composable
private fun RequestPromptOptionItem(
    option: RequestPromptOption,
    isSelected: Boolean,
    onSelect: (RequestPromptOption) -> Unit,
) {
    val theme = MaterialTheme.extendedColors

    val backgroundColor = if (isSelected) {
        theme.btnFillSecondary
    } else {
        theme.btnGray100
    }

    val textColor = if (isSelected) {
        theme.textPointBlue
    } else {
        theme.textGhost
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onSelect(option) }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = option.label,
            style = MaterialTheme.appTypography.bodyMedium16.copy(
                lineHeight = 22.sp
            ),
            color = textColor,
        )
    }
}

@Preview(showBackground = true, name = "요청 프롬프트 리스트 프리뷰")
@Composable
private fun Preview() {
    // 1. 테스트용 가상 데이터 리스트
    val mockOptions = listOf(
        RequestPromptOption("1", "📝 요약해 주세요"),
        RequestPromptOption("2", "💡 아이디어를 제안해 주세요"),
        RequestPromptOption("3", "🔍 문법 오류를 찾아 주세요"),
        RequestPromptOption("4", "🎨 스타일을 더 화려하게 바꿔 주세요"),
        RequestPromptOption("5", "🌐 다른 언어로 번역해 주세요")
    )

    // 2. 클릭 동작을 확인하기 위한 선택 상태 관리
    var selectedId by remember { mutableStateOf<String?>(null) }

    // 3. 실제 컴포넌트 호출
    TeumTeumEatTheme {
        RequestPromptOptionList(
            modifier = Modifier.padding(16.dp), // 프리뷰 여백
            options = mockOptions,
            selectedId = selectedId,
            onSelect = { clickedOption ->
                // 클릭 시 상태 업데이트 -> UI 리컴포지션 발생
                selectedId = clickedOption.id
            },

        )
    }
}