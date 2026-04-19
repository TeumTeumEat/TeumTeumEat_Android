---
name: teum-viewmodel-test
description: 틈틈잇 ViewModel에 대한 단위 테스트를 MockK + Turbine + CoroutineTestRule 조합으로 표준화된 형식으로 작성하는 스킬. StateFlow 기반 UI 상태 검증 패턴을 반영한다. 사용자가 "이 ViewModel 테스트 작성해줘", "유닛 테스트 추가", "테스트 코드 만들어줘", "커버리지 올리자", "테스트 보일러플레이트" 같은 요청을 할 때 반드시 사용할 것. ViewModel 외 UseCase/Repository 테스트에도 응용 가능하다.
---

# 틈틈잇 ViewModel Test

ViewModel 단위 테스트를 표준 패턴으로 작성한다.

## 테스트 도구 스택

- **JUnit 4** — 기본 러너
- **MockK** — UseCase/Repository 모킹
- **Turbine** — StateFlow 순차 검증
- **kotlinx-coroutines-test** — TestDispatcher, runTest
- **Robolectric** — (필요 시) Android 리소스 접근

## 표준 구조

### 1. 테스트 클래스 헤더

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class <Feature>ViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getXxxUseCase: GetXxxUseCase
    private lateinit var viewModel: <Feature>ViewModel
}
```

### 2. @Before 설정

```kotlin
@Before
fun setUp() {
    getXxxUseCase = mockk(relaxed = true)
    viewModel = <Feature>ViewModel(getXxxUseCase)
}
```

### 3. 필수 테스트 케이스 (3종 기본)

1. **성공 경로** — UseCase 성공 응답 → UiState.success 검증
2. **실패 경로** — UseCase 예외 → UiState.error 검증
3. **로딩 상태** — 초기 진입 시 isLoading = true 검증

### 4. Given-When-Then 구조

```kotlin
@Test
fun `goal 목록 조회 성공 시 UiState에 반영된다`() = runTest {
    // Given
    val mockGoals = listOf(Goal(id = 1, title = "운동"))
    coEvery { getGoalsUseCase() } returns flowOf(mockGoals)

    // When
    viewModel.loadGoals()

    // Then
    viewModel.uiState.test {
        val state = awaitItem()
        assertEquals(mockGoals, state.goals)
        assertFalse(state.isLoading)
    }
}
```

## MainDispatcherRule 템플릿

프로젝트에 없으면 아래를 함께 생성:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

## 작업 흐름

1. 대상 ViewModel 파일 읽기
2. 주입받는 UseCase/Repository 목록 추출
3. UiState 구조 분석 (어떤 필드가 있는지)
4. public 메서드별로 테스트 케이스 설계:
    - 성공/실패/로딩 3종 기본
    - 엣지 케이스 (빈 데이터, 네트워크 단절 등)
5. 위 템플릿에 맞춰 테스트 파일 생성
6. `build.gradle.kts` 의존성 확인:
    - `io.mockk:mockk`
    - `app.cash.turbine:turbine` (미설치 시 추가 권장)
    - `kotlinx-coroutines-test`

## 주의사항

- `coEvery` (suspend 함수) vs `every` (일반 함수) 구분
- Flow 반환 함수는 `returns flowOf(...)` 또는 `returns emptyFlow()`
- StateFlow 검증 시 `viewModel.uiState.test { ... }` (Turbine) 권장
- 테스트 이름은 한글 backtick(\` \`) 사용 가능, 의도 명확히 표현