package com.teumteumeat.teumteumeat.localdata.work_manager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ResetSnackWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        // Hilt EntryPoint 사용 (Worker는 constructor 주입 불가)
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ResetSnackWorkerEntryPoint::class.java
        )

        val homePreference = entryPoint.homePreference()
        homePreference.clearSnackState()

        return Result.success()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ResetSnackWorkerEntryPoint {
    fun homePreference(): com.teumteumeat.teumteumeat.localdata.preference.HomePreference
}

fun scheduleMidnightSnackReset(context: Context) {
    val now = LocalDateTime.now()
    val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
    val delay = Duration.between(now, nextMidnight).toMillis()

    val request = OneTimeWorkRequestBuilder<ResetSnackWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "midnight_snack_reset",
        ExistingWorkPolicy.REPLACE,
        request
    )
}