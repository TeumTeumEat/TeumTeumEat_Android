package com.teumteumeat.teumteumeat.utils.date_change_reciver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class DateChangeReceiver @Inject constructor(
    @ApplicationContext private val context: Context,
) : BroadcastReceiver() {

    // 콜백을 외부에서 동적으로 등록하기 위한 변수
    private var onDateChanged: (() -> Unit)? = null

    fun setOnDateChangedListener(listener: () -> Unit) {
        this.onDateChanged = listener
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("HomeViewModel", "브로드캐스트 수신 성공! Action: ${intent?.action}")
        if (intent?.action == Intent.ACTION_DATE_CHANGED ||
            intent?.action == "com.teumteumeat.test.ACTION_DATE_CHANGED") {
            onDateChanged?.invoke()
        }
    }
}