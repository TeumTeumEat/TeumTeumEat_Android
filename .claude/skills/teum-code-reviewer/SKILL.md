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