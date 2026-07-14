package com.teumteumeat.teumteumeat.di

import com.teumteumeat.teumteumeat.data.network.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.sse.EventSource
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * SSE(Server-Sent Events) 인프라를 위한 Hilt 모듈.
 *
 * ### 제공 바인딩
 * - `@SseClient OkHttpClient` : readTimeout = 0 (무제한)인 SSE 전용 클라이언트
 * - `EventSource.Factory`     : [EventSources.createFactory]로 생성되는 싱글턴 팩토리
 *
 * ### SSE 전용 OkHttpClient 설정 이유
 * SSE 연결은 서버가 스트림을 닫을 때까지 HTTP 응답 바디 읽기가 지속된다.
 * 기존 클라이언트의 readTimeout(60 s)이 적용되면 60 초 후 강제 종료되므로
 * `readTimeout(0)`으로 무제한 대기를 허용한다.
 */
@Module
@InstallIn(SingletonComponent::class)
object SseModule {

    /**
     * SSE 전용 [OkHttpClient]를 구분하기 위한 Qualifier.
     * [NetworkModule]의 기존 클라이언트와 충돌하지 않는다.
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SseClient

    /**
     * SSE 전용 [OkHttpClient].
     *
     * - [AuthInterceptor] : Bearer 토큰 자동 첨부
     * - readTimeout = 0   : 스트리밍 연결 유지
     * - connectTimeout    : 기본값(10 s) 유지
     */
    @Provides
    @Singleton
    @SseClient
    fun provideSseOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .readTimeout(0, TimeUnit.SECONDS)
            .build()
    }

    /**
     * SSE 연결 생성을 담당하는 [EventSource.Factory] 싱글턴.
     *
     * [okhttp3.sse.EventSources.createFactory]는 내부적으로 `Accept: text/event-stream`
     * 헤더를 자동 추가하므로 호출부에서 별도 설정 없이 사용할 수 있다.
     */
    @Provides
    @Singleton
    fun provideEventSourceFactory(
        @SseClient okHttpClient: OkHttpClient
    ): EventSource.Factory = EventSources.createFactory(okHttpClient)
}
