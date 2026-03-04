package com.teumteumeat.teumteumeat.di

import android.content.Context
import com.teumteumeat.teumteumeat.utils.date_change_reciver.DateChangeReceiver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DateModule {
    @Provides
    @Singleton
    fun provideDateChangeReceiver(@ApplicationContext context: Context): DateChangeReceiver {
        return DateChangeReceiver(context)
    }
}