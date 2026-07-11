package com.teumteumeat.teumteumeat.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.teumteumeat.teumteumeat.data.datastore.goalTrackingDataStore
import com.teumteumeat.teumteumeat.data.datastore.quizTrackingDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/** [GoalTrackingDataStore][com.teumteumeat.teumteumeat.data.datastore.GoalTrackingDataStore] 전용 DataStore 바인딩 구분자 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoalTrackingPreferences

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideQuizTrackingPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.quizTrackingDataStore

    @Provides
    @Singleton
    @GoalTrackingPreferences
    fun provideGoalTrackingPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.goalTrackingDataStore
}
