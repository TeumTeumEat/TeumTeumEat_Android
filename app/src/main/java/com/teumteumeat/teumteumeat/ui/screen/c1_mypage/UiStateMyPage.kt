package com.teumteumeat.teumteumeat.ui.screen.c1_mypage

import com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.NotificationSettingGuideType


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
    val isSelGoalCompleted: Boolean = true,

    // 알림
    val isAlarmEnabled: Boolean = true,
    val requestNotificationPermission: Boolean = false,
    val notificationGuideType: NotificationSettingGuideType = NotificationSettingGuideType.NONE,

    // 계정 정보
    val socialProvider: SocialProvider = SocialProvider.NONE,
    val loginProvider: String = "Unknown",
    val email: String = "Unknown@unknown.com",

    // 기타
    val appVersion: String = "v1.0.0"
)

fun SocialProvider.toIconImg(): Int =
    when (this) {
        SocialProvider.KAKAO -> R.drawable.icon_kakao_mini // 카카오 노랑
        SocialProvider.GOOGLE -> R.drawable.icon_google_mini      // 구글 흰색
        SocialProvider.NONE -> R.drawable.icon_none_mini    // 기본값
    }