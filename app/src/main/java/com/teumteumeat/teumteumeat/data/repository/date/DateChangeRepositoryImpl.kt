package com.teumteumeat.teumteumeat.data.repository.date

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.domain.repository.date.DateChangeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class DateChangeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DateChangeRepository {

    override fun observeDateChange(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // 시스템 ACTION_DATE_CHANGED 또는 테스트용 액션 수신 시 이벤트 전송
                trySend(Unit)
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            if (BuildConfig.DEBUG) {
                addAction("com.teumteumeat.test.ACTION_DATE_CHANGED")
            }
        }

        val flags = if (BuildConfig.DEBUG) {
            ContextCompat.RECEIVER_EXPORTED
        } else {
            ContextCompat.RECEIVER_NOT_EXPORTED
        }

        // 리시버 등록
        ContextCompat.registerReceiver(context, receiver, filter, flags)

        // Flow가 닫힐 때(ViewModel이 clear될 때 등) 리시버 자동으로 해제
        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                Log.e("DateChangeRepo", "Receiver already unregistered", e)
            }
        }
    }
}