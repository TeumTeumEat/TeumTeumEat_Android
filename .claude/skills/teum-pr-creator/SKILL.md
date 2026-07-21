---
name: teum-pr-creator
description: |
  틈틈잇(teumteumeat) Android 프로젝트 전용 Pull Request 자동 생성 스킬.
  사용자가 "PR 올려줘", "풀리퀘스트 작성해줘", "PR 만들어줘", "PR 내용 작성해줘" 라고
  말하면 반드시 이 스킬을 사용한다.
  현재 브랜치의 커밋 내역과 변경 파일을 분석하여 정해진 목차 구조의 PR을 생성하고,
  사용자 승인 후 GitHub에 PR을 올린다.
---

# teum-pr-creator 스킬

틈틈잇 프로젝트의 변경 내역을 분석하여 정해진 목차 구조의 GitHub Pull Request를 생성하는 스킬.

---

## PR 목차 구조 (고정)

아래 7~10개 섹션을 순서대로 구성한다.
스크린샷·연관 PR처럼 조건부 섹션은 해당 내용이 없으면 섹션 자체를 생략한다.

```
1. 이 PR이 무엇인가요?          (필수)
2. 기능 구현 배경               (필수)
3. 사고 과정 및 구현 방법        (필수)
4. 주요 변경 내용               (필수)
5. 핵심 코드                    (필수)
6. 기능 설계 원칙               (필수)
7. 스크린샷                     (조건부 — 변경이 UI에 영향을 줄 때만)
8. 리뷰어에게                   (필수)
9. 체크리스트                   (필수)
10. 연관 PR                    (조건부 — 관련 PR이 있을 때만)
```

---

## 실행 절차

### STEP 1 — 베이스 브랜치 확인

먼저 아래 명령으로 원격 브랜치 목록을 파악한 뒤, 사용자에게 PR 대상 브랜치를 확인한다.

```bash
# 현재 브랜치명
git rev-parse --abbrev-ref HEAD

# 원격 브랜치 목록 (선택지 제공용)
git branch -r --format='%(refname:short)' | sed 's|origin/||' | grep -v HEAD | sort
```

확인 질문 형식:
```
PR을 어느 브랜치로 올릴까요?

현재 브랜치: [현재 브랜치명]

원격 브랜치 목록:
  1. main
  2. develop
  3. [기타 브랜치]
  ...

번호를 입력하거나 브랜치명을 직접 입력해주세요. (기본값: main)
```

사용자가 브랜치를 선택하면 이후 모든 단계에서 해당 브랜치를 베이스 브랜치(`BASE_BRANCH`)로 사용한다.
`main` 브랜치를 현재 작업 브랜치로 직접 PR하려는 경우 경고 후 재확인한다.

이후 선택된 `BASE_BRANCH`를 기준으로 커밋·diff를 수집한다.

```bash
# BASE_BRANCH 대비 커밋 목록
git log {BASE_BRANCH}..HEAD --oneline

# 변경 파일 목록 + 라인 수
git diff {BASE_BRANCH}...HEAD --stat

# 전체 diff (핵심 코드 발굴용)
git diff {BASE_BRANCH}...HEAD --unified=3
```

### STEP 2 — 사용자 제공 컨텍스트 확인

PR 생성 요청 시 사용자가 아래 내용을 제공했는지 확인한다.
제공된 경우 해당 내용을 섹션에 반영한다.

| 컨텍스트 항목         | 반영 섹션              |
|-------------------|----------------------|
| 기능 개발 배경·목적  | 2. 기능 구현 배경       |
| 구현 시 고민한 점    | 3. 사고 과정 및 구현 방법|
| 스크린샷 파일 경로   | 7. 스크린샷             |
| 관련 이슈 번호      | 10. 연관 PR / footer   |

### STEP 3 — PR 본문 초안 생성

아래 각 섹션 작성 규칙에 따라 초안을 생성한다.

---

## 섹션별 작성 규칙

### 1. 이 PR이 무엇인가요?

- 2~4문장으로 PR의 핵심 목적을 요약한다.
- "무엇을 왜 추가/수정/개선했는가"를 한 문단으로 설명한다.
- 기술 용어는 원어 병기 (예: Firebase Analytics, User Property).

```markdown
## 이 PR이 무엇인가요?

[2~4문장 요약]
```

---

### 2. 기능 구현 배경

- 사용자가 제공한 배경 설명을 바탕으로 작성한다.
- 없으면 커밋 메시지와 변경 파일에서 추론한다.
- "왜 지금 이 기능이 필요한가"에 답한다.

```markdown
## 기능 구현 배경

[배경 설명 — 2~5문장]
```

---

### 3. 사고 과정 및 구현 방법

- 구현을 결정하기까지의 고민, 대안, 선택 이유를 서술한다.
- 구체적인 구현 흐름(데이터 흐름, 레이어 간 의존 방향)을 설명한다.
- 그림이 필요한 경우 텍스트 다이어그램(ASCII, Mermaid)으로 표현한다.

```markdown
## 사고 과정 및 구현 방법

### 문제 정의
[해결하려는 문제]

### 접근 방법
[선택한 방법과 이유, 기각한 대안]

### 구현 흐름
[데이터·이벤트 흐름 설명 또는 다이어그램]
```

---

### 4. 주요 변경 내용

- 변경된 파일을 **레이어별**로 분류하여 표 또는 목록으로 정리한다.
- 각 항목은 "무엇이 어떻게 바뀌었는가"를 한 줄로 요약한다.

```markdown
## 주요 변경 내용

### [레이어명 또는 도메인명]
| 파일 | 변경 내용 |
|---|---|
| `파일명.kt` | 설명 |
```

---

### 5. 핵심 코드

- diff에서 이 PR의 핵심 의도를 가장 잘 드러내는 코드 1~3개를 선택한다.
- 각 코드 블록 앞에 **왜 이렇게 구현했는지** 1~2문장으로 설명한다.
- 50줄 초과 스니펫은 핵심 부분만 발췌한다.

```markdown
## 핵심 코드

### [코드 제목]
[이 코드의 의도·선택 이유 설명]

```kotlin
// 코드 스니펫
```
```

---

### 6. 기능 설계 원칙

- 이 PR 전반에 걸쳐 지킨 설계 규칙을 3~5개 bullet로 정리한다.
- 프로젝트 컨벤션(CLAUDE.md)과 연관되는 경우 언급한다.

```markdown
## 기능 설계 원칙

- **[원칙명]**: [설명]
- ...
```

---

### 7. 스크린샷 (조건부)

**작성 조건**: UI 화면 또는 사용자가 직접 보는 동작에 변화가 있을 때만 작성.
순수 비즈니스 로직·Analytics·백엔드 연동만 변경된 경우 섹션 생략.

표 형태로 Before / After 또는 화면 목록을 삽입할 자리를 준비한다.
실제 이미지는 사용자가 직접 삽입한다.

```markdown
## 스크린샷

| 화면 | Before | After |
|---|---|---|
| [화면 이름] | [이미지 삽입] | [이미지 삽입] |
```

또는 단순 추가 화면인 경우:

```markdown
## 스크린샷

| 화면 | 스크린샷 |
|---|---|
| [화면 이름] | [이미지 삽입] |
```

---

### 8. 리뷰어에게

- 리뷰어가 변경 내용을 직접 확인할 수 있는 **재현 방법**을 단계별로 작성한다.
- 테스트 환경 조건(디버그 모드, 특정 계정, 기기 등)이 있으면 명시한다.
- 리뷰 시 집중해서 봐 주길 바라는 포인트나 우려 사항을 솔직하게 기술한다.

```markdown
## 리뷰어에게

### 확인 방법
1. [단계별 재현 순서]

### 중점 리뷰 포인트
- [리뷰어에게 특히 확인을 요청할 부분]
```

---

### 9. 체크리스트

아래 항목을 기본으로 제공하고, PR 내용에 맞게 불필요한 항목은 제거·추가한다.

```markdown
## 체크리스트

- [ ] `./gradlew assembleDebug` 빌드 성공 확인
- [ ] 새로운 XML 레이아웃 파일을 생성하지 않았다
- [ ] ViewModel에 Android 프레임워크(Context, View 등)를 직접 참조하지 않았다
- [ ] `MutableStateFlow`가 `private`으로 선언되고 `.asStateFlow()`로 노출된다
- [ ] 네트워크 예외를 `Result` / `sealed class`로 래핑했다
- [ ] 민감 정보(`google-services.json`, `*.keystore`)가 커밋에 포함되지 않았다
- [ ] 셀프 코드 리뷰(`/teum-code-reviewer`)를 통과했다
```

---

### 10. 연관 PR (조건부)

**작성 조건**: 선행 PR이 있거나 후속 작업이 예정된 경우에만 작성.

```markdown
## 연관 PR

- 선행: #[번호] — [제목]
- 후속: #[번호] — [제목]
```

---

## STEP 4 — 클립보드 복사 및 사용자 승인 요청

초안이 완성되면 먼저 PR 본문 전체를 클립보드에 복사한다. 본문을 터미널에 다시 출력하지 않는다.

```bash
cat <<'EOF' | pbcopy
[PR 본문 전체]
EOF
```

복사 후 아래 형식으로만 출력하고 승인 대기한다.

```
아래 내용으로 PR을 올릴까요?

제목: [PR 제목]
브랜치: [현재 브랜치] → [BASE_BRANCH]

본문 전체가 클립보드에 복사되었습니다.

[y] 확정   |   수정할 내용을 입력하면 반영 후 재확인합니다.
```

수정 요청이 들어오면 본문을 다시 생성하고, 클립보드 복사(pbcopy)부터 다시 수행한 뒤 동일한 형식으로 재확인한다.

---

## STEP 5 — PR 생성 페이지 오픈

승인 후 아래 순서로 실행한다. 본문은 `--body` 없이 두어, STEP 4에서 클립보드에 복사된 내용을 사용자가 직접 붙여넣도록 한다.

```bash
# 1. 브랜치 푸시 (최신 상태 반영)
git push -u origin [현재 브랜치명]

# 2. 제목만 채운 채로 GitHub PR 생성 페이지를 브라우저에서 연다
gh pr create --title "[PR 제목]" --base [BASE_BRANCH] --web
```

브라우저가 열리면 본문 입력란에 클립보드 내용을 붙여넣도록(Cmd+V) 사용자에게 안내한다.

### PR 제목 규칙
- 커밋 컨벤션 type(scope) 형식 그대로: `feat(analytics): Firebase Analytics 이벤트 로깅 추가`
- 50자 이내, 마침표 금지

---

## STEP 6 — 결과 확인

사용자가 브라우저에서 본문을 붙여넣고 PR을 생성하면, 아래 명령으로 생성된 PR URL을 확인한다.

```bash
gh pr view --json url --jq '.url'
```

PR URL을 사용자에게 출력한다.

---

## 안전 규칙

- `local.properties`, `*.jks`, `*.keystore` 변경 감지 시 → PR 생성 중단, 즉시 경고
- `main` 브랜치 직접 PR 시도 시 → 경고 후 사용자 재확인
- 변경 파일이 없는(clean) 브랜치 → "커밋할 내용이 없습니다" 안내

---

## 작성 예시

### feat(analytics) PR 예시

```markdown
## 이 PR이 무엇인가요?

Firebase Analytics 이벤트 로깅 인프라를 구축하고, 로그인 및 온보딩 전 구간에 걸쳐
이벤트와 User Property를 추가합니다. ViewModel이 FirebaseAnalytics를 직접 참조하지
않도록 `TeumAnalyticsLogger` 래퍼 클래스를 도입하였습니다.

## 기능 구현 배경

유저의 앱 이탈 지점과 주요 기능 이용 패턴을 파악하여 이후 기획 방향을 데이터 기반으로
수립하기 위해 Analytics 이벤트 로깅을 추가하기로 하였습니다.

## 사고 과정 및 구현 방법

### 문제 정의
각 화면에서 FirebaseAnalytics를 직접 호출하면 이벤트 이름 오타, 파라미터 불일치,
단위 테스트 불가 등의 문제가 발생합니다.

### 접근 방법
- **채택**: `TeumAnalyticsLogger` 싱글턴 래퍼 + `TeumAnalyticsEvent` 상수 파일 분리
- **기각**: ViewModel 내 직접 호출 → Android 프레임워크 의존성 증가, 테스트 불가

### 구현 흐름
ViewModel → TeumAnalyticsLogger → FirebaseAnalytics SDK → GA4 서버

## 주요 변경 내용

### Data / Firebase utils
| 파일 | 변경 내용 |
|---|---|
| `TeumAnalyticsEvent.kt` | 이벤트 이름·파라미터 키 상수 정의 |
| `TeumAnalyticsLogger.kt` | Firebase Analytics 전용 래퍼, Hilt @Singleton |

### Presentation / OnBoarding
| 파일 | 변경 내용 |
|---|---|
| `OnBoardingViewModel.kt` | 온보딩 각 단계 이벤트 로깅 호출 추가 |

## 핵심 코드

### 중복 발송 방지 — app_install_or_update
최초 설치 또는 업데이트 직후 1회만 이벤트를 발송하기 위해 SharedPreferences에
마지막 발송 versionCode를 저장하고 비교합니다.

```kotlin
val lastSentVersionCode = prefs.getLong(KEY_LAST_SENT_VERSION_CODE, -1L)
if (currentVersionCode == lastSentVersionCode) return
analytics.logEvent(AppInstallOrUpdate.NAME, params)
prefs.edit { putLong(KEY_LAST_SENT_VERSION_CODE, currentVersionCode) }
```

## 기능 설계 원칙

- **ViewModel 격리**: ViewModel이 `FirebaseAnalytics`를 직접 참조하지 않고 `TeumAnalyticsLogger`만 의존
- **중앙 상수 관리**: 이벤트 이름·파라미터 키를 `TeumAnalyticsEvent`에 집중하여 오타 컴파일 타임 방지
- **중복 발송 방지**: versionCode 비교, SavedStateHandle 키 존재 여부로 중복 이벤트 차단

## 리뷰어에게

### 확인 방법
1. 디버그 빌드 설치 후 GA4 DebugView 활성화 (`adb shell setprop debug.firebase.analytics.app [패키지명]`)
2. 온보딩 전 단계 진행하며 이벤트 순서 확인
3. 앱 강제 종료 후 재진입 시 `onboarding_start` 중복 미발송 확인

### 중점 리뷰 포인트
- `logOnboardingComplete()` 호출 시점이 Success 상태 전환 직전인지 확인 부탁드립니다.

## 체크리스트

- [x] `./gradlew assembleDebug` 빌드 성공
- [x] ViewModel에 Android 프레임워크 직접 참조 없음
- [x] 민감 파일 커밋 없음
- [x] `/teum-code-reviewer` 통과
```
