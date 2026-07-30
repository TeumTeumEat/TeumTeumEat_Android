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

## Git 전략

### 브랜치 전략이 필요한 이유
- Issue 단위로 작업을 분리하기 위해
- 각 단계에서 해야 하는 작업의 목표를 명시적으로 분리하기 위해

### 브랜치 운영 원칙
[아틀라시안 BitBucket 팀의 조언](https://www.atlassian.com/git/articles/trust-the-merge-and-branch-simplification-musings) 기반
(원글은 대부분 삭제되었으며, 현재 [화해 팀 블로그 글](https://blog.hwahae.co.kr/all/tech/9507)에서 확인 가능)
- 코드 병합을 믿어라
- 극단적인 브랜치 전략을 피해라
- 가능하다면 브랜치 계층 구조를 단순화하라

### 브랜치 네이밍
`<type>/<scope>-<short-description>` — type·scope 상세는 `teum-branch-creator` 스킬 참고

### 커밋 메시지
`<type>(<scope>): <subject>` — type 6종(`feat` `fix` `refactor` `chore` `test` `hotfix`), scope 매핑은 `teum-commit` 스킬 참고

### 브랜치 계층 구조

| 브랜치 | 역할 | 베이스 | 병합 대상 | 태그 |
|---|---|---|---|---|
| `main` | 🚀 실서버(Prod) 배포 브랜치. 언제나 배포 가능한 상태로 유지 | — | — | 소스 변경 시 프로젝트 담당자가 생성: `git tag -a <TAG NAME> -m "<TAG MESSAGE>"` |
| `release` | ✅ QA 검증 브랜치 | `develop` | `main` | QA 배포 시점에 프로젝트 담당자가 생성 |
| `develop` | ✅ 배포·빌드 대상 통합 브랜치. `feature`에서 개발 완료된 기능이 병합됨 | — | `release` | — |
| `feature/*`, `fix/*` 등 | 기능 개발·버그 수정 브랜치 | `develop` | `develop` | — |
| `hotfix/*` | 프로덕션 긴급 수정 브랜치 | `main` | `main` (+ `develop`/`release` 백포트) | `main` 병합 시 프로젝트 담당자가 생성 |

- ❗ `main` / `develop` / `release` 에서는 직접 소스 수정·개발을 진행하지 않는다 — 모든 변경은 `feature`/`hotfix` 브랜치를 거쳐 병합으로만 반영한다
- 어떤 상황에서든 `main`에 바로 풀리퀘스트·머지를 진행하지 않는다 — 불가피한 경우 `teum-pr-creator` STEP 1.4 게이트(진행 여부·사유 확인 → 타당성 검증)를 통과해야 한다
- `main` / `develop` / `release`에 대한 직접 커밋은 `teum-commit` STEP 0 게이트를 통과해야 한다
- 프로젝트가 성숙 단계에 접어들면 `release` 계층을 생략하고 `develop` + `main` 2단계로 단순화할 수 있다 (브랜치 계층 단순화 원칙)

## 스킬 트리거
| 요청 유형 | 사용 스킬 |
|---|---|
| 브랜치 생성 · 기능 브랜치 분기 | `teum-branch-creator` |
| 새 화면 · 기능 추가 | `teum-feature-scaffolder` |
| 코드 리뷰 · 컨벤션 검증 | `teum-code-reviewer` |
| Room 스키마 변경 | `teum-room-migration` |
| ViewModel 테스트 작성 | `teum-viewmodel-test` |
| Pull Request 생성 | `teum-pr-creator` |
| main PR 병합 후 스토어용 3줄 릴리즈 노트 요약 | `teum-store-release-note` |

## 민감 파일 (읽기·수정 금지)
`google-services.json` · `local.properties` · `*.keystore` · `*.jks`

## 응답 규칙
- 한국어, 기술 용어 원어 병기
- 불필요한 postamble 생략
- 코드 블록: import 포함, KDoc 포함, 파일 경로 명시
- 에러: 원인 → 영향 → 수정안