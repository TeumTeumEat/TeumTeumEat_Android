---
name: teum-feature-scaffolder
description: 틈틈잇 앱에 새로운 화면(Activity/Screen) 또는 기능(feature)을 추가할 때 Clean Architecture 3계층(Presentation/Domain/Data) 파일 골격을 자동 생성하는 스킬. 사용자가 "새 화면 추가", "새 feature 만들어줘", "a4_7_xxx 액티비티 만들어줘", "○○ 기능 구현해줘", "ViewModel부터 Repository까지" 같은 요청을 할 때 반드시 사용할 것. 기존 디렉터리 네이밍 컨벤션(예: a4_main, b2_quiz, c3_edit_user_info)을 준수하여 파일을 배치한다.
---

# 틈틈잇 Feature Scaffolder

새 기능을 Clean Architecture 3계층에 맞춰 생성한다.

## 디렉터리 네이밍 규칙

기존 구조를 준수한다:
- `a0_splash`, `a1_login`, `a2_on_boarding`, `a4_main`
- `a4_main/a4_3_daily_summary_detail`, `a4_main/a4_5_add_goal` (서브 스크린)
- `b1_summary`, `b2_quiz`, `b3_quiz_result` (퀴즈 플로우)
- `c1_mypage`, `c2_goal_list`, `c3_edit_user_info` (마이페이지)

새 화면은 영역 알파벳(a/b/c) + 순번 + 의미 있는 snake_case 이름을 조합한다.

## 생성할 파일 목록

### Presentation Layer (`ui/screen/<feature>/`)
1. `<Feature>Activity.kt` — ComponentActivity + setContent
2. `<Feature>Screen.kt` — @Composable 함수, state/event 람다 인자
3. `<Feature>ViewModel.kt` — @HiltViewModel + MutableStateFlow/StateFlow
4. `<Feature>UiState.kt` — data class, 초기값 포함
5. `<Feature>UiEvent.kt` — sealed interface, 일회성 이벤트 처리

### Domain Layer (`domain/usecase/`, `domain/repository/`)
6. `<Action>UseCase.kt` — suspend operator fun invoke() 형태
7. `<Feature>Repository.kt` — interface (Domain에 위치)

### Data Layer (`data/repository/`, `data/db/`, `data/mapper/`)
8. `<Feature>RepositoryImpl.kt` — Repository 구현체
9. `<Feature>Entity.kt` — @Entity (Room)
10. `<Feature>Dao.kt` — @Dao, Flow 반환
11. `<Feature>Mapper.kt` — Entity ↔ Domain 모델 변환

### DI
12. `di/<Feature>Module.kt` — @Module + @Binds (Repository 연결)

## 작성 규칙

- 모든 파일 상단에 패키지 + import 포함
- 모든 public API에 KDoc 주석
- ViewModel은 Android 프레임워크 참조 금지 (Context/Resources 등)
- UI 상태는 `private val _uiState = MutableStateFlow(...)` + `val uiState = _uiState.asStateFlow()`
- Coroutines는 `viewModelScope.launch(ioDispatcher)` 형태로 Dispatcher 명시
- Repository는 Flow 반환을 기본으로 하되, 단건 조회는 suspend fun 허용

## 작업 흐름

1. 사용자에게 feature 이름과 디렉터리 위치 확인
2. 위 12개 파일을 순서대로 생성 (Presentation → Domain → Data → DI)
3. AndroidManifest.xml에 `<activity>` 등록 안내
4. 필요한 의존성이 `build.gradle.kts`에 있는지 확인

## 참고

상세 템플릿은 `templates/` 폴더의 각 `.template` 파일을 참조한다.