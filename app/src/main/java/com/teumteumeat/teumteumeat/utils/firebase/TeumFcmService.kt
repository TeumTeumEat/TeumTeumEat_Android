package com.teumteumeat.teumteumeat.utils.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.screen.deeplink.PushRouterActivity
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenStore
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenSyncUtil

class TeumFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        if (BuildConfig.DEBUG) Log.d("FCM_TOKEN", "새 디바이스 토큰: $token")

        // ✅ 로컬 저장 (DataStore / SharedPreferences)
        FcmTokenStore.save(applicationContext, token)

        // 2️⃣ 서버 동기화 (Util 재사용)
        FcmTokenSyncUtil.syncWithServer(applicationContext, token)
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM", "🔥 메시지 수신됨")
        Log.d("FCM", "notification=${message.notification}")
        Log.d("FCM", "data=${message.data}")

        val title = message.notification?.title ?: "틈틈잇"
        val body = message.notification?.body ?: ""

        showNotification(
            title = title,
            body = body,
            link = message.data[DeepLinkConstants.PUSH_DATA_KEY_LINK],
            type = message.data[DeepLinkConstants.PUSH_DATA_KEY_TYPE],
        )
    }

    /**
     * 알림 탭 시 [PushRouterActivity] 로 들어가는 PendingIntent.
     * 중계 Activity 가 이동 정책(퀴즈/주제 추가/새 목표 설정 중에는 이동하지 않음)을 판정하고, 이동할 때는
     * 이전 백스택을 비우고 스플래시부터 시작해 로그인/온보딩 진행도를 확인한 뒤 홈 또는 홈 → 리그 순으로 이동한다.
     * 앱이 꺼진/백그라운드 상태에서 시스템이 직접 띄운 알림(notification 메시지)을 탭하면 시스템이 스플래시를 열고
     * data 의 `link`/`type` 이 같은 extra 로 전달된다 (스플래시가 같은 정책을 적용).
     */
    private fun deepLinkPendingIntent(link: String?, type: String?): PendingIntent {
        val intent = Intent(this, PushRouterActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            link?.let { putExtra(DeepLinkConstants.PUSH_DATA_KEY_LINK, it) }
            type?.let { putExtra(DeepLinkConstants.PUSH_DATA_KEY_TYPE, it) }
        }
        return PendingIntent.getActivity(
            this,
            "$type|$link".hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        link: String? = null,
        type: String? = null,
    ) {
        val channelId = "default_channel"

        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 이상 필수
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "기본 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_teumteumeat_round) // 반드시 존재
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .apply {
                val safeLink = link?.takeIf { it.isNotBlank() }
                val safeType = type?.takeIf { it.isNotBlank() }
                if (safeLink != null || safeType != null) {
                    setContentIntent(deepLinkPendingIntent(safeLink, safeType))
                }
            }
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

fun logCurrentFcmToken() {
    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            if (BuildConfig.DEBUG) Log.d("FCM_TOKEN", "Firebase 발급 토큰: $token")
        }
        .addOnFailureListener { e ->
            Log.e("FCM_TOKEN_TRACE", "❌ 토큰 발급 실패", e)
        }
}
