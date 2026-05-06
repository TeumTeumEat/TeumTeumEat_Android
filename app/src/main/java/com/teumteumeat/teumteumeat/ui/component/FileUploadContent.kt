package com.teumteumeat.teumteumeat.ui.component

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.ContentSelectableBoxButton
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.Utils.UxUtils.extractFileName

/**
 * PDF 파일 업로드 UI — 온보딩과 목표 추가 화면이 공유하는 컴포넌트.
 *
 * 파일 선택 런처를 내부에서 관리하며, 선택 결과를 [onFileSelected]로 전달한다.
 * 파일 유효성 검증(MIME 타입·크기·확장자)은 ViewModel에서 처리한다.
 *
 * @param selectedFileName 현재 선택된 파일명 (없으면 빈 문자열)
 * @param pageErrorMessage Toast로 표시할 에러 메시지 (null이면 표시 안 함)
 * @param onFileSelected 파일 선택 완료 콜백 — ViewModel이 유효성 검증 후 상태 반영
 * @param onFileDeleted 파일 삭제 버튼 클릭 콜백
 * @param onErrorShown Toast 표시 후 에러 초기화 콜백
 * @param onNext "다음으로" 버튼 클릭 콜백
 */
@Composable
fun FileUploadContent(
    selectedFileName: String,
    pageErrorMessage: String?,
    onFileSelected: (uri: Uri, fileName: String, mimeType: String, size: Long) -> Unit,
    onFileDeleted: () -> Unit,
    onErrorShown: () -> Unit,
    onNext: () -> Unit,
) {
    val context = LocalContext.current

    pageErrorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onErrorShown()
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        val contentResolver = context.contentResolver
        val fileName = context.extractFileName(uri)
        val mimeType = contentResolver.getType(uri) ?: "application/pdf"
        val size = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
        onFileSelected(uri, fileName, mimeType, size)
    }

    val launchFilePicker: () -> Unit = {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ).toUri()
                )
            }
        }
        fileLauncher.launch(intent)
    }

    DefaultMonoBg(
        extensionHeight = 0.dp,
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
                        .padding()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    SpeechBubble(text = "공부하고 싶은 PDF 자료를\n선택해 주세요!")
                    Image(
                        painter = painterResource(R.drawable.char_onboarding_five_two),
                        contentDescription = "앞을 보는 케릭터",
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(15.dp))

                    ContentSelectableBoxButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 37.dp),
                        iconRes = R.drawable.icon_file_fill,
                        titleText = "파일 업로드",
                        lableText = "50MB 이하 파일만 업로드 가능",
                        onClick = launchFilePicker,
                        isSelectableContent = selectedFileName.isNotEmpty(),
                        contentFileName = selectedFileName,
                        onDelContentClick = onFileDeleted,
                    )

                    Spacer(Modifier.height(150.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = Typography.labelMedium.copy(lineHeight = 24.sp),
                        isEnabled = selectedFileName.isNotEmpty(),
                        onClick = onNext,
                        conerRadius = 16.dp,
                    )
                }
            }
        },
    )
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "파일 미선택")
@Composable
private fun FileUploadContentEmptyPreview() {
    TeumTeumEatTheme {
        FileUploadContent(
            selectedFileName = "",
            pageErrorMessage = null,
            onFileSelected = { _, _, _, _ -> },
            onFileDeleted = {},
            onErrorShown = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "파일 선택됨 — 다음 활성화")
@Composable
private fun FileUploadContentSelectedPreview() {
    TeumTeumEatTheme {
        FileUploadContent(
            selectedFileName = "android_study_notes.pdf",
            pageErrorMessage = null,
            onFileSelected = { _, _, _, _ -> },
            onFileDeleted = {},
            onErrorShown = {},
            onNext = {},
        )
    }
}