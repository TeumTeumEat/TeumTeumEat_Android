package com.teumteumeat.teumteumeat.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.teumteumeat.teumteumeat.data.datastore.quizTrackingDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideQuizTrackingPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.quizTrackingDataStore
}
