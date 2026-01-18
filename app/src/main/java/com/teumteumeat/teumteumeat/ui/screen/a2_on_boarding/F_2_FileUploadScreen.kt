package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.widget.Toast
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
import androidx.core.net.toUri
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.ContentSelectableBoxButton
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.SpeechBubble
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.Utils.UxUtils.extractFileName


@Composable
fun FileUploadScreen(
    name: String = "",
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {

    val context = LocalContext.current
    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    // 🔔 에러 메시지 변경 감지 → Toast 표시
    uiState.pageErrorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast
                .makeText(context, message, Toast.LENGTH_SHORT)
                .show()

            // ✅ 토스트 표시 후 메시지 초기화
            viewModel.clearPageErrorMessage()
        }
    }

    val downloadLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult

        val uri = result.data?.data ?: return@rememberLauncherForActivityResult


        val contentResolver = context.contentResolver

        val fileName = context.extractFileName(uri)
        val mimeType = contentResolver.getType(uri) ?: "application/pdf"

        // 🔹 선택한 파일의 "실제 크기(Byte 단위)"를 가져오기 위한 코드
        // - S3 presigned 업로드 전, 클라이언트 단에서 파일 크기 제한(예: 50MB)을 검증하기 위함
        // - ContentResolver는 Android에서 외부 파일에 접근하기 위한 표준 인터페이스
        // - openFileDescriptor("r") : 읽기 전용으로 파일 디스크립터를 열고
        // - statSize : 해당 파일의 전체 크기를 Byte 단위로 반환
        // - 만약 파일 정보를 가져오지 못하면 0L로 처리하여 안전하게 방어
        val size = contentResolver
            .openFileDescriptor(uri, "r")
            ?.statSize ?: 0L

        viewModel.onFileSelected(
            uri = uri,
            fileName = fileName,
            mimeType = mimeType,
            size = size
        )
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
                    Spacer(modifier = Modifier.height(4.dp))

                    SpeechBubble(text = "공부하고 싶은 PDF 자료를\n" +
                            "선택해 주세요!")
                    Spacer(modifier = Modifier.height(12.dp))

                    Image(
                        painter = painterResource(R.drawable.char_onboarding_five_two),
                        contentDescription = "앞을 보는 케릭터",
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // 파일 업로드 버튼
                    ContentSelectableBoxButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 27.dp, horizontal = 21.dp),
                        iconRes = R.drawable.icon_files_big,
                        titleText = "파일 업로드",
                        lableText = "50MB 이하 파일만 업로드 가능",
                        onClick = {
                            //  1. 서버에 한번에 보낼때 적절한 데이터의 양의 한계 50MB
                            //  2. 데이터의 형식 - .pdf 로 제한
                            //  3. 다운로드 폴더를 초기 위치로 설정
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "application/pdf"

                                // ⭐ 다운로드 폴더를 초기 위치로 설정
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    putExtra(
                                        DocumentsContract.EXTRA_INITIAL_URI,
                                        Environment.getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_DOWNLOADS
                                        ).toUri()
                                    )
                                }
                            }

                            downloadLauncher.launch(intent)
                        },
                        isSelectableContent = uiState.selectedFileName != "",
                        contentFileName = uiState.selectedFileName,
                        onDelContentClick = { viewModel.onFileDeleted() },
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
                            onNext()
                            // viewModel.issuePresignedUrl()
                        },
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}