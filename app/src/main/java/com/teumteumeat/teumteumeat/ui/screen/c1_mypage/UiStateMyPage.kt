package com.teumteumeat.teumteumeat.ui.screen.c1_mypage

import androidx.compose.ui.graphics.Color
import com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider


data class UiStateMyPage(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // 학습 주제
    val selectedTopic: String = "IT > 앱개발자 > Kotlin Multiplatform",
    val topicDescription: String =
        "안드로이드 기반 개발자가 멀티플랫폼 환경에서\n" +
                "Kotlin을 활용해 생산성을 높이는 방법을 학습합니다.",
    val goalWeek: String = "4주",
    val goalDifficulty: String = "난이도 상",

    // 알림
    val isAlarmEnabled: Boolean = true,

    // 계정 정보
    val socialProvider: SocialProvider = SocialProvider.NONE,
    val loginProvider: String = "카카오 로그인",
    val email: String = "teum1234@kakao.com",

    // 기타
    val appVersion: String = "v1.0.0"
)

fun SocialProvider.toIconBackgroundColor(): Color =
    when (this) {
        SocialProvider.KAKAO -> Color(0xFFFFE812) // 카카오 노랑
        SocialProvider.GOOGLE -> Color.White      // 구글 흰색
        SocialProvider.NONE -> Color.LightGray    // 기본값
    }