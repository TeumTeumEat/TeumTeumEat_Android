package com.teumteumeat.teumteumeat.utils.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teumteumeat.teumteumeat.R
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

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
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
