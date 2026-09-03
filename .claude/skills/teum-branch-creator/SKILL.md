---
name: teum-branch-creator
description: |
  틈틈잇(teumteumeat) Android 프로젝트 전용 브랜치 생성 자동화 스킬.
  사용자가 "브랜치 만들어줘", "새 브랜치 생성해줘", "기능 브랜치 파줘",
  "브랜치 따줘", "작업 브랜치 만들어줘" 라고 말하면 반드시 이 스킬을 사용한다.
  개발 의도와 기능 설명을 물어보고, 구현 방법을 제시한 뒤
  브랜치명 3개를 추천하고 원격에 push 한다.
---

# teum-branch-creator 스킬

틈틈잇 프로젝트에서 현재 브랜치를 베이스로 새 기능 브랜치를 생성하는 스킬.
개발 의도 파악 → 구현 방법 제시 → 브랜치명 추천 → 생성 및 원격 push 까지 일괄 처리한다.

---

## 브랜치 네이밍 컨벤션

```
<type>/<scope>-<short-description>
```

### type 목록
| type       | 사용 기준                              |
|------------|--------------------------------------|
| feat       | 새 기능·화면 추가                       |
| fix        | 버그 수정                              |
| refactor   | 로직 변경 없는 구조 개선                  |
| chore      | 빌드·설정·의존성 변경                    |
| test       | 테스트 코드 작성·수정                    |
| hotfix     | 프로덕션 긴급 수정                      |

### scope — 화면·도메인 기준
| 화면 / 영역                        | scope       |
|-----------------------------------|-------------|
| a0_splash                         | splash      |
| a1_login                          | login       |
| a2_on_boarding                    | onboarding  |
| a4_main                           | main        |
| b1_summary                        | summary     |
| b2_quiz                           | quiz        |
| b3_quiz_result                    | quiz-result |
| c1_mypage                         | mypage      |
| c2_goal_list / c3_edit_user_info  | goal        |
| domain / usecase                  | domain      |
| data / repository / db            | data        |
| utils/firebase                    | notification |
| build.gradle / libs.versions      | gradle      |
| 여러 레이어에 걸친 변경              | core        |

### short-description 규칙
- 소문자 + 하이픈(`-`) 구분
- 동사 또는 명사형 영어 단어 2~4개
- 예: `add-quiz-retry`, `fix-permission-crash`, `refactor-goal-viewmodel`

---

## 실행 절차

### STEP 1 — 현재 브랜치 상태 파악

```bash
# 현재 브랜치명 확인
git rev-parse --abbrev-ref HEAD

# 로컬 브랜치 목록
git branch --format='%(refname:short)'

# 원격 브랜치 목록
git branch -r --format='%(refname:short)' | sed 's|origin/||' | grep -v HEAD | sort
```

베이스 브랜치는 브랜치 type에 따라 고정된다 (CLAUDE.md `Git 전략` 참고).
이 저장소는 원격에 `develop` 브랜치가 없고 `develop_kbs`가 실질적인 통합 브랜치로 운영되므로,
CLAUDE.md의 `develop` 표기는 이 저장소에서는 `develop_kbs`로 해석한다.

| type | 베이스 브랜치 |
|---|---|
| `hotfix` | `main` |
| `feat` / `fix` / `refactor` / `chore` / `test` | `develop_kbs` |

정확한 베이스는 STEP 4에서 type이 정해진 뒤 STEP 4.5에서 검증·전환한다.

---

### STEP 2 — 개발 의도 및 기능 설명 수집

아래 질문을 한 번에 출력하고 사용자 답변을 기다린다.

```
브랜치를 생성하기 전에 몇 가지 여쭤볼게요.

1. 어떤 기능을 개발하려고 하시나요?
   (예: 퀴즈 오답 복습 화면 추가, FCM 토큰 갱신 버그 수정 등)

2. 이 기능을 개발하는 이유나 배경이 있나요?
   (예: 사용자 리텐션 개선, 크래시 신고 대응 등 — 없으면 생략 가능)
```

---

### STEP 3 — 구현 방법 제시

사용자가 답변하면 아래 정보를 수집한 뒤 구현 방법을 제안한다.

#### 3-1. 관련 코드 탐색

```bash
# 관련 화면·도메인 디렉터리 확인
find app/src/main/java -type d | sort

# 유사 구현 grep (기능 키워드로 검색)
grep -r "<기능 핵심 키워드>" app/src/main/java --include="*.kt" -l
```

#### 3-2. 구현 방법 출력 형식

```
## 구현 방법 제안

### 영향 레이어
[기능에 따라 변경이 필요한 레이어와 파일 목록을 나열]

Presentation (ui/screen/...)
  - XxxScreen.kt    : [역할]
  - XxxViewModel.kt : [역할]

Domain (domain/...)
  - XxxUseCase.kt        : [역할]
  - XxxRepository.kt     : [역할 - 인터페이스]

Data (data/...)
  - XxxRepositoryImpl.kt : [역할]
  - XxxDao.kt / XxxEntity.kt : [Room 변경 시]
  - XxxApiService.kt     : [네트워크 연동 시]

### 구현 흐름
[데이터·이벤트 흐름을 간단한 텍스트 다이어그램으로 표현]
예: Screen → ViewModel → UseCase → Repository → (DB / API)

### 주요 고려사항
- [프로젝트 컨벤션(CLAUDE.md)과 연관되는 주의점]
- [상태 관리 방법 — StateFlow / sealed class 설계]
- [Hilt 모듈 추가 여부]
- [Room 스키마 변경 필요 여부 → teum-room-migration 스킬 안내]
```

---

### STEP 4 — 브랜치명 3개 추천

위 정보를 바탕으로 컨벤션에 맞는 브랜치명 3가지를 추천한다.

출력 형식:
```
아래 3가지 브랜치명을 추천합니다. (베이스: <해당 type의 올바른 베이스 브랜치>)

  1. feat/<scope>-<description>
     → [한 줄 선택 이유]

  2. feat/<scope>-<description>
     → [한 줄 선택 이유]

  3. feat/<scope>-<description>
     → [한 줄 선택 이유]

번호를 선택하거나 직접 브랜치명을 입력해주세요.
```

추천 시 유의사항:
- 1번은 가장 표준적이고 명확한 이름
- 2번은 scope를 더 구체화한 이름
- 3번은 짧고 간결한 이름
- 이미 존재하는 브랜치명과 중복되지 않도록 확인

```bash
# 기존 브랜치명 중복 확인
git branch -a | grep "<예정 브랜치명>"
```

---

### STEP 4.5 — 베이스 브랜치 검증 및 전환

선택된 브랜치명의 type으로 올바른 베이스를 결정한다 (`hotfix` → `main`, 그 외 → `develop_kbs`).

**Case A — 현재 브랜치가 이미 올바른 베이스(`develop_kbs` 또는 `hotfix`의 경우 `main`)와 같은 경우**
전환 없이 바로 아래처럼 작업 승인만 요청하고 STEP 5로 진행한다.

```
현재 브랜치(`<현재 브랜치명>`)가 이미 올바른 베이스입니다.
이 브랜치를 기준으로 `<선택된 브랜치명>` 을 생성할까요?

[y] 진행   |   [n] 취소
```

**Case B — 현재 브랜치가 올바른 베이스와 다른 경우**
자동으로 `develop_kbs`(또는 `main`)로 전환하지 않고, 아래처럼 베이스 후보 2가지를 제시하여 사용자가 선택하게 한다.

```
현재 브랜치: `<현재 브랜치명>`

어떤 브랜치를 기준으로 `<선택된 브랜치명>` 을 생성할까요?

  1. 현재 브랜치 (`<현재 브랜치명>`) 기준으로 생성
  2. `<올바른 베이스>` 기준으로 생성 (전환 후 최신화)

번호를 선택해주세요.
```

1번 선택 시: 전환 없이 현재 브랜치를 베이스로 STEP 5 진행.
2번 선택 시:
```bash
git checkout <올바른 베이스>
git pull origin <올바른 베이스>
```
이후 STEP 5 진행.

`n` 또는 거부 응답 시 브랜치 생성을 중단한다.

---

### STEP 5 — 브랜치 생성 및 원격 Push

베이스 브랜치가 확정된 상태에서 아래를 실행한다.

```bash
# 베이스 브랜치 기준 새 브랜치 생성
git checkout -b <선택된 브랜치명>

# 원격에 push (upstream 설정 포함)
git push -u origin <선택된 브랜치명>
```

실행 후 결과를 아래 형식으로 출력한다:

```
브랜치 생성 완료!

  베이스 브랜치 : <올바른 베이스 브랜치>
  새 브랜치     : <새 브랜치명>
  원격 push     : origin/<새 브랜치명>

이제 이 브랜치에서 작업을 시작할 수 있습니다.
개발이 완료되면 `teum-code-reviewer` -> `teum-commit` → `teum-pr-creator` 스킬을 순서대로 사용하세요.
```

---

## 안전 규칙

- `hotfix`가 아닌 type이 `main`을 베이스로 하려는 경우 → STEP 4.5에서 `develop_kbs` 사용 안내(Case B)
- `hotfix` type이 `develop_kbs`를 베이스로 하려는 경우 → STEP 4.5에서 `main` 사용 안내(Case B)
- 브랜치명에 대문자, 공백, 특수문자(`@`, `#`, `*`, `~`, `^`, `:`, `?`) 포함 시 → 자동 소문자·하이픈 변환 후 안내
- 현재 브랜치에 미커밋 변경사항이 있을 경우:
  ```bash
  git status --short
  ```
  변경사항이 있으면 사용자에게 알리고 `stash` 또는 커밋 여부를 확인한다.
  (`teum-commit` 스킬 사용 안내)
- 원격 push 실패 시 오류 메시지를 분석하여 원인(인증, 권한, 네트워크)을 사용자에게 안내

---

## 작업 완료 후 안내

브랜치 생성이 완료된 뒤, 기능 복잡도에 따라 아래 스킬 사용을 제안한다.

| 상황 | 권장 스킬 |
|---|---|
| 새 화면(Activity/Screen)이 필요한 경우 | `teum-feature-scaffolder` |
| Room 테이블 변경이 필요한 경우 | `teum-room-migration` |
| 개발 완료 후 커밋 | `teum-commit` |
| 개발 완료 후 PR 생성 | `teum-pr-creator` |
