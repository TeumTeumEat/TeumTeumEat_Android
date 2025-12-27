package com.teumteumeat.teumteumeat.di

import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepositoryImpl
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepositoryImpl
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepository
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepositoryImpl
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.data.repository.user.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds abstract fun bindAuthRepository(
        impl: SocialLoginRepositoryImpl
    ): SocialLoginRepository

    @Binds abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds abstract fun bindDocumentRepository(
        impl: DocumentRepositoryImpl
    ): DocumentRepository

    @Binds abstract fun bindGoalRepository(
        impl: GoalRepositoryImpl
    ): GoalRepository

}





