package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

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
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OnBoardingViewModel
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.UiStateOnboardingState
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.Utils.UxUtils.extractFileName


private const val MAX_FILE_SIZE_MB = 50L
private const val MAX_FILE_SIZE_BYTE = MAX_FILE_SIZE_MB * 1024 * 1024
private const val PDF_MIME_TYPE = "application/pdf"

@Composable
fun AddGoalFileUploadScreen(
    name: String = "",
    viewModel: AddGoalViewModel,
    uiState: UiStateAddGoalState,
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

        val mimeType = contentResolver.getType(uri) ?: "application/pdf"
        if (mimeType != PDF_MIME_TYPE) {
            viewModel.showFileError(
                title = "앗! 파일 형식이 달라요",
                message = "PDF 형식의 파일만 업로드할 수 있어요.\n" +
                        "다른 파일을 선택해주세요."
            )
            return@rememberLauncherForActivityResult
        }

        // 🔹 선택한 파일의 "실제 크기(Byte 단위)"를 가져오기 위한 코드
        // - S3 presigned 업로드 전, 클라이언트 단에서 파일 크기 제한(예: 50MB)을 검증하기 위함
        // - ContentResolver는 Android에서 외부 파일에 접근하기 위한 표준 인터페이스
        // - openFileDescriptor("r") : 읽기 전용으로 파일 디스크립터를 열고
        // - statSize : 해당 파일의 전체 크기를 Byte 단위로 반환
        // - 만약 파일 정보를 가져오지 못하면 0L로 처리하여 안전하게 방어

        // 2️⃣ 파일 크기 검사 (50MB 제한)
        val size = contentResolver
            .openFileDescriptor(uri, "r")
            ?.statSize
            ?: 0L

        if (size <= 0L || size > MAX_FILE_SIZE_BYTE) {
            viewModel.showFileError(
                title = "앗! 파일이 너무 커요",
                message = "50MB 이상의 파일은 업로드 할 수 없어요.\n" +
                        "다른 파일을 선택해주세요."
            )
            return@rememberLauncherForActivityResult
        }

        // 3️⃣ 파일명 추출
        val fileName = context.extractFileName(uri)

        // ✅ 모든 제약 통과 → ViewModel 전달
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