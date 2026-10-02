package com.teumteumeat.teumteumeat.di

import android.content.Context
import com.teumteumeat.teumteumeat.utils.date_change_reciver.DateChangeReceiver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DateModule {
    @Provides
    @Singleton
    fun provideDateChangeReceiver(@ApplicationContext context: Context): DateChangeReceiver {
        return DateChangeReceiver(context)
    }

    /** 현재 시각 소스. 테스트에서 고정/가상 시간으로 교체할 수 있도록 주입해서 쓴다. */
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
