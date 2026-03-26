package com.teumteumeat.teumteumeat.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography

@Composable
fun NoLableTextField(
    modifier: Modifier = Modifier,
    value: String,
    labelText: String = "",
    placeholderText: String,
    onValueChange: (String) -> Unit,
    showCharCount: Boolean = true, // ← 글자 수 표시 ON/OFF
    seenMaxLength: Int = 10,
    rearMaxLength: Int = Int.MAX_VALUE,
    isFocused: Boolean,
    focusRequesterThis: FocusRequester,
    interactionSource: MutableInteractionSource,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    isOneLine: Boolean = true,
    onDone: KeyboardActionScope.() -> Unit = {}
) {
    val containerColor = if (!isError) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.error

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp) // 원하는 높이 지정
            .border(
                width = 1.dp,
                color = containerColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 25.dp)
    ) {
        // label 고정
        Text(
            text = labelText,
            style = TextStyle(fontSize = 14.sp, color = Color.Gray),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        // placeholder (입력 전 중앙)
        if (value.isEmpty()) {
            Text(
                text = placeholderText,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            BasicTextField(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequesterThis),
                value = value,
                onValueChange = { text ->
                    if (text.length <= rearMaxLength) onValueChange(text)
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Start
                ),
                singleLine = true,
                interactionSource = interactionSource,
            )

            if (showCharCount) {
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${value.length}/$seenMaxLength",
                    fontSize = 12.sp,
                    color = containerColor
                )
            }
        }

        /*// 입력 텍스트
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequesterThis),
            value = value,
            onValueChange = { text ->
                if (text.length <= rearMaxLength) onValueChange(text)
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            ),
            singleLine = isOneLine,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            interactionSource = interactionSource,
            keyboardActions = KeyboardActions(
                onDone = {
                    this.onDone()
                }
            )

        )

        // 글자 수 표시 (옵션)
        if (showCharCount) {
            Text(
                text = "${value.length}/$seenMaxLength",
                style = TextStyle(fontSize = 12.sp, color = containerColor),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            )
        }*/
    }
}


@Composable
fun NoLableMultiLineTextField(
    modifier: Modifier = Modifier,
    value: String,
    labelText: String = "",
    placeholderText: String,
    onValueChange: (String) -> Unit,
    showCharCount: Boolean = true, // ← 글자 수 표시 ON/OFF
    minLines: Int = 2,                 // ✅ 기본 2줄
    maxLines: Int = 4,     // ✅ 2줄 이상 입력 허용
    maxLength: Int = 30,
    isFocused: Boolean,
    focusRequesterThis: FocusRequester,
    interactionSource: MutableInteractionSource,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    isOneLine: Boolean = false,
) {
    val containerColor = if (!isError) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.error

    Box(
        // contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(
                width = 1.dp,
                color = containerColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(all = 18.dp)
    ) {
        // label 고정
        Text(
            text = labelText,
            style = TextStyle(fontSize = 14.sp, color = Color.Gray),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        // 🔹 Placeholder (중앙 정렬)
        if (value.isEmpty()) {
            Text(
                text = placeholderText,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

        // 🔹 입력 텍스트 (좌상단부터 시작)
        BasicTextField(
            value = value,
            onValueChange = { text ->
                if (text.length <= maxLength) {
                    onValueChange(text)
                }
            },
            textStyle = MaterialTheme.appTypography.bodyMedium16.copy(
                textAlign = TextAlign.Start,
                lineHeight = 22.sp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(top = if (labelText.isNotEmpty()) 24.dp else 0.dp)
                .focusRequester(focusRequesterThis),
            singleLine = false,
            minLines = minLines,
            maxLines = maxLines,
        )

        // 🔹 글자 수 표시
        if (value.isNotEmpty()) {
            Text(
                text = "${value.length}/$maxLength",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = containerColor
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun NoLableTextFieldPreview() {
    var text by remember { mutableStateOf("") }

    val focusRequesterInput = remember { FocusRequester() } // 이름 인풋 필드 포커스 요청자
    val inputInteractionSource =
        remember { MutableInteractionSource() } // 사용자가 특정 UI 요소와 상호작용하고 있는지를 감지하는 객체
    val inputFocused by inputInteractionSource.collectIsFocusedAsState() // ✅ 포커스 여부 감지

    val focusManager = LocalFocusManager.current

    var materialTheme = MaterialTheme.colorScheme
    TeumTeumEatTheme {
        // 뒤로가기 버튼 처리
        BackHandler(enabled = inputFocused) {
            focusManager.clearFocus() // 포커스 해제
        }
        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
            color = materialTheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        // 클릭 시 키보드 및 포커스 해제
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus() // 포커스 해제
                    }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NoLableTextField(
                    value = text,
                    labelText = "",
                    placeholderText = "입력해주세요.",
                    onValueChange = {
                        text = it // viewModel set 함수 위치
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isFocused = inputFocused,
                    focusRequesterThis = focusRequesterInput,
                    interactionSource = inputInteractionSource,
                    onDone = { focusManager.clearFocus() }
                )

                NoLableMultiLineTextField(
                    value = text,
                    labelText = "",
                    placeholderText = "상황설정 예시가 필요합니다.\n" +
                            "어떤 식으로 할지 어떤 상황인지 입력해주세요\n"+
                            "ex) IT 트렌드나 프로그래밍 관련 퀴즈를 \n풀고 싶어요",
                    onValueChange = {
                        text = it // viewModel set 함수 위치
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isFocused = inputFocused,
                    focusRequesterThis = focusRequesterInput,
                    interactionSource = inputInteractionSource,
                )
            }

        }
    }
}