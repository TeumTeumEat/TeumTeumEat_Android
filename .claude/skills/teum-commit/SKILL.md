---
name: teumteum-commit
description: |
  틈틈잇(teumteumeat) Android 프로젝트 전용 git 커밋 자동화 스킬.
  사용자가 "커밋해줘", "이 변경사항 커밋해줘", "[작업 내용] 커밋해줘" 라고 말하면
  반드시 이 스킬을 사용한다. git status/diff를 분석하여 프로젝트 컨벤션에 맞는
  커밋 메시지를 자동 생성하고, 사용자 승인 후 커밋을 실행한다.
  커밋 메시지 작성이나 git 커밋 관련 요청이 있을 때도 항상 이 스킬을 사용한다.
---

# teumteum-commit 스킬

틈틈잇 프로젝트 커밋 컨벤션에 맞는 메시지를 자동 생성하고 커밋을 실행하는 스킬.

---

## 커밋 컨벤션 규칙

### 메시지 구조
```
<type>(<scope>): <subject>

[body - 조건부]

[footer - 조건부]
```

### type 목록 (6종)
| type     | 사용 기준                              |
|----------|--------------------------------------|
| feat     | 새 파일·기능 추가                        |
| fix      | 버그 수정, 예외 처리 보완                  |
| refactor | 로직 변경 없이 구조·네이밍 개선             |
| chore    | build.gradle, libs.versions.toml, 설정 |
| test     | *Test.kt, *Spec.kt 파일 변경            |
| hotfix   | 프로덕션 긴급 수정 (`hotfix/*` 브랜치의 커밋) |

### scope — 파일 경로 기준 매핑
| 변경 파일 경로 키워드                    | scope       |
|--------------------------------------|-------------|
| ui/screen/a0_splash                  | splash      |
| ui/screen/a1_login                   | login       |
| ui/screen/a2_on_boarding             | onboarding  |
| ui/screen/a4_main                    | main        |
| ui/screen/b1_summary                 | summary     |
| ui/screen/b2_quiz                    | quiz        |
| ui/screen/b3_quiz_result             | quiz-result |
| ui/screen/c1_mypage                  | mypage      |
| ui/screen/c2_goal_list, c3_edit_user_info | goal   |
| domain/, usecase/                    | domain      |
| data/, repository/, db/              | data        |
| utils/firebase                       | notification |
| build.gradle.kts / libs.versions.toml | gradle     |
| 여러 레이어·경로에 걸친 공통 변경          | core        |

### subject 규칙
- 영어 type + 한국어 subject 혼용
- 명사형 종결 (동사형 금지: "수정했음" → "수정")
- 50자 이내, 마침표 금지

### body 작성 조건 (아래 중 하나 해당 시만 작성)
- 변경 이유가 파일명·diff만으로 불명확할 때
- 다른 접근법을 검토했다가 기각한 경우
- 비직관적인 구현 선택이 있을 때

### footer 작성 조건
- 이슈 연동 시: `Closes #이슈번호`
- Breaking Change 시: `BREAKING CHANGE: <설명>`

---

## 실행 절차

### STEP 0 — 보호 브랜치 게이트 (현재 브랜치가 `main` / `develop` / `release`일 때만)

```bash
git rev-parse --abbrev-ref HEAD
```

`main`(배포) · `release`(QA 검증) · `develop`(통합·빌드) 세 브랜치는
**실제 소스 수정·개발을 직접 진행하지 않는 것이 원칙**이다. 모든 변경은 `feature`/`hotfix`
브랜치를 거쳐 병합으로만 반영한다. 현재 브랜치가 이 셋에 해당하지 않으면 이 단계 전체를
건너뛰고 STEP 1로 이동한다.

불가피하게 위 세 브랜치 중 하나에 직접 커밋해야 한다면 아래 순서로 확인하고,
하나라도 통과하지 못하면 커밋을 중단하고 `teum-branch-creator`로 기능 브랜치를 만들어 작업하도록 안내한다.

**1) 진행 여부 확인**

```
현재 브랜치가 <main|develop|release> 입니다. 이 브랜치는 직접 소스 수정·개발을
진행하지 않는 것이 원칙이며, 모든 변경은 feature/hotfix 브랜치의 병합으로만 반영됩니다.

정말 <브랜치명>에 바로 커밋하시겠습니까?

[y] 진행   |   [n] 취소
```

`n` 또는 거부 응답 시 커밋을 중단하고 기능 브랜치로 전환할 것을 안내한다.

**2) 작업 이유 확인**

```
<브랜치명>에 직접 커밋해야 하는 이유를 알려주세요.
```

**3) 타당성 검증**

아래에 해당하는 경우에만 타당한 것으로 판단한다.

- 프로젝트 담당자가 배포 관리 목적(예: 버전·태그 관련 메타데이터)으로 진행하는 경우
- 그 외 기능 개발·버그 수정 성격의 변경은 타당하지 않음 → 기능 브랜치로 전환 안내 후 중단

**타당하지 않다고 판단되면** 판단 근거를 설명하고 커밋을 중단한다.
**타당하다고 판단되면** 판단 근거를 사용자에게 알리고 STEP 1로 진행한다.

### STEP 1 — 변경 파일 탐색
```bash
git status
git diff --stat
```
변경된 파일 목록과 수정 라인 수를 파악한다.

### STEP 2 — 컨벤션 메시지 초안 생성
위 scope 매핑 테이블을 기준으로:
1. 변경 파일 경로에서 scope 결정
2. 변경 내용(추가/수정/삭제)에서 type 결정
3. 변경의 핵심을 50자 이내 한국어 명사형으로 subject 작성
4. body 필요 여부 판단 후 작성

### STEP 3 — 사용자 승인 요청
생성된 메시지를 아래 형식으로 출력하고 승인 대기:
```
아래 커밋 메시지로 진행할까요?
---
<생성된 커밋 메시지 전문>
---
[y] 확정  |  수정 내용을 입력하면 메시지를 수정한 후 재확인합니다.
```

### STEP 4 — 커밋 실행 (승인 후)
body가 없는 경우:
```bash
git add -A
git commit -m "<type>(<scope>): <subject>"
```

body가 있는 경우:
```bash
git add -A
git commit -m "<type>(<scope>): <subject>" -m "<body>"
```

footer가 있는 경우 `-m "<footer>"` 추가.

### STEP 5 — 결과 확인
```bash
git log --oneline -1
```
커밋 해시와 메시지를 출력하여 완료를 알린다.

---

## 안전 규칙 (커밋 전 반드시 확인)
- `local.properties`, `*.jks`, `*.keystore` 파일 → 커밋 금지, 사용자에게 경고
- `build/`, `.gradle/` 디렉토리 → 커밋 금지
- `git status`에서 untracked 파일 발견 시 → 사용자에게 포함 여부 확인
- 커밋 전 `./gradlew test` 통과 여부 → 사용자에게 확인 후 진행
- 현재 브랜치가 `main`/`develop`/`release`인 경우 → STEP 0 게이트(진행 여부·사유 확인 → 타당성 검증) 통과 전까지 커밋 금지

---

## 올바른 예시

```
fix(permission): 알림 권한 취소 후 ViewModel 상태 초기화 버그 수정

시스템 설정에서 POST_NOTIFICATIONS 권한 취소 후 복귀 시
Process Death로 StateFlow 데이터가 소실되는 문제를 수정.
SavedStateHandle을 적용하여 핵심 UI 상태 복원 처리.

Closes #12
```

```
feat(quiz): 퀴즈 결과 화면 오답 복습 기능 추가
```

```
chore(gradle): firebase-bom 버전 34.7.0 업데이트
```

## 잘못된 예시 (생성 금지)
```
fix: bug fixed.                          ← scope 없음, 영어, 마침표
Fix(Permission): 권한 버그 수정했음        ← 대문자 type, 동사형 종결
feat(quiz): Add quiz result screen       ← 한국어 subject 규칙 위반
```
