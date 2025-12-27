package com.teumteumeat.teumteumeat.di

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.document.DocumentApiService
import com.teumteumeat.teumteumeat.data.api.goal.GoalApiService
import com.teumteumeat.teumteumeat.data.api.user.CategoryApiService
import com.teumteumeat.teumteumeat.data.api.user.UserApiService
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
    fun provideCategoryApiService(retrofit: Retrofit): CategoryApiService =
        retrofit.create(CategoryApiService::class.java)

    @Provides @Singleton
    fun provideDocumentApiService(retrofit: Retrofit): DocumentApiService =
        retrofit.create(DocumentApiService::class.java)

    @Provides @Singleton
    fun provideGoalApiService(retrofit: Retrofit): GoalApiService =
        retrofit.create(GoalApiService::class.java)

    // ---- api 서비스 추가 ----

}
