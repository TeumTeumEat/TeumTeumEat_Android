package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.net.Uri

data class SelectedFile(
    val uri: Uri,          // 실제 파일 접근 경로 (핵심)
    val fileName: String,  // 서버에 전달할 파일명
    val mimeType: String,  // Content-Type (application/pdf)
    val size: Long         // 파일 크기 (50MB 검증용)
)
