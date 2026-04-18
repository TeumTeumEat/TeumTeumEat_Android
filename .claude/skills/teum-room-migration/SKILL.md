---
name: teum-room-migration
description: 틈틈잇의 Room 데이터베이스 스키마 변경 시 Migration 코드와 마이그레이션 테스트를 안전하게 생성하는 스킬. 오프라인 우선 앱에서 사용자 데이터 손실 없이 스키마를 업그레이드하는 것이 핵심이다. 사용자가 "Room 스키마 변경", "테이블에 컬럼 추가", "@Entity 수정", "database version 올려야 해", "마이그레이션 코드 작성" 같은 요청을 할 때 반드시 사용할 것. 실사용자 데이터 유실 리스크를 줄이기 위해 항상 이 스킬을 경유한다.
---

# 틈틈잇 Room Migration

Room 스키마 변경을 안전하게 처리한다.

## 변경 유형 판별

사용자 요청을 다음 4가지로 분류한다:

1. **컬럼 추가 (Additive)** — 가장 안전. defaultValue 필수.
2. **컬럼 삭제/이름 변경** — AutoMigration + `@RenameColumn`/`@DeleteColumn` 권장
3. **테이블 추가** — `CREATE TABLE` SQL + Entity 등록
4. **타입 변경/복잡한 변환** — 수동 Migration 필수 + 데이터 변환 로직

## 생성할 산출물

### 1. @Database 업데이트
- `version`을 +1
- `autoMigrations` 배열에 새 버전 추가 (AutoMigration 가능 시)
- `exportSchema = true` 확인 (schema JSON 저장 필수)

### 2. Migration 객체
- AutoMigration 가능: `@AutoMigration(from = N, to = N+1, spec = ...)`
- 수동 Migration 필요: `object Migration_N_to_Np1 : Migration(N, N+1)` + `override fun migrate(db: SupportSQLiteDatabase)`

### 3. Migration 테스트 (`androidTest/`)
- `MigrationTestHelper` 사용
- 이전 버전 DB 생성 → 마이그레이션 실행 → 데이터 보존 검증

### 4. 리스크 분석 보고
- 기존 사용자 데이터 유실 여부
- 롤백 가능 여부
- 앱 버전 호환성 (최소 지원 버전 확인)

## 작업 흐름

1. 현재 `@Database` 클래스와 기존 Entity 확인
2. 변경 유형 판별 (위 4가지 중)
3. `schemas/` 폴더 내 기존 스키마 JSON 확인 (`exportSchema = true` 필수)
4. 변경 유형에 맞는 Migration 코드 생성
5. Migration 테스트 코드 작성
6. `build.gradle.kts`의 `androidTestImplementation` 의존성 점검:
    - `androidx.room:room-testing`
7. 리스크 분석 보고 작성

## 주의사항

- `fallbackToDestructiveMigration()`을 프로덕션 코드에 사용 금지 (데이터 전체 삭제됨)
- DefaultValue 없는 NOT NULL 컬럼 추가 시 Migration 실패 → 반드시 default 지정
- 복잡한 데이터 변환 시 임시 테이블 기법 권장 (CREATE temp → COPY → DROP → RENAME)

## 출력 형식

```
[변경 유형] Additive / Schema Change / Data Transformation
[권장 방식] AutoMigration / Manual Migration
[리스크] 낮음 / 중간 / 높음
[생성 파일]
1. AppDatabase.kt (수정)
2. Migration_N_to_Np1.kt (신규)
3. MigrationTest.kt (신규)
[데이터 유실 가능성] 없음 / 있음 (상세)
```