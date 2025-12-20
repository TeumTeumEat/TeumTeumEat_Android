package com.teumteumeat.teumteumeat.di

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepository
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {

    @Provides
    fun provideSocialLoginRepository(
        apiService: AuthApiService,
        tokenLocalDataSource: TokenLocalDataSource
    ): SocialLoginRepository {
        return SocialLoginRepositoryImpl(apiService, tokenLocalDataSource)
    }

}





