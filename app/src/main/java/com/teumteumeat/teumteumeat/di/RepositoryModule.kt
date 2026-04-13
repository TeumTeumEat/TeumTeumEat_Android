package com.teumteumeat.teumteumeat.di

import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepositoryImpl
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepositoryImpl
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepositoryImpl
import com.teumteumeat.teumteumeat.domain.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.data.repository.history.HistoryRepositoryImpl
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepository
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepositoryImpl
import com.teumteumeat.teumteumeat.data.repository.notification.NotificationRepository
import com.teumteumeat.teumteumeat.data.repository.notification.NotificationRepositoryImpl
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepositoryImpl
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.data.repository.user.UserRepositoryImpl
import com.teumteumeat.teumteumeat.domain.repository.date.DateChangeRepository
import com.teumteumeat.teumteumeat.data.repository.date.DateChangeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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

    @Binds abstract fun bindQuizRepository(
        impl: QuizRepositoryImpl
    ): QuizRepository

    @Binds abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds @Singleton abstract fun bindDateChangeRepository(
        impl: DateChangeRepositoryImpl
    ): DateChangeRepository

}





