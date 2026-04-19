# 틈틈잇 — 프로젝트 컨텍스트

## 앱
- 패키지: `com.teumteumeat.teumteumeat`
- minSdk 26 / targetSdk 36 / 버전 상세: `libs.versions.toml` 참조
- 핵심 전제: 5분 이하 세션, 알림 기반 리마인더

## 스택
Kotlin + Jetpack Compose + Hilt + Room + DataStore + Coroutines/Flow
Retrofit/OkHttp · FCM/OneSignal · Firebase(Analytics/Crashlytics) · Kakao SDK
아키텍처: MVVM + Clean Architecture (Presentation / Domain / Data)

## 디렉터리
```
ui/screen/
  a0_splash  a1_login  a2_on_boarding
  a4_main/
    a4_3_daily_summary_detail  a4_4_daily_quiz_result
    a4_5_add_goal  a4_6_guide_expired_goal
  b1_summary  b2_quiz  b3_quiz_result
  c1_mypage  c2_goal_list  c3_edit_user_info
domain/  model/ usecase/ repository/
data/    repository/ db/ network/ datastore/ mapper/
di/  utils/firebase/
```
클래스 접미사: `ViewModel` `UseCase` `Repository` `RepositoryImpl` `Dao` `Entity` `Mapper`

## ❌ 금지 (위반 시 자동 수정)
- XML 레이아웃 신규 생성
- ViewModel 내 Context / Resources / Intent / View 참조
- LiveData · RxJava · GlobalScope · runBlocking (프로덕션)
- `_uiState` public 노출 → 반드시 `private` + `.asStateFlow()`
- Repository 직접 호출 → UseCase 경유 필수
- Dispatcher 하드코딩 → `@IoDispatcher` Qualifier 주입
- Room에서 `fallbackToDestructiveMigration()`
- `remember { ViewModel() }` → `hiltViewModel()` 사용
- Material2 import → Material3 사용
- 네트워크 예외 throw → `Result` / `sealed class` 래핑

## 명령어
```
빌드:     ./gradlew assembleDebug
테스트:   ./gradlew test
특정테스트: ./gradlew :app:testDebugUnitTest --tests "*.<ClassName>"
Lint:     ./gradlew lintDebug
의존성:   ./gradlew :app:dependencies
```

## 작업 규칙
1. 코드 작성 전 유사 구현 grep — 있으면 그 패턴 따름
2. 작성 후 `assembleDebug` 자동 실행 — 실패 시 분석·수정·재빌드 루프
3. 커밋 전 teum-code-reviewer 스킬로 컨벤션 검증

커밋: `feat(quiz): 설명` / `fix(goal): 설명` / `refactor` / `chore` / `test`

## 스킬 트리거
| 요청 유형 | 사용 스킬 |
|---|---|
| 새 화면 · 기능 추가 | `teum-feature-scaffolder` |
| 코드 리뷰 · 컨벤션 검증 | `teum-code-reviewer` |
| Room 스키마 변경 | `teum-room-migration` |
| ViewModel 테스트 작성 | `teum-viewmodel-test` |

## 민감 파일 (읽기·수정 금지)
`google-services.json` · `local.properties` · `*.keystore` · `*.jks`

## 응답 규칙
- 한국어, 기술 용어 원어 병기
- 불필요한 postamble 생략
- 코드 블록: import 포함, KDoc 포함, 파일 경로 명시
- 에러: 원인 → 영향 → 수정안