package com.teumteumeat.teumteumeat.utils.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenStore


object FcmTokenInitializer {

    fun init(context: Context) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (!token.isNullOrBlank()) {
                    // 디버그 빌드 전용: 서버 테스트 발송용 디바이스 토큰 등록에 쓰도록 전체 토큰을 출력한다 (logcat 필터: FCM_TOKEN)
                    if (BuildConfig.DEBUG) Log.d("FCM_TOKEN", "디바이스 토큰: $token")
                    FcmTokenStore.save(context, token)
                }

            }
            .addOnFailureListener { e ->
                Log.e("FCM", "FCM 토큰 초기화 실패", e)
            }
    }
}