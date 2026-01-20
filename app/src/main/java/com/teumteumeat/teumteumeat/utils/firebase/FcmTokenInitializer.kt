package com.teumteumeat.teumteumeat.utils.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenStore


object FcmTokenInitializer {

    fun init(context: Context) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (!token.isNullOrBlank()) {
                    Log.d("FCM", "초기 FCM 토큰 저장: $token")
                    FcmTokenStore.save(context, token)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "FCM 토큰 초기화 실패", e)
            }
    }
}