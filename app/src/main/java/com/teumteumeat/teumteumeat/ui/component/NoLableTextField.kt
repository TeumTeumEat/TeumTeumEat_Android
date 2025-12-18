package com.teumteumeat.teumteumeat.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

@Composable
fun NoLableTextField(
    value: String,
    labelText: String,
    placeholderText: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    showCharCount: Boolean = true, // ← 글자 수 표시 ON/OFF
    maxLength: Int = 10,
    isFocused: Boolean,
    focusRequesterThis: FocusRequester,
    interactionSource: MutableInteractionSource,
    keyboardType: KeyboardType = KeyboardType.Text,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp) // 원하는 높이 지정
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(30.dp)
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
                style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 입력 텍스트
        BasicTextField(
            value = value,
            onValueChange = { text ->
                if (text.length <= maxLength) onValueChange(text)
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequesterThis),
        )

        // 글자 수 표시 (옵션)
        if (showCharCount) {
            Text(
                text = "${value.length}/$maxLength",
                style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
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
                verticalArrangement = Arrangement.Center
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
                )
            }
        }
    }
}