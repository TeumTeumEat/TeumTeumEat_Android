package com.teumteumeat.teumteumeat.di

import com.teumteumeat.teumteumeat.data.api.HistoryApiService
import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.document.DocumentApiService
import com.teumteumeat.teumteumeat.data.api.goal.GoalApiService
import com.teumteumeat.teumteumeat.data.api.quiz.QuizApiService
import com.teumteumeat.teumteumeat.data.api.category.CategoryApiService
import com.teumteumeat.teumteumeat.data.api.notification.NotificationApiService
import com.teumteumeat.teumteumeat.data.api.user.UserApiService
import com.teumteumeat.teumteumeat.di.NetworkModule.SlowClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

    @Provides @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides @Singleton
    fun provideCategoryApiService(
        @SlowClient retrofit: Retrofit
    ): CategoryApiService =
        retrofit.create(CategoryApiService::class.java)

    @Provides @Singleton
    fun provideDocumentApiService(@SlowClient retrofit: Retrofit): DocumentApiService =
        retrofit.create(DocumentApiService::class.java)

    @Provides @Singleton
    fun provideGoalApiService(retrofit: Retrofit): GoalApiService =
        retrofit.create(GoalApiService::class.java)

    // 첫 퀴즈 조회 시 타임아웃을 고려하여 @SlowClient 추가 처리
    @Provides @Singleton
    fun provideQuizApiService(
        @SlowClient retrofit: Retrofit
    ): QuizApiService =
        retrofit.create(QuizApiService::class.java)

    @Provides @Singleton
    fun provideHistoryApiService(retrofit: Retrofit): HistoryApiService =
        retrofit.create(HistoryApiService::class.java)


    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService =
        retrofit.create(NotificationApiService::class.java)

    // ---- api 서비스 추가 ----

}
