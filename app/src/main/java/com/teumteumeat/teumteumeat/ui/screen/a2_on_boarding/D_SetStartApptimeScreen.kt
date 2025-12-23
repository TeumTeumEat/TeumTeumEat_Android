package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onesignal.OneSignal
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.component.BottomSheetContainer
import com.teumteumeat.teumteumeat.ui.component.CheckBoxCircle
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.MinuteRadioGroup
import com.teumteumeat.teumteumeat.ui.component.TimeSliderWithPickTime
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.ui.theme.Typography


@Composable
fun OnBoardingSetUsingApptimeScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnBoardingMain,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    val isSetAllTimeValid = uiState.isSetWorkInTime && uiState.isSetWorkOutTime

    /**
     * 🔹 화면 최초 진입 시
     * 🔹 권한 상태만 확인 (팝업 ❌)
     */
    LaunchedEffect(Unit) {
        val granted = OneSignal.Notifications.permission
        viewModel.syncNotificationPermission(granted)
    }


    // 🔔 권한 팝업은 "이 상태가 true일 때만" 실행
    LaunchedEffect(uiState.requestNotificationPermission) {
        if (uiState.requestNotificationPermission) {
            // 1️⃣ 시스템 권한 팝업 호출
            OneSignal.Notifications.requestPermission(true)

            // 2️⃣ 현재 권한 상태 확인
            val granted = OneSignal.Notifications.permission

            // 3️⃣ ViewModel에 결과 전달
            viewModel.onNotificationPermissionResult(granted)

            // 이벤트 소비
            viewModel.consumeNotificationPermissionRequest()
        }
    }

    DefaultMonoBg(
        color = MaterialTheme.colorScheme.surface,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))
                    Text(
                        "틈틈잇 몇 분 이용할 건지",
                        style = Typography.headlineMedium.copy(
                            fontSize = 18.sp,
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Image(
                        painter = painterResource(R.drawable.character_front),
                        contentDescription = "앞을 보는 케릭터",
                        modifier = Modifier.size(width = 200.dp, height = 162.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(25.dp))

                    MinuteRadioGroup(
                        options = listOf(5, 7, 10, 15),
                        selectedMinute = uiState.selectedMinute,
                        onSelect = { viewModel.onMinuteSelected(it) }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = Typography.labelMedium.copy(
                            lineHeight = 24.sp
                        ),
                        isEnabled = uiState.selectedMinute != null,
                        onClick = {
                            viewModel.updateCommuteTime()
                            onNext()
                        }
                    )
                }
            }
        },
    )
}



@Preview(showBackground = true)
@Composable
fun OnBoardingSetUsingApptimeScreen() {

    val fakeViewModel : OnBoardingViewModel = hiltViewModel()
    TeumTeumEatTheme {
        /*OnBoardingSetUsingApptimeScreen(
            name = "Android",
            viewModel = fakeViewModel,
            uiState = UiStateOnBoardingMain(errorMessage = "한글 또는 영문만 입력할 수 있어요", isNameValid = false),
            onNext = {},
            onPrev = {}
        )*/
    }
}