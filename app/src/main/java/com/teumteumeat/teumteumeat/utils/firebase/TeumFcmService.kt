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
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.screen.a0_splash.SplashActivity
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenStore
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenSyncUtil

class TeumFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM", "새 디바이스 토큰: $token")

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

        showNotification(title, body, message.data[DeepLinkConstants.PUSH_DATA_KEY_LINK])
    }

    /**
     * 알림 탭 시 스플래시로 들어가는 PendingIntent.
     * 이전 백스택을 비우고 스플래시부터 시작해 로그인/온보딩 진행도를 확인한 뒤, 홈 → [link] 목적지(리그) 순으로 이동한다.
     * 앱이 꺼진 상태에서 시스템이 직접 띄운 알림(notification 메시지)을 탭했을 때도 data 의 `link` 가 같은 extra 로 전달된다.
     */
    private fun deepLinkPendingIntent(link: String): PendingIntent {
        val intent = Intent(this, SplashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(DeepLinkConstants.PUSH_DATA_KEY_LINK, link)
        }
        return PendingIntent.getActivity(
            this,
            link.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun showNotification(title: String, body: String, link: String? = null) {
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
            .apply { link?.takeIf { it.isNotBlank() }?.let { setContentIntent(deepLinkPendingIntent(it)) } }
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

fun logCurrentFcmToken() {
    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            Log.e("FCM_TOKEN_TRACE", "🔥 Firebase 발급 토큰 = $token")
        }
        .addOnFailureListener { e ->
            Log.e("FCM_TOKEN_TRACE", "❌ 토큰 발급 실패", e)
        }
}
