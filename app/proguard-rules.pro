# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# ============================================================================
# 공통 속성 (attributes)
# ----------------------------------------------------------------------------
# AGP 8.x 는 R8 full mode 가 기본 활성화(android.enableR8.fullMode=true)이며,
# full mode 에서는 keep 대상이 아닌 클래스의 제네릭 시그니처가 제거된다.
# Gson 은 ApiResponse<T, D> 같은 제네릭 타입을 리플렉션으로 해석하므로
# Signature 가 없으면 역직렬화가 실패한다.
# ============================================================================
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keepattributes *Annotation*
-keepattributes AnnotationDefault
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# ============================================================================
# Retrofit 2.9.0
# ----------------------------------------------------------------------------
# 2.9.0 의 consumer 규칙은 R8 full mode 이전 버전이라 suspend 함수의
# 제네릭 반환 타입을 보존하지 못한다. 이 프로젝트의 API 메서드는 전부 suspend 이며
# 실제 반환 타입이 Continuation 파라미터로 소거되므로 아래 규칙이 필수다.
# ============================================================================
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# @GET/@POST 등이 선언된 인터페이스의 반환 타입 클래스를 보존
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# Retrofit API 서비스 인터페이스 (동적 프록시 생성 대상)
-keep,allowobfuscation interface com.teumteumeat.teumteumeat.data.api.**

# ============================================================================
# OkHttp
# ----------------------------------------------------------------------------
# OkHttp 은 BouncyCastle / Conscrypt / OpenJSSE 를 선택적 TLS 프로바이더로
# 참조하지만 이 프로젝트는 셋 다 의존성에 포함하지 않는다.
# 런타임에 Android 기본 플랫폼만 사용하므로 경고를 무시한다.
# ============================================================================
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ============================================================================
# Gson
# ----------------------------------------------------------------------------
# 이 프로젝트의 DTO 는 대부분 @SerializedName 을 사용하지 않고
# 프로퍼티명 = JSON 키에 의존한다. 따라서 필드명이 난독화되면
# 모든 API 응답이 null/기본값으로 파싱된다. DTO 는 필드까지 전부 keep 한다.
# ============================================================================
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# --- 네트워크 DTO 패키지 ---
-keep class com.teumteumeat.teumteumeat.data.network.model.** { *; }
-keep class com.teumteumeat.teumteumeat.data.network.model_request.** { *; }
-keep class com.teumteumeat.teumteumeat.data.network.model_response.** { *; }

# --- data.network 패키지 밖에 있는 DTO ---
-keep class com.teumteumeat.teumteumeat.data.api.user.CommuteTimeRequest { *; }
-keep class com.teumteumeat.teumteumeat.data.api.user.UpdateNameRequest { *; }
-keep class com.teumteumeat.teumteumeat.data.document.response.** { *; }
-keep class com.teumteumeat.teumteumeat.data.history.response.** { *; }
-keep class com.teumteumeat.teumteumeat.domain.model.on_boarding.CategoriesResponseDto { *; }
-keep class com.teumteumeat.teumteumeat.domain.model.on_boarding.CategoryDto { *; }
-keep class com.teumteumeat.teumteumeat.domain.model.on_boarding.UserName { *; }
-keep class com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingStatus { *; }
-keep class com.teumteumeat.teumteumeat.domain.model.auth.ResponseBody { *; }

# --- SSE 응답 파싱용 private 중첩 클래스 ---
# 패키지 와일드카드로 도달하지 않으므로 개별 지정한다.
-keep class com.teumteumeat.teumteumeat.data.repository.document.DocumentProcessingStreamRepositoryImpl$StatusPayload { *; }
-keep class com.teumteumeat.teumteumeat.data.remote.sse.SseEventSourceListener$SseErrorBody { *; }

# ============================================================================
# Enum
# ----------------------------------------------------------------------------
# Gson 은 enum 을 상수명(name) 문자열로 직렬화/역직렬화한다.
# R8 full mode 는 enum 상수 필드명도 난독화하므로 Gson 경계를 넘는
# enum 은 상수명까지 보존해야 한다.
# (data.network.** 하위 enum 은 위 DTO 규칙으로 이미 보존됨)
# ============================================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.teumteumeat.teumteumeat.domain.model.goal.Difficulty { *; }
-keep class com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType { *; }
-keep class com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState { *; }
# AccountInfoResponse.socialProvider 의 타입 — UI 패키지에 있으나 Gson 필드다.
-keep class com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider { *; }

# ============================================================================
# WorkManager
# ----------------------------------------------------------------------------
# Worker 는 클래스명 문자열로 리플렉션 인스턴스화된다.
# ============================================================================
-keep class com.teumteumeat.teumteumeat.localdata.work_manager.ResetSnackWorker {
    public <init>(...);
}