package com.teumteumeat.teumteumeat.domain.usecase

import android.content.Context
import android.util.Log
import com.teumteumeat.teumteumeat.data.repository.notification.NotificationRepository
import com.teumteumeat.teumteumeat.domain.usecase.auth.LogoutUseCase
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenStore
import com.teumteumeat.teumteumeat.utils.Utils.PrefsUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val logoutUseCase: LogoutUseCase,
    private val notificationRepository: NotificationRepository,
) {

    private val _sessionEvent = MutableSharedFlow<SessionEvent>()
    val sessionEvent = _sessionEvent.asSharedFlow()

    suspend fun expireSession() {
        // 로그아웃 시 현재 디바이스 기기 알림 기능 해제 구현
        //  1. 알림 기능 off APi 구현
        //  2. 로그아웃 시 호출 되도록 적용
        val fcmToken = FcmTokenStore.get(appContext) ?: ""
        if (fcmToken.isBlank()) Log.e("${this@SessionManager}", "fcmToken: $fcmToken")

        notificationRepository.deleteDeviceToken(token = fcmToken) // 디바이스 토큰 삭제
        logoutUseCase() /* 🔥 로그인 토큰 삭제 */
        PrefsUtil.setOnboardingCompleted(appContext, false)
        _sessionEvent.emit(SessionEvent.Expired)
    }
}

sealed class SessionEvent {
    object Expired : SessionEvent()
}
