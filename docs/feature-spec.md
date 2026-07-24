# 틈틈잇(TeumTeumEat) 기능 명세서

> 이 문서는 2026-07-24 기준 코드베이스(`claude/learning-app-features-0dk8i0` 브랜치)를 전수 조사하여 **현재 실제로 동작하는 기능**을 정리한 명세서입니다. 기획 의도가 아니라 구현 사실을 기준으로 작성했으며, 코드와 어긋나는 CLAUDE.md 컨벤션(예: Room)이나 죽은 코드(dead code)는 [11. 알려진 이슈 및 기술부채](#11-알려진-이슈-및-기술부채)에 별도로 정리했습니다.

## 목차
1. [개요](#1-개요)
2. [핵심 사용자 흐름](#2-핵심-사용자-흐름)
3. [화면별 기능 명세](#3-화면별-기능-명세)
4. [도메인(Domain) 레이어](#4-도메인domain-레이어)
5. [데이터(Data) 레이어](#5-데이터data-레이어)
6. [인증/로그인](#6-인증로그인)
7. [알림 (FCM / OneSignal)](#7-알림-fcm--onesignal)
8. [게이미피케이션 현황](#8-게이미피케이션-현황)
9. [수익 모델 현황](#9-수익-모델-현황)
10. [버전 및 주요 의존성](#10-버전-및-주요-의존성)
11. [알려진 이슈 및 기술부채](#11-알려진-이슈-및-기술부채)

---

## 1. 개요

- **제품명**: 틈틈잇 (TeumTeumEat) — "틈틈이 이어 먹는다"는 뜻으로, 하루 5분 이하의 짧은 세션을 알림으로 리마인드하여 자투리 시간에 학습을 이어가게 하는 컨셉의 안드로이드 학습 앱.
- **패키지**: `com.teumteumeat.teumteumeat` / minSdk 26, target·compileSdk 36.
- **스택**: Kotlin + Jetpack Compose + Hilt + DataStore(Preferences) + Coroutines/Flow + Retrofit/OkHttp(SSE 포함) + FCM + OneSignal(권한 UX 전용) + Firebase Analytics/Crashlytics/Remote Config + Kakao SDK.
- **아키텍처**: MVVM + Clean Architecture(Presentation / Domain / Data 3계층). ViewModel은 `UiState`(StateFlow, `.asStateFlow()`로 캡슐화)와 `UiEvent`(1회성 이벤트, SharedFlow/Channel)를 노출하고, Repository 직접 호출 없이 UseCase를 경유하는 패턴을 전반적으로 따름.
- **학습 콘텐츠 생성 방식**: 서버가 LLM으로 요약·퀴즈를 생성하고, 클라이언트는 **SSE(Server-Sent Events)** 로 생성 과정을 실시간 스트리밍 수신하여 타이핑되듯 렌더링함(요약 생성, PDF 문서 OCR 처리 상태 2곳에 사용).
- **로컬 데이터베이스 없음**: CLAUDE.md는 Room 컨벤션을 명시하고 있으나, 실제로는 Room을 전혀 사용하지 않습니다. 자세한 내용은 [11장](#11-알려진-이슈-및-기술부채) 참고.

## 2. 핵심 사용자 흐름

```
[스플래시] → (최초) [로그인] → [온보딩] ─┐
                                          │
[스플래시] → (재방문, 자동로그인 성공) ───┴─→ [메인(하단탭: 홈/추가/라이브러리)]
                                                      │
                            ┌─────────────────────────┼─────────────────────────┐
                            ▼                         ▼                         ▼
                     [홈 - 오늘의 학습]          [+ 목표 추가]              [라이브러리 - 기록]
                            │                                              (날짜별/주제별)
             ┌──────────────┴──────────────┐                                    │
             ▼                              ▼                            과거 기록 재열람
      [오늘의 요약(b1)]              (요약 다 봤으면) 광고 시청           ┌────┴────┐
       SSE 스트리밍 생성                → 쿠폰 적립 → 1회 더 학습         ▼         ▼
             │                                                    [일별 요약]  [일별 퀴즈결과]
             ▼                                                      (읽기 전용)
      [오늘의 퀴즈(b2)]
       OX/MCQ, 문항별 즉시채점
             │
             ▼
      [퀴즈 결과(b3)]
       정답개수 → 상세해설 → (완주 시) 축하화면 → 새 목표 시작 or 홈
```

목표(학습 주제)가 만료되거나 완료되면 전용 화면 없이 **홈 화면의 다이얼로그**로 안내되며, 마이페이지에서 알림 설정·계정 관리·목표 전환이 가능합니다.

## 3. 화면별 기능 명세

### 3.1 스플래시 (`ui/screen/a0_splash`)
| 항목 | 내용 |
|---|---|
| 목적 | 앱 진입 시 버전 체크·자동 로그인·분기 처리 |
| 흐름 | 로티 애니메이션 재생 → Firebase Remote Config `fetchAndActivate()` → 강제/선택 업데이트 필요 여부 확인 → `AutoLoginUseCase`로 자동 로그인 시도 → 성공 시 온보딩 완료 여부(로컬 우선, 서버 재확인)에 따라 메인/온보딩으로 이동, 실패 시 로그인 화면으로 이동 |
| 관련 파일 | `SplashActivity.kt`, `SplashViewModel.kt`, `SplasUiState.kt` |

### 3.2 로그인 (`ui/screen/a1_login`)
| 항목 | 내용 |
|---|---|
| 목적 | 소셜 로그인을 통한 회원가입/로그인 |
| 지원 방식 | **카카오**(카카오톡 앱 로그인 우선, 미설치 시 카카오계정 웹 로그인 폴백) / **구글**(`GoogleSignInClient`, idToken 방식) |
| 흐름 | idToken 획득 → `POST /api/v1/auth/oauth/register` → 서버가 약관 미동의(`AUTH-006`) 응답 시 약관 동의 바텀시트 노출 → 동의 후 재요청 → 온보딩 완료 여부에 따라 분기 |
| 회원탈퇴 | 이 화면에도 탈퇴 기능 존재(`DELETE /api/v1/users/withdrawal`, 마이페이지와 중복 구현) |
| 관련 파일 | `LoginActivity.kt`, `LoginViewModel.kt`, `LoginScreen.kt` |

### 3.3 온보딩 (`ui/screen/a2_on_boarding`)
코드상 7개 화면(Welcome~Complete)으로 구성되며, 완료 시 사용자의 학습 목표(Goal)를 최초 생성합니다. 단, 2026-07-24 실기기 QA에서는 "5단계"로 체감된다는 관찰이 있었습니다 — 안내/확인 성격의 Welcome·Complete 화면은 '입력 단계'로 세지 않고, 실제 데이터를 입력하는 5단계(학습분량+알림시간 / 카테고리 / 난이도 / 학습기간 / 최종확인)로 체감하는 것으로 추정됩니다. 사용자 체감 기준으로는 5단계 쪽이 더 정확할 수 있습니다.

| 단계 | 화면 | 수집/처리 내용 |
|---|---|---|
| 1 | Welcome | 캐릭터 애니메이션 + 시작하기 |
| 2 | SetRoutine | 하루 문제 수(3/5/7/10) 선택, 출근/퇴근 알림 시간 2개 설정, 알림 권한 요청(OneSignal) |
| 3 | SelectLearningMethod | 학습 방식 선택: **카테고리 기반** vs **PDF 문서 업로드 기반** |
| 3-A | SelectCategory | 서버 카테고리 트리를 3-depth로 드릴다운하여 관심 주제 선택 |
| 3-B | UploadFile | PDF 선택(MIME `application/pdf`, 최대 50MB, 확장자 검증) |
| 4 | OptimizeData | 난이도(상/중/하) + 학습 요청 프롬프트(사전정의 10종 중 택1 또는 직접입력, 최대 30자) |
| 5 | SetStudyPeriod | 학습 기간(주 단위) 선택, 종료일 자동 계산 |
| 6 | Review | 입력값 전체 최종 확인 |
| 7 | Complete | 목표 생성 완료(+ PDF인 경우 업로드 및 OCR 처리 SSE 진행률 표시) 후 메인 이동 |

> 참고: 온보딩에서 **닉네임은 입력받지 않습니다.** 소셜 로그인 시 서버가 부여한 기본 닉네임을 그대로 사용하며, 변경은 마이페이지 → 정보수정에서만 가능합니다(§11 참고).

관련 파일: `OnBoardingActivity.kt`, `OnBoardingViewModel.kt`, `OnBoardingNavHost.kt`, `OnBoardingFlow.kt`

### 3.4 메인 (`ui/screen/a4_main`)
하단 네비게이션 3개 항목으로 구성된 앱의 메인 셸입니다: **홈 / 추가(+) / 라이브러리**.

#### 3.4.1 홈 (`a4_1_home`)
| 항목 | 내용 |
|---|---|
| 목적 | 오늘의 학습 상태를 확인하고 시작하는 진입점 |
| 핵심 UX | 화면 중앙의 "간식(음식) 캐릭터"가 오늘의 학습 상태를 은유적으로 표현(3가지 상태: `Available`(학습 전) / `Consumed`(학습 완료, 광고 보상 가능) / `Completed`(목표 자체 완료)) |
| 상태별 동작 | `Available` 탭 → 오늘의 요약 화면 이동 / `Consumed` 탭 → 광고 시청 유도 모달 / `Completed` 탭 → 완료 안내 토스트 |
| 목표 완료 시 | `GoalCompletedDialog`로 "새 목표 시작" 또는 "다른 진행중 목표로 전환" 안내 |
| 동기부여 카드 | 연속 학습일수(스트릭)를 4단계로 시각화하는 캐릭터 카드 노출(§8) |
| 관련 파일 | `HomeScreen.kt`, `HomeViewModel.kt`, `UiStateHome.kt` |

#### 3.4.2 라이브러리 (`a4_2_library`)
| 항목 | 내용 |
|---|---|
| 목적 | 과거 학습 기록 탐색 |
| 탭 구성 | **날짜별**(캘린더, 학습한 날짜에 스탬프 표시) / **주제별**(카테고리·문서별 그룹, "진행중만 보기" 필터) |
| 상세 이동 | 기록 카드 탭 시 일별 요약/퀴즈결과 화면으로 이동(읽기 전용) |
| 관련 파일 | `LibraryScreen.kt`, `LibraryViewModel.kt`, `component/calendar/HistoryCalendar.kt` |

#### 3.4.3 / 3.4.4 일별 기록 상세 (`a4_3_daily_summary_detail`, `a4_4_daily_quiz_result`)
라이브러리에서 과거 날짜를 선택했을 때 진입하는 **읽기 전용** 화면으로, 오늘의 요약/퀴즈(§3.5~3.7)와 동일한 API를 날짜 파라미터만 바꿔 재사용합니다(실시간 SSE 생성 없이 이미 생성된 데이터 단순 조회).

#### 3.4.5 목표 추가 (`a4_5_add_goal`)
| 항목 | 내용 |
|---|---|
| 목적 | 온보딩 이후 신규 학습 목표 생성 |
| 진입 경로 | 홈 화면 "+" 버튼(타입 사전 선택) / 완주 축하 화면의 "새 주제 시작하기"(타입 미지정) |
| 흐름 | (타입 미지정 시 선택 화면) → 카테고리 선택 or 파일 업로드 → 난이도·프롬프트 → 학습기간 → 최종확인 → 완료. 온보딩과 달리 알림시간·닉네임 등 1회성 설정 단계는 반복하지 않음 |
| 생성 후 처리 | 새로 만든 목표를 **즉시 현재 활성 목표로 전환**. 2026-07-24 실기기 QA로 확인: 기존에 진행 중이던 목표가 있으면 "새로운 주제로 바뀌었어요!" 메시지와 함께 **사전 경고 없이 즉시 교체**됩니다(§11 참고). |
| 관련 파일 | `AddGoalActivity.kt`, `AddGoalViewModel.kt` |

### 3.5 오늘의 요약 (`ui/screen/b1_summary`)
| 항목 | 내용 |
|---|---|
| 목적 | AI가 생성하는 오늘의 학습 콘텐츠(요약글) 열람 |
| 생성 방식 | SSE로 실시간 스트리밍(청크 단위로 마크다운 텍스트 누적 렌더링) → 완료 후 최종 데이터 재조회 + 퀴즈 프리페치 |
| 최초 1회 가이드 | 퀴즈를 처음 시작하는 사용자에게는 안내 화면(B_GuideScreen)을 먼저 보여줌("다시 보지 않기" 옵션) |
| 예외 처리 | 목표 기간 만료/완료 시 홈으로 이동, 이미 생성된 요약이 있으면 재생성 대신 조회로 폴백 |
| 관련 파일 | `SummaryActivity.kt`, `SummaryViewModel.kt` |

### 3.6 오늘의 퀴즈 (`ui/screen/b2_quiz`)
| 항목 | 내용 |
|---|---|
| 문제 유형 | OX(참/거짓), MCQ(객관식) 2종 |
| 채점 방식 | **문항별 즉시 채점**(제출 즉시 정답·해설 반환) 후 자동으로 다음 문제 진행. 마지막 문제 후 세트 전체를 서버에 최종 제출 |
| 이탈 처리 | 첫 문제에서 나가기 시 확인 다이얼로그, 세트를 다 풀지 않고 이탈하면 이탈 이벤트 기록 |
| 관련 파일 | `QuizActivity.kt`, `QuizViewModel.kt` |

### 3.7 퀴즈 결과 (`ui/screen/b3_quiz_result`)
| 항목 | 내용 |
|---|---|
| 흐름 | 정답 개수 발표 → 문항별 정답/오답/해설 상세 → (선택) 요약 원문 재열람 → 종료 화면 |
| 완주 시 | 목표를 완료했다면 종료 화면 대신 **완주 축하 화면**으로 분기("새 주제 시작하기" 또는 "홈으로") |
| 관련 파일 | `QuizResultActivity.kt`, `QuizResultViewModel.kt` |

### 3.8 마이페이지 (`ui/screen/c1_mypage`)
| 항목 | 내용 |
|---|---|
| 제공 기능 | 현재 학습 목표 표시 및 전환(목표목록 이동), 알림(푸시) 설정 토글, 계정 정보(로그인 방식·이메일) 조회, 이용약관/고객센터 링크, 앱 버전 표시, 로그아웃, 회원탈퇴 |
| 알림 토글 로직 | 기기 알림권한 + 서버 설정을 함께 판단하여 최종 활성 여부 계산, 거부 이력이 있으면 설정화면 유도 |
| 관련 파일 | `MyPageActivity.kt`, `MyPageViewModel.kt` |

### 3.9 목표 목록 (`ui/screen/c2_goal_list`)
| 항목 | 내용 |
|---|---|
| 목적 | 사용자가 생성한 모든 목표(완료 포함) 조회 및 활성 목표 전환 |
| 동작 | 카드 탭 → 변경 확인 오버레이 → 확인 시 해당 목표로 즉시 전환(신규 생성 아님) |
| 제약 | **목표 삭제 기능 없음**(전환만 가능, §11 참고) |
| 관련 파일 | `GoalListActivity.kt`, `GoalListViewModel.kt` |

### 3.10 사용자 정보 수정 (`ui/screen/c3_edit_user_info`)
| 항목 | 내용 |
|---|---|
| 수정 가능 항목 | 닉네임(1~10자, 한글/영문/숫자/공백), 출근·퇴근 알림 시간, 하루 학습량(문제 수 3/5/7/10 ↔ 분 5/7/10/15 상호 환산) |
| 특이사항 | 앱 내에서 **닉네임을 실제로 입력할 수 있는 유일한 화면**(§11 참고). 변경사항이 없으면 저장 확인 팝업을 띄우지 않음 |
| 관련 파일 | `EditUserInfoActivity.kt`, `EditUserInfoViewModel.kt` |

### 3.11 공용 컴포넌트 (`ui/screen/common_screen`, `ui/aa0_base`)
- `BaseActivity`: 모든 주요 Activity가 상속. 네트워크 끊김을 전역 감지해 `FullScreenErrorModal`을 자동 노출하고, 각 화면이 `onRetryClick()`으로 재조회 로직을 구현.
- `AuthBlockingOverlay`, `ErrorState`, `LoadingScreen`, `PopupOverlay` 등 공용 UI 상태 컴포넌트.

## 4. 도메인(Domain) 레이어

### 4.1 주요 모델
| 모델 | 설명 |
|---|---|
| `UserGoal` | 학습 목표(주제/기간/난이도/진행상태) |
| `DomainGoalType` | 목표 유형: `CATEGORY`(카테고리 기반) / `DOCUMENT`(PDF 업로드 기반) |
| `GoalCategory` | 카테고리 트리 노드 |
| `Difficulty` | `EASY` / `MEDIUM` / `HARD` |
| `UserQuizStatus` | 오늘 풀이 여부·최초 여부·남은 쿠폰 수·목표 세트 수 등 홈 화면 상태의 핵심 소스 |
| `DailySummary` / `CalendarDailyItem` | 학습 기록(요약/캘린더) 모델 |
| `PdfDocumentSummary` | PDF 문서 요약 결과 |
| `RequestPromptOption` | 온보딩/목표추가에서 선택하는 사전정의 학습 요청 프롬프트 10종 |
| `SseEvent` 계열 | SSE 스트리밍 이벤트/예외 모델 |

### 4.2 주요 UseCase
| UseCase | 역할 |
|---|---|
| `AutoLoginUseCase` / `LogoutUseCase` / `SessionManager` | 자동 로그인, 로그아웃, 세션 만료 시 전역 이벤트 발행 |
| `GetGoalListUseCase` / `CreateGoalUseCase` / `UpdateGoalUseCase` | 목표 조회/생성/전환 |
| `GetDocumentsUseCase` / `IssuePresignedUrlUseCase` / `UploadDocumentUseCase` | PDF 문서 업로드 파이프라인(파일 읽기 → presigned URL 발급 → S3 업로드 → 서버 등록) |
| `StreamDocumentProcessingUseCase` / `StreamPdfSummaryUseCase` / `StreamDailySummaryUseCase` | SSE 스트림 구독 |
| `GetPushNotificationStatusUseCase` | 기기 권한 + 서버 설정을 결합한 최종 알림 활성 여부 계산 |
| `GetCategoriesUseCase` / `GetAccountInfoUseCase` / `GetUserNameUseCase` / `UpdateCommuteTimeUseCase` | 사용자·카테고리 정보 조회/수정 |
| `ObserveDateChangeUseCase` / `EmitGoalRefreshUseCase` | 자정 날짜 변경 감지, 전역 데이터 갱신 시그널 |

## 5. 데이터(Data) 레이어

### 5.1 로컬 저장 (Room 없음)
로컬 영속화는 Room이 아닌 **DataStore(Preferences) + SharedPreferences** 조합으로 처리됩니다.

| 저장소 | 용도 |
|---|---|
| `GoalTrackingDataStore`(DataStore) | 완주한 목표 ID 집합, 직전 완주 목표 스냅샷(Analytics 이벤트 복원용) |
| `QuizTrackingDataStore`(DataStore) | 완료/진입한 문서 ID 집합(퀴즈 재진입 유형 판정), 누적 풀이 문항 수 |
| `TokenLocalDataSource`(SharedPreferences) | accessToken/refreshToken, 소셜 로그인 provider |
| `HomePreference`(SharedPreferences) | 오늘 소비한 요약 여부, 오늘의 랜덤 음식, 쿠폰으로 활성화된 목표 ID |
| `PrefsUtil` / `FcmTokenStore`(SharedPreferences) | 온보딩 완료 플래그, 알림 거부 이력, FCM 토큰 캐시 |

> 앱 재설치 시 위 로컬 이력은 모두 소실되며, 이는 의도된 동작으로 코드 주석에 명시되어 있습니다.

### 5.2 네트워크 API (Retrofit, Base URL: `BuildConfig.BASE_DOMAIN`)
| 서비스 | 주요 엔드포인트 |
|---|---|
| `AuthApiService` | 로그인(`/auth/oauth/register`), 토큰 재발급(`/v1`·`/v2 users/reissue`), 로그아웃, 회원탈퇴 |
| `UserApiService` | 푸시 설정, 출퇴근 시간, 닉네임, 계정정보, 온보딩 완료 여부(V1/V2 병존) |
| `GoalApiService` | 목표 생성/조회/전환 |
| `QuizApiService` | 퀴즈 가이드/상태/제출/세트완료/광고보상 + 테스트(ADMIN) 전용 엔드포인트 |
| `CategoryApiService` | 카테고리 트리, 오늘의 카테고리 문서 |
| `DocumentApiService` | S3 presigned URL 발급, 문서 등록/조회, 문서 요약 조회 |
| `HistoryApiService` | 주제별/날짜별/캘린더(스탬프·스트릭) 학습 이력 |
| `NotificationApiService` | FCM 디바이스 토큰 등록/삭제 |

**SSE 엔드포인트**(Retrofit이 아닌 OkHttp 기반 자체 `SseClient` 사용): 문서 OCR 처리 상태, PDF 요약 생성, 카테고리 일일 요약 생성 — 총 3곳.

### 5.3 공통 네트워크 구조
- `ApiResponse<T,D>` — 서버 공통 응답 포맷(`{code, message, details, data}`).
- `AuthInterceptor` — 인증이 필요 없는 요청(재발급, S3 presigned 등) 제외 나머지에 accessToken을 자동 첨부.
- **401 발생 시 자동 재발급 후 재시도**: `BaseRepository.handleUnauthorizedVer2()`가 refreshToken으로 재발급 시도, 실패 시 세션만료 처리(`SessionManager.expireSession()`) → 전역 이벤트로 모든 화면에서 로그인 화면으로 강제 이동.

## 6. 인증/로그인

- **카카오**: `v2-user` SDK, 카카오톡 앱 로그인 우선 + 웹 로그인 폴백. 딥링크 스킴으로 `AuthCodeHandlerActivity` 등록.
- **구글**: `play-services-auth`, idToken 방식.
- **세션 관리**: accessToken/refreshToken을 SharedPreferences에 저장(메모리 캐시 병행). 401 응답 시 자동 재발급, 재발급도 실패하면 전역 세션만료 이벤트로 모든 화면에서 로그인 화면 강제 이동.
- **자동 로그인**: 스플래시에서 저장된 refreshToken 유효성만 확인하여 재로그인 없이 진입.
- **회원탈퇴**: 로그인 화면·마이페이지 양쪽에서 가능.

## 7. 알림 (FCM / OneSignal)

- **실제 푸시 수신·표시는 FCM이 전담**: `TeumFcmService`가 토큰 갱신 시 로컬 저장 + 서버 동기화를 수행하고, 메시지 수신 시 알림 채널을 생성해 직접 알림을 표시.
- **OneSignal은 시스템 알림 권한 요청 UX 용도로만 사용**: 온보딩 중 권한 팝업을 띄우는 데만 쓰이며, OneSignal을 통한 실제 푸시 발송/수신이나 외부 사용자ID 연동 코드는 없습니다.
- **디바이스 토큰 생명주기**: 앱 시작 시 로컬 저장 → 온보딩 완료 시 서버 등록 → 로그아웃/세션만료 시 서버에서 삭제.
- **알림 발송 트리거(언제 보낼지)는 백엔드에 있으며, 이 저장소에는 클라이언트의 수신·표시 로직만 존재**합니다.
- 자정에는 `DateChangeReceiver`가 홈 상태를 재조회하도록 트리거합니다(알림은 아니지만 일일 리셋과 연결됨).

## 8. 게이미피케이션 현황

현재 3가지 경량 게이미피케이션 요소가 구현되어 있으며, **적립형 재화(포인트/코인) 시스템은 없습니다.**

| 요소 | 설명 | 비고 |
|---|---|---|
| **스탬프** | 하루 학습(퀴즈 세트)을 처음으로 완료한 날에만 적립. 캘린더에 표시 | 재도전 시 미적립 |
| **스트릭** | 연속 학습일수를 홈 화면 카드에 4단계(0일 / 1~6일 / 7~29일 / 30일+)로 시각화 | 스트릭이 끊겨도 페널티나 "프리즈" 같은 보호 수단 없음 |
| **쿠폰** | 하루 학습을 다 마친 뒤 보상형 광고를 시청하면 지급, 그날 한정으로 1회 추가 학습(요약+퀴즈) 가능 | 이월/누적/거래 불가한 "당일 1회용 이용권" 성격 |

## 9. 수익 모델 현황

- **결제/과금 관련 코드가 전혀 없습니다**(Google Play Billing, 인앱결제, 구독, PG 연동 미포함, `libs.versions.toml`에도 관련 의존성 없음).
- 현재 앱은 완전 무료이며, **AdMob 전면광고·보상형광고**가 유일한 수익모델로 추정됩니다.

## 10. 버전 및 주요 의존성

- `versionCode 19`, `versionName "1.1.8"` (`app/build.gradle.kts`)
- Kotlin 2.1.0 · AGP 8.9.1 · Compose BOM 2025.12.01 · Hilt 2.57.1 · Retrofit 2.9.0 · OkHttp 4.10.0(+SSE) · Kakao SDK v2-user 2.20.0 · DataStore Preferences 1.2.0 · Firebase BOM 34.7.0 · OneSignal [5.1.0, 5.1.99] · Markwon 4.6.2(마크다운 렌더링)
- 테스트: JUnit4, Robolectric, MockK 1.13.8, kotlinx-coroutines-test, Turbine 1.1.0(Flow 테스트)

## 11. 알려진 이슈 및 기술부채

코드 조사 중 발견한, 명세 정확성을 위해 남겨두는 사실 기반 메모입니다(이번 작업 범위에서 수정하지 않음). 1~6번은 코드 조사로 발견, 7~11번은 2026-07-24 실기기 QA(로컬 Claude Code가 adb로 기기를 직접 조작해 검증)로 새로 발견되었습니다.

1. **Room 미사용**: CLAUDE.md는 `data/db`, `Dao`, `Entity`, "Room에서 `fallbackToDestructiveMigration()` 금지" 등을 컨벤션으로 명시하고 `teum-room-migration` 스킬까지 존재하지만, 실제 코드에는 Room 의존성이 없고 `@Entity`/`@Dao` 매치도 0건입니다. 로컬 DB 도입을 대비한 선언적 컨벤션으로 보이며, 현재 구현과는 무관합니다.
2. **`a4_6_guide_expired_goal` 미구현**: CLAUDE.md 디렉터리 컨벤션과 문자열 리소스(`title_activity_guide_expired_goal`)에는 남아있지만 실제 화면/Activity가 존재하지 않습니다. 목표 만료/완료 안내는 현재 **홈 화면 다이얼로그 + SSE 에러코드(`GOAL-002`/`GOAL-003`) 리다이렉트**로 대체 처리되고 있습니다.
3. **온보딩 닉네임 입력 단절**: `OnboardingSetUserNickNameScreen.kt`가 존재하지만 `OnBoardingNavHost`에 연결되어 있지 않아 온보딩 중 닉네임을 입력할 방법이 없습니다(코드상 이름 등록 단계도 주석 처리됨). 닉네임은 소셜 로그인 기본값을 쓰다가 마이페이지에서만 변경 가능합니다. → 실기기 QA로 재확인됨.
4. **목표 삭제 기능 없음 + 전환/교체 시 경고 없음**: 목표 목록 화면에서 활성 목표 전환만 가능하고 삭제는 불가능합니다(코드 조사로 확인). 추가로 실기기 QA에서 확인: 홈의 "+" 버튼으로 새 목표를 만들면 기존에 진행 중이던 목표가 **사전 경고 다이얼로그 없이 즉시 교체·소실**됩니다 — 진행 중이던 학습 맥락을 실수로 잃을 위험이 있는 데이터 손실 리스크입니다.
5. **신구 버전 코드 병존**: `ApiResult`(구)/`ApiResultV2`(신), `DomainGoalType_v1`/`GoalTypeUiState`(구/신), `CreateGoalUseCase`/`CreateGoalUseCaseV1` 등이 함께 존재합니다. 화면에서는 신버전이 쓰이는 경우가 대부분이나, 리팩터링 시 확인이 필요합니다.
6. **미사용 레거시 코드**: `ui/screen/unused_welcome`(전체), `webView/KakaoLoginWebViewActivity`, `ui/component/modal/CupponModal.kt` 등은 다른 코드에서 참조되지 않는 죽은 코드로 보입니다.
7. **온보딩 카테고리 선택 라벨-데이터 불일치**: 온보딩 중 "Android 개발" 카테고리를 선택했으나 실제로 저장·반영된 값은 "iOS 개발 > 테스트 > Swift 언어"였습니다(온보딩 요약 화면과 마이페이지 양쪽에서 동일하게 재현). 다른 카테고리(예: "IT테스트" → "Xcode")는 breadcrumb가 정확히 표시되어, 카테고리 트리의 특정 구간에 국한된 데이터 바인딩 버그로 추정됩니다.
8. **알림 권한 체크박스 미동기화**: 온보딩의 알림 동의 체크박스가 OS 알림 권한 상태와 실시간으로 동기화되지 않습니다. 설정 앱에서 권한을 켜고 돌아와도 앱은 계속 "꺼짐"으로 인식하며, 앱을 재시작해야 정상 반영됩니다.
9. **홈 화면 스트릭 카운터 실시간 미갱신**: 퀴즈 세트를 완료한 직후 홈 화면 상단바의 스트릭 카운터가 즉시 갱신되지 않고 이전 값을 유지합니다. 라이브러리(캘린더) 탭으로 이동하면 정상 반영된 값을 확인할 수 있어, 홈 상단바 카운터만 별도로 실시간 리프레시되지 않는 것으로 보입니다.
10. **(경미) 요약 스트리밍 중 일시적 띄어쓰기 누락**: SSE로 요약이 스트리밍되는 중간 청크 단계에서 한글 띄어쓰기가 일시적으로 사라지는 현상이 관찰되었으나, 스트리밍이 끝난 최종 텍스트는 정상입니다(순수 렌더링 단계의 코스메틱 이슈).
11. **(경미) 마이페이지 초기 로딩 스피너 부재**: 마이페이지 최초 진입 시 약 2~3초간 스켈레톤/로딩 인디케이터 없이 빈 화면이 표시된 뒤 콘텐츠가 로드됩니다.

---

*이 문서는 코드 조사 시점의 스냅샷입니다. 기능이 변경되면 함께 갱신해 주세요.*
