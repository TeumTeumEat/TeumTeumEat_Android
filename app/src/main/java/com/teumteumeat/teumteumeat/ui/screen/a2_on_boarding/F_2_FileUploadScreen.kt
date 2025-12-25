package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onesignal.OneSignal
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.ContentSelectableBoxButton
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.Utils.UxUtils.extractFileName


@Composable
fun FileUploadScreen(
    name: String = "",
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnBoardingMain,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {

    val context = LocalContext.current
    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    val isSetAllTimeValid = uiState.isSetWorkInTime && uiState.isSetWorkOutTime

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        val fileName = context.extractFileName(uri)

        viewModel.onFileSelected(
            uri = uri,
            fileName = fileName
        )
    }

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
                        "원하는 PDF자료를 넣으세요!",
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

                    // 파일 업로드 버튼
                    ContentSelectableBoxButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 27.dp, horizontal = 21.dp),
                        iconRes = R.drawable.icon_files,
                        titleText = "파일 업로드",
                        lableText = "공부하고 싶은\n내용이 있어요.",
                        onClick = {
                            // todo. 추후에 서버에 실제 파일 전송을 위한 설계 작업 필요
                            //  1. 서버에 한번에 보낼때 적절한 데이터의 양?
                            //  2. 데이터의 형식?
                            //  3. 이외에 설정하면 좋을 제약사항
                            launcher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "image/*",
                                    "text/plain"
                                )
                            )
                        },
                        isSelectableContent = uiState.selectedFileName != "",
                        contentFileName = uiState.selectedFileName,
                        onDelContentClick = { viewModel.onFileDeleted()},
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
                        // todo. uiStateMain 에 파일 가져옴 상태를 정의 후 해당 값으로 버튼 활성화
                        isEnabled = uiState.selectedFileName != "",
                        onClick = {
                        },
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}