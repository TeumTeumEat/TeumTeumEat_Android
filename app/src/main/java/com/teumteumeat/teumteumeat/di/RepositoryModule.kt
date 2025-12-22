package com.teumteumeat.teumteumeat.di

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepository
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepositoryImpl
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.data.repository.user.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

/*    @Provides
    fun provideSocialLoginRepository(
        apiService: AuthApiService,
        tokenLocalDataSource: TokenLocalDataSource
    ): SocialLoginRepository {
        return SocialLoginRepositoryImpl(apiService, tokenLocalDataSource)
    }*/

    @Binds abstract fun bindAuthRepository(
        impl: SocialLoginRepositoryImpl
    ): SocialLoginRepository

    @Binds abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

}





