---
name: teum-code-reviewer
description: 틈틈잇 프로젝트의 코드가 프로젝트 컨벤션(XML 금지, ViewModel의 Android 프레임워크 직접 참조 금지, LiveData/RxJava/GlobalScope 금지, MutableStateFlow 캡슐화 등)을 준수하는지 검증하고 리팩터링안을 제시하는 스킬. 사용자가 "코드 리뷰해줘", "컨벤션 맞는지 확인", "이 코드 문제 있어?", "리팩터링 제안해줘", "PR 올리기 전 검증"같은 요청을 할 때 반드시 사용할 것. 커밋 직전 자가 검증 용도로도 사용한다.
---

# 틈틈잇 Code Reviewer

코드를 프로젝트 컨벤션 체크리스트로 검증한다.

## 필수 체크리스트

### UI 레이어
- [ ] XML 레이아웃 파일(activity_*.xml, fragment_*.xml)을 신규 생성하지 않았는가?
- [ ] Compose 함수가 state를 인자로 받고 event를 람다로 노출하는 UDF 구조인가?
- [ ] Preview 어노테이션이 포함되어 있는가? (재사용 컴포넌트의 경우)

### ViewModel 레이어
- [ ] `Context`, `Resources`, `Intent`, `View` 등 Android 프레임워크를 직접 참조하지 않는가?
- [ ] `MutableStateFlow`는 `private`이고, 외부에는 `StateFlow`(`.asStateFlow()`)로 노출되는가?
- [ ] 일회성 이벤트는 `Channel` + `receiveAsFlow()` 또는 `SharedFlow(replay=0)`를 사용하는가?
- [ ] Repository를 직접 호출하지 않고 UseCase를 경유하는가?
- [ ] `@HiltViewModel` + `@Inject constructor`로 주입되는가?

### 비동기/동시성
- [ ] `GlobalScope` 사용이 없는가?
- [ ] `runBlocking`을 프로덕션 코드에서 사용하지 않는가?
- [ ] `LiveData`, `RxJava`, `Observable`을 사용하지 않는가?
- [ ] Dispatcher가 `@IoDispatcher` 등 Qualifier로 주입되는가? (하드코딩 금지)

### Domain/Data 레이어
- [ ] Domain 모듈에 Android 의존성이 없는가?
- [ ] Repository 인터페이스가 Domain에, 구현체가 Data에 위치하는가?
- [ ] Room Entity와 Domain 모델이 Mapper로 분리되어 있는가?
- [ ] Flow가 UI까지 일관되게 전파되는가?

### 앱 특성
- [ ] 세션 중단 대비 상태 저장(`rememberSaveable`, Room 즉시 저장)이 구현되어 있는가?
- [ ] 네트워크 실패를 예외 대신 `Result`/`sealed class`로 처리하는가?

## 리뷰 출력 형식

---
name: teumteum-review
description: |
틈틈잇(teumteumeat) Android 프로젝트 전용 코드 리뷰 스킬.
사용자가 "코드 리뷰해줘", "변경 내역 리뷰해줘", "PR 리뷰해줘", "diff 리뷰해줘",
"작성한 코드 봐줘" 라고 말하면 반드시 이 스킬을 사용한다.
git diff 또는 변경된 파일을 분석하여 틈틈잇 프로젝트 컨벤션 및
Clean Architecture 관점에서 우선순위별 리뷰 코멘트를 생성한다.
---

# teumteum-review 스킬

틈틈잇 Android 프로젝트의 변경 코드를 분석하여
[문제 분석 - 해결 코드 - 검증 방법] 구조의 리뷰 코멘트를 생성하는 스킬.

---

## 프로젝트 컨텍스트

```
Stack   : Kotlin, Jetpack Compose, Hilt, Room, DataStore, Coroutines+Flow
Arch    : MVVM + Clean Architecture (Presentation / Domain / Data)
minSdk  : 26  |  targetSdk : 36
Build   : ./gradlew assembleDebug
Test    : ./gradlew test
```

### 레이어 의존 방향 (위반 시 P0 블로커)
```
Presentation  →  Domain  ←  Data
 (UI / VM)    (UseCase)   (Repo / API / DB)
```

### 강제 컨벤션 (위반 시 P0 블로커)
| 항목 | 허용 | 금지 |
|---|---|---|
| UI | Jetpack Compose 전용 | XML Layout |
| 상태 노출 | `StateFlow` | `LiveData`, `MutableStateFlow` 직접 노출 |
| 비동기 | `viewModelScope`, `CoroutineScope` | `GlobalScope`, `RxJava` |
| ViewModel | UseCase 호출만 | Android 프레임워크 직접 참조 |
| Domain | 순수 Kotlin | `import android.*` |

---

## 실행 절차

### STEP 1 — 변경 내역 수집
사용자가 diff 또는 파일을 직접 제공한 경우 → STEP 2로 이동.
제공하지 않은 경우 아래 명령으로 수집:

```bash
# 변경 파일 목록
git status

# 상세 diff (컨텍스트 5줄)
git diff HEAD~1 --unified=5

# 또는 스테이징된 변경
git diff --cached --unified=5
```

### STEP 2 — 레이어 식별
변경 파일 경로를 아래 매핑 테이블로 레이어·도메인 분류:

| 경로 키워드 | 레이어 | 도메인 |
|---|---|---|
| `ui/screen/a0_splash` | Presentation | Splash |
| `ui/screen/a1_login` | Presentation | Login |
| `ui/screen/a2_on_boarding` | Presentation | OnBoarding |
| `ui/screen/a4_main` | Presentation | Main |
| `ui/screen/b1_summary` | Presentation | Summary |
| `ui/screen/b2_quiz` | Presentation | Quiz |
| `ui/screen/b3_quiz_result` | Presentation | QuizResult |
| `ui/screen/c1_mypage` | Presentation | MyPage |
| `ui/screen/c2_goal_list` | Presentation | Goal |
| `ui/screen/c3_edit_user_info` | Presentation | UserInfo |
| `domain/usecase` | Domain | - |
| `domain/entity` / `domain/model` | Domain | - |
| `domain/repository` | Domain | - |
| `data/repository` | Data | - |
| `data/remote` / `data/api` | Data | - |
| `data/local` / `data/db` | Data | - |
| `utils/firebase` | Data | Notification |
| `build.gradle.kts` / `libs.versions.toml` | Build | - |

### STEP 3 — 우선순위별 이슈 분류

아래 체크리스트를 순서대로 검토한다.

---

#### 🔴 P0 — 블로커 (머지 전 필수 수정)

**[아키텍처 위반]**
- [ ] Presentation → Data 직접 의존 (Repository 직접 import)
- [ ] ViewModel에서 `Context`, `Activity`, `View` 참조
- [ ] Domain 레이어에 `import android.*` 존재
- [ ] UseCase가 다수의 책임을 가짐 (클래스명으로 단일 동작 표현 불가)

**[컨벤션 위반]**
- [ ] `GlobalScope` 사용
- [ ] `LiveData` / `RxJava` 사용
- [ ] `MutableStateFlow` 를 public으로 노출
- [ ] XML Layout 사용 (Compose 전용 프로젝트)
- [ ] `StateFlow` 대신 일반 변수로 UI 상태 관리

**[보안·안전]**
- [ ] API Key / 토큰 하드코딩 (BuildConfig 미사용)
- [ ] NPE 가능성 있는 강제 non-null (`!!`) 남용
- [ ] 경쟁 조건(Race Condition) 유발 코드

---

#### 🟡 P1 — 이번 PR 내 수정 권장

**[Compose]**
- [ ] `remember` 누락 등 으로 불필요한 recomposition 유발
- [ ] 람다를 Composable 본문에서 매번 새 인스턴스로 생성
- [ ] `LaunchedEffect` key 미지정 또는 잘못 지정
- [ ] Side Effect를 Composable 최상단에서 직접 실행 (LaunchedEffect 미사용)
- [ ] `derivedStateOf` 없이 복잡한 State 연산 반복

**[비동기 처리]**
- [ ] `collect` 없이 Flow 방치
- [ ] `repeatOnLifecycle` 없이 UI에서 Flow collect (메모리 누수)
- [ ] suspend 함수 내 `withContext` 없이 블로킹 IO 호출
- [ ] Exception 처리 없는 `launch` 블록 
- 📌 비동기 처리 리뷰 시 아래 [코루틴 패턴 레퍼런스]를 기준으로 판단한다.

## 코루틴 패턴 레퍼런스

비동기 처리 리뷰 시 아래 기준으로 올바른 패턴 여부를 판단한다.

### Scope 규칙

| 사용 위치 | 올바른 Scope | 금지 |
|---|---|---|
| ViewModel | `viewModelScope.launch` | `GlobalScope` |
| Composable 진입 시 | `LaunchedEffect` | 본문 직접 호출 |
| Composable 이벤트 응답 | `rememberCoroutineScope` | `GlobalScope` |

### Dispatcher 책임

| 레이어 | Dispatcher | 이유 |
|---|---|---|
| Data (Repository / API / DB) | `withContext(Dispatchers.IO)` | 블로킹 IO 전용 스레드 |
| Domain (UseCase) | 별도 지정 없음 | IO는 Data에 위임 |
| Presentation (ViewModel / UI) | `Dispatchers.Main` (기본값 유지) | UI 업데이트, StateFlow emit |

### 패턴 선택 기준

```
작업 유형                     올바른 패턴
──────────────────────────────────────────────────────
1회성 데이터 로드             launch + suspend + try/catch
지속 관찰 (DB / 실시간)       Flow → stateIn(WhileSubscribed(5_000))
독립 작업 동시 실행           async/await + supervisorScope
네비게이션 / 토스트 이벤트    SharedFlow(replay=0)
시간 제한 작업                withTimeout
불안정 네트워크 재시도         retry + 지수 백오프
```

**[코드 품질]**
- [ ] 함수 길이 50줄 초과 (분리 필요)
- [ ] 매직 넘버·스트링 리터럴 미상수화
- [ ] 동일 로직 2곳 이상 중복 (DRY 위반)
- [ ] Sealed Class 대신 Boolean/Int 플래그로 상태 분기

---

#### 🟢 P2 — 다음 이터레이션 개선 권장

**[가독성]**
- [ ] 함수·클래스 KDoc 누락
- [ ] 복잡한 비즈니스 로직 인라인 주석 부재
- [ ] 변수·함수명이 의도를 명확히 표현하지 않음
- [ ] 불필요한 주석 (코드로 표현 가능한 내용)

**[테스트]**
- [ ] 새 UseCase 유닛 테스트 누락
- [ ] ViewModel 상태 전이 테스트 누락
- [ ] 엣지 케이스 (빈 리스트, 네트워크 실패) 미검증

---

### STEP 4 — 틈틈잇 도메인 전용 체크

변경된 도메인에 따라 아래 항목을 추가 검토한다.

#### 학습 플로우 (Summary → Quiz → QuizResult)
- [ ] 퀴즈 세션 상태가 Sealed Class로 명확히 모델링되었는가
- [ ] 앱 백그라운드 전환 시 학습 진행 상태 복원 처리 (`SavedStateHandle`)
- [ ] 학습 완료 이벤트 중복 발송 방지 처리
- [ ] 퀴즈 정답 제출 후 UI 반응이 단방향 데이터 흐름을 유지하는가

#### 알림 (FCM / OneSignal)
- [ ] 딥링크 처리 시 Activity 백스택이 올바르게 구성되는가
- [ ] 알림 권한 미허용 시 Graceful Degradation (앱 크래시 없음)
- [ ] 포그라운드 / 백그라운드 알림 수신 분기 처리

#### 인증 (카카오 / Google)
- [ ] 토큰 만료 → 자동 갱신 → 재시도 로직 (OkHttp Authenticator)
- [ ] 로그아웃 시 DataStore 전체 초기화 여부
- [ ] 소셜 로그인 콜백에서 UI 스레드 안전성
- [ ] 인증 실패 시 사용자에게 UI 피드백을 제공하는가

#### 광고 (AdMob)
- [ ] 광고 로드 실패 시 UI 레이아웃 깨짐 방지 (fallback 처리)
- [ ] 테스트 디바이스 ID가 release BuildConfig에 포함되지 않았는가
- [ ] 광고 요청 시 사용자 동의 여부 체크 (GDPR / 개인정보)

#### DataStore
- [ ] 직렬화 가능한 타입만 저장 (복잡한 객체는 직렬화 처리 확인)
- [ ] `IOException` 등 예외 처리 (`catch` 연산자 활용 여부)

---

### STEP 5 — 리뷰 코멘트 생성

발견된 이슈 각각을 아래 형식으로 출력한다.
이슈가 없는 우선순위는 ✅ 통과로 표기한다.

```

## 코드 리뷰 결과

### 변경 범위
- 레이어: [Presentation / Domain / Data / Build]
- 도메인: [도메인명]
- 변경 파일 수: N개

---

### 🔴 P0 블로커 (N건)

#### [이슈 제목]
📍 위치: `파일명.kt` L{줄번호}

**문제 분석**
[현재 코드의 문제점과 리스크를 2-3문장으로 설명]

**현재 코드**
```kotlin
// 문제가 있는 코드 스니펫
```

**해결 코드**
```kotlin
// 수정 제안 코드 (import 포함, 타입 명시)
```

**검증 방법**
- [ ] [어떤 테스트 또는 동작으로 수정 여부를 확인할 수 있는지]

---

### 🟡 P1 권장 수정 (N건)
[동일 형식 반복]

---

### 🟢 P2 개선 제안 (N건)
[동일 형식 반복]

---

### ✅ 잘 작성된 부분
[칭찬할 점을 1-3가지 구체적으로 언급]

---

### 실재 개선 효과
[실제 코드 개선으로 개선된 점을 언급]

### 📊 리뷰 요약
| 우선순위 | 건수 | 상태 |
|---|---|---|
| 🔴 P0 블로커 | N | 머지 전 필수 수정 |
| 🟡 P1 권장 | N | 이번 PR 내 수정 권장 |
| 🟢 P2 개선 | N | 다음 이터레이션 |
```

---

### STEP 6 — 머지 가능 여부 판정

```
P0 건수 = 0  →  "✅ 머지 가능 (P1 수정 후 권장)"
P0 건수 > 0  →  "🚫 머지 블로킹 — P0 {N}건 수정 후 재리뷰 필요"
```

---

## 올바른 리뷰 코멘트 예시

### 🔴 P0 예시

#### ViewModel에서 Context 직접 참조
📍 위치: `QuizViewModel.kt` L42

**문제 분석**
ViewModel이 Android 프레임워크(`Context`)를 직접 참조하면 단위 테스트가 불가능해지고,
화면 회전 시 메모리 누수가 발생할 수 있습니다.
Clean Architecture에서 ViewModel은 프레임워크 독립성을 유지해야 합니다.

**현재 코드**
```kotlin
// ❌ P0: Context 직접 참조
class QuizViewModel(private val context: Context) : ViewModel()
```

**해결 코드**
```kotlin
// ✅ Application Context가 필요한 경우 AndroidViewModel 사용
// 또는 의존성을 UseCase/Repository로 위임
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val getQuizUseCase: GetQuizUseCase
) : ViewModel()
```

**검증 방법**
- [ ] `./gradlew test`로 QuizViewModelTest 통과 확인
- [ ] 화면 회전 후 상태 유지 여부 수동 확인

---

### 🟡 P1 예시

#### LaunchedEffect key 미지정
📍 위치: `SummaryScreen.kt` L88

**문제 분석**
`LaunchedEffect(Unit)`은 컴포지션 진입 시 1회만 실행되므로
`summaryId`가 변경되어도 재실행되지 않습니다.

**현재 코드**
```kotlin
// ⚠️ P1: key가 Unit이므로 summaryId 변경 시 재실행 안 됨
LaunchedEffect(Unit) {
    viewModel.loadSummary(summaryId)
}
```

**해결 코드**
```kotlin
// ✅ summaryId를 key로 지정
LaunchedEffect(summaryId) {
    viewModel.loadSummary(summaryId)
}
```

**검증 방법**
- [ ] 다른 `summaryId`로 화면 재진입 시 새 데이터 로드 확인

---

## 안전 규칙

- `local.properties`, `*.jks`, `*.keystore` 포함 여부 발견 시 → 즉시 경고, 리뷰 중단
- diff에 민감 정보(토큰, 패스워드 패턴) 감지 시 → P0 보안 이슈로 즉시 보고
- 변경 파일이 50개 초과 시 → 도메인별로 분리 리뷰 진행 제안