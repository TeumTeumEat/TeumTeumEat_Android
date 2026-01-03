package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillSmallButton
import com.teumteumeat.teumteumeat.ui.screen.a0_splash.ErrorState
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContainer(
    onDismiss: () -> Unit,
    titleText: String = "타이틀",
    content: @Composable () -> Unit,
) {
    // ✅ [변경 1] 항상 Expanded로 시작하는 SheetState
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            // ⭐ 핵심: Partial 상태 차단
            newValue != SheetValue.PartiallyExpanded
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState, // ✅ 직접 전달
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {

            // 🔹 헤더 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "닫기",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
            // 🔹 시간 선택 영역 (추후 교체 예정)
            // TimeSliderPlaceholder()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContainerRightTopConfirm(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    titleText: String = "타이틀",
    content: @Composable () -> Unit,
    tittleBottomPadding: Int = 24,
    onCompleteEnable: Boolean = false,
) {
    // ✅ [변경 1] 항상 Expanded로 시작하는 SheetState
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            // ⭐ 핵심: Partial 상태 차단
            newValue != SheetValue.PartiallyExpanded
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState, // ✅ 직접 전달
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(vertical = 15.dp)
        ) {

            // 🔹 헤더 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )

                BaseFillSmallButton(
                    onClick = onConfirm,
                    isEnabled = onCompleteEnable,
                    text = "완료"
                )
            }

            Spacer(modifier = Modifier.height(tittleBottomPadding.dp))

            content()
            // 🔹 시간 선택 영역 (추후 교체 예정)
            // TimeSliderPlaceholder()
        }
    }
}

@Composable
fun FullScreenErrorModal(
    errorState: ErrorState,
    isShowBackBtn: Boolean = true,
    onBack: () -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.systemBars) // ✅ SafeArea,
    ) {
        // 🔙 뒤로가기
        if (isShowBackBtn) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = {
                        onBack()
                    },

                    ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "previous page",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(0.dp),
                    )
                }

            }
        }

        // 본문
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 아이콘/로고 자리
            // Icon(...) 또는 Image(...)

            androidx.compose.material3.Text(
                text = errorState.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            androidx.compose.material3.Text(
                text = errorState.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


        }

        // 이동 버튼
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            BaseFillButton(
                onClick = errorState.onRetry,
                text = errorState.retryLabel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ModalBottomSheetPreview() {

    // Preview에서는 항상 열린 상태로
    var showSheet by remember { mutableStateOf(false) }

    TeumTeumEatTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { showSheet = true }) {
                Text("시간 선택 열기")
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    // ✅ 상단 드래그 핸들 제거
                    dragHandle = null
                ) {
                    BottomSheetContainer(
                        onDismiss = { showSheet = false },
                        titleText = "집을 나오는 시간",
                        content = {}
                    )
                }
            }
        }
    }
}

