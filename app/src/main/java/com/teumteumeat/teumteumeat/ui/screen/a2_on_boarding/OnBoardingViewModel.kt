package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_response.PresignedResponse
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.CreateGoalUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.GetCategoriesUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.IssuePresignedUrlUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.UpdateCommuteTimeUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.RegisterUserNameUseCase
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.Difficulty
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    val updateCommuteTimeUseCase: UpdateCommuteTimeUseCase,
    val registerUserNameUseCase: RegisterUserNameUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val issuePresignedUrlUseCase: IssuePresignedUrlUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
) : ViewModel() {

    // 이름 입력 제약조건 부분
    companion object {
        private const val MIN_LENGTH = 1
        private const val MAX_LENGTH = 10
        private val ALLOWED_REGEX = Regex("^[가-힣a-zA-Z0-9]*$")
    }

    // Flow 값으로 currentPage 읽기
    private val currentPage get() = uiState.value.currentPage
    private val totalPage get() = uiState.value.totalPage

    private val _uiState = MutableStateFlow<UiStateOnBoardingMain>(UiStateOnBoardingMain())
    val uiState = _uiState.asStateFlow()

    fun onCreateGoalClick() {
        viewModelScope.launch {
            val request = _uiState.value.run {
                CreateGoalRequest(
                    type = goalType,
                    endDate = endDate,
                    difficulty = difficulty,
                    prompt = promptInput.takeIf { it.isNotBlank() },
                    categoryId = if (goalType == GoalType.CATEGORY) {
                        selectedCategoryId
                    } else {
                        null                 // DOCUMENT → categoryId 미포함
                    }
                )
            }

            _uiState.update {
                it.copy(
                    isLoading = true,
                    pageErrorMessage = null
                )
            }

            when (val result = createGoalUseCase(request)) {

                is ApiResultV2.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                }

                is ApiResultV2.ServerError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pageErrorMessage = result.uiMessage
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pageErrorMessage = result.uiMessage
                        )
                    }
                }
            }
        }
    }

    fun onStudyWeekSelected(week: Int) {
        // 🔹 1. 오늘 날짜 (기준 날짜)
        val today = LocalDate.now()

        // 🔹 2. 선택한 주(week) 만큼 더해서 종료 날짜 계산
        val endDate = today.plusWeeks(week.toLong())

        // 🔹 3. 서버/기획 요구사항에 맞는 포맷 ("yyyy-MM-dd")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedEndDate = endDate.format(formatter)

        // 🔹 4. UI 상태 업데이트
        _uiState.update {
            it.copy(
                selectedStudyWeek = week,
                endDate = formattedEndDate,
            )
        }
    }

    private val PROMPT_MIN_LENGTH = 0
    private val PROMPT_MAX_LENGTH = 30

    fun onPromptInputChanged(input: String) {
        viewModelScope.launch {

            // ✅ 입력은 최대 길이까지만 저장
            val trimmedToMax =
                if (input.length > PROMPT_MAX_LENGTH)
                    input.take(PROMPT_MAX_LENGTH)
                else
                    input

            // ✅ 유효성 판단 (입력은 허용, 상태만 invalid)
            val violation = when {
                trimmedToMax.isBlank() ->
                    PromptViolation.Empty

                trimmedToMax.length < PROMPT_MIN_LENGTH ->
                    PromptViolation.TooShort

                trimmedToMax.length > PROMPT_MAX_LENGTH ->
                    PromptViolation.TooLong

                else ->
                    PromptViolation.None
            }

            val isValid = violation == PromptViolation.None

            val errorMessage = when (violation) {
                PromptViolation.None -> ""
                PromptViolation.Empty -> "프롬프트를 입력해주세요"
                PromptViolation.TooShort -> "최소 ${PROMPT_MIN_LENGTH}자 이상 입력해주세요"
                PromptViolation.TooLong -> "최대 ${PROMPT_MAX_LENGTH}자까지 입력할 수 있어요"
            }

            _uiState.update {
                it.copy(
                    promptInput = trimmedToMax,
                    promptInputErrMsg = errorMessage,
                    isPromptVaild = isValid
                )
            }
        }
    }


    fun openBottomSheet(type: BottomSheetType) {
        _uiState.update {
            it.copy(
                bottomSheetType = type,
                showBottomSheet = true,
            )
        }
    }

    fun closeBottomSheet() {
        _uiState.update {
            it.copy(
                bottomSheetType = BottomSheetType.NONE,
                showBottomSheet = false,
            )
        }
    }


    fun onDifficultySelected(difficulty: Difficulty) {
        _uiState.update {
            it.copy(
                difficulty = difficulty,
                bottomSheetType = BottomSheetType.NONE,
            )
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = getCategoriesUseCase()) {

                is ApiResult.Success -> {
                    Log.d("카테고리 로직: ", "카테고리 데이터: ${result.data}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            categories = result.data
                        )
                    }
                }

                is ApiResult.SessionExpired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSessionExpired = true,
                            pageErrorMessage = result.message
                        )
                    }
                }

                is ApiResult.ServerError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pageErrorMessage = result.message
                        )
                    }
                }

                is ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pageErrorMessage = result.message
                        )
                    }
                }

                is ApiResult.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pageErrorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun calculateTargetPageForItemUnChecked(
        selection: CategorySelectionState
    ): Int {
        return when {
            selection.depth1 == null -> 0
            selection.depth2 == null -> 1
            else -> 2
        }
    }


    fun toggleDepth1(category: Category) {
        _uiState.update { state ->
            val newSelection =
                if (state.categorySelection.depth1?.id == category.id) {
                    CategorySelectionState() // 전체 해제
                } else {
                    CategorySelectionState(depth1 = category)
                }

            state.copy(
                categorySelection = newSelection,
                targetCategoryPage = calculateTargetPageForItemUnChecked(newSelection)
            )
        }
    }


    fun toggleDepth2(category: Category) {
        _uiState.update { state ->
            val currentDepth2 = state.categorySelection.depth2
            val isUnselecting = currentDepth2?.id == category.id

            val newSelection = if (isUnselecting) {
                // 🔁 2뎁스 해제 → 1뎁스로 복귀
                state.categorySelection.copy(
                    depth2 = null,
                    depth3 = null
                )
            } else {
                // ✅ 2뎁스 선택
                state.categorySelection.copy(
                    depth2 = category,
                    depth3 = null
                )
            }

            state.copy(
                categorySelection = newSelection,
                selectedCategoryId = null, // 2뎁스에서는 서버 id 확정 ❌
                targetCategoryPage = if (isUnselecting) {
                    0   // ⭐ 2뎁스 해제 → 1뎁스 페이지
                } else {
                    2   // 2뎁스 선택 → 3뎁스 페이지
                }
            )
        }
    }


    fun toggleDepth3(category: Category) {
        _uiState.update { state ->
            val currentDepth3 = state.categorySelection.depth3
            val isUnselecting = currentDepth3?.id == category.id

            val newSelection = if (isUnselecting) {
                state.categorySelection.copy(depth3 = null)
            } else {
                state.categorySelection.copy(depth3 = category)
            }


            state.copy(
                categorySelection = newSelection,
                selectedCategoryId = category.serverCategoryId,
                targetCategoryPage = if (isUnselecting) {
                    1   // ⭐ 3뎁스 해제 → 2뎁스 페이지
                } else {
                    2   // 3뎁스 선택 → 3뎁스 페이지 유지
                }
            )
        }
    }


    fun clearDepth1() {
        _uiState.update {
            it.copy(
                categorySelection = CategorySelectionState(),
                targetCategoryPage = 0
            )
        }
    }

    fun clearDepth2() {
        _uiState.update { state ->
            val newSelection =
                state.categorySelection.copy(
                    depth2 = null,
                    depth3 = null
                )

            state.copy(
                categorySelection = newSelection,
                targetCategoryPage = calculateTargetPageForItemUnChecked(newSelection)
            )
        }
    }

    fun clearDepth3() {
        _uiState.update { state ->
            val newSelection =
                state.categorySelection.copy(depth3 = null)

            state.copy(
                categorySelection = newSelection,
                targetCategoryPage = calculateTargetPageForItemUnChecked(newSelection)
            )
        }
    }


    fun onFileDeleted(
    ) {
        _uiState.update {
            it.copy(
                selectedFileUri = null,
                selectedFileName = ""
            )
        }
    }

    fun onFileSelected(
        uri: Uri,
        fileName: String,
        mimeType: String,
        size: Long
    ) {
        // 🔍 DEBUG 1: 원본 파일명 그대로 출력
        println("DEBUG: Selected fileName = [$fileName]")
        println("DEBUG: Selected mimeType = [$mimeType]")
        println("DEBUG: Selected fileSize = [$size] bytes")

        // 🔹 1. MIME 타입 검증
        if (mimeType != "application/pdf") {
            println("DEBUG: MIME type validation failed")

            _uiState.update {
                it.copy(
                    pageErrorMessage = "PDF 파일만 업로드할 수 있어요. (파일 형식 오류)"
                )
            }
            return
        }

        // 🔹 2. 파일 크기 검증 (50MB)
        val maxSize = 50L * 1024 * 1024
        if (size > maxSize) {
            println("DEBUG: File size validation failed")

            _uiState.update {
                it.copy(
                    pageErrorMessage = "파일 용량은 최대 50MB까지 업로드할 수 있어요."
                )
            }
            return
        }

        // 🔹 3. 확장자 검증 (대소문자 확인용)
        val lowerCaseFileName = fileName.lowercase()
        val isPdfExtension = lowerCaseFileName.endsWith(".pdf")

        // 🔍 DEBUG 2: 확장자 관련 디버깅
        val actualExtension = fileName.substringAfterLast('.', missingDelimiterValue = "")
        println("DEBUG: Actual file extension = [$actualExtension]")
        println("DEBUG: isPdfExtension (case-insensitive) = [$isPdfExtension]")

        if (!isPdfExtension) {
            _uiState.update {
                it.copy(
                    pageErrorMessage = "확장자가 .pdf 인 파일만 업로드할 수 있어요."
                )
            }
            return
        }

        if (!isPdfExtension) {
            _uiState.update {
                it.copy(
                    pageErrorMessage = "PDF 파일만 업로드할 수 있어요."
                )
            }
            return
        }

        // 🔹 4. 모든 검증 통과 → UI 상태에 저장
        println("DEBUG: File validation passed")

        val normalizedFileName = fileName
            .substringBeforeLast('.', fileName)
            .lowercase() + ".pdf"

        // 🔹 3. 모든 검증 통과 → UI 상태에 저장
        // 이 시점부터 presignedUrl 발급 → PUT 업로드가 가능해짐
        _uiState.update {
            it.copy(
                selectedFileUri = uri,
                selectedFileName = normalizedFileName,
                selectedFileMimeType = mimeType,
                selectedFileSize = size,
                pageErrorMessage = null
            )
        }
    }

    fun clearPageErrorMessage() {
        _uiState.update {
            it.copy(pageErrorMessage = null)
        }
    }

    fun issuePresignedUrl() {
        val fileName = _uiState.value.selectedFileName
        if (fileName.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    pageErrorMessage = null
                )
            }

            val result = issuePresignedUrlUseCase(fileName)

            handlePresignedResult(result)
        }
    }

    private fun handlePresignedResult(
        result: ApiResultV2<PresignedResponse>
    ) {
        when (result) {

            is ApiResultV2.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        presignedUrl = result.data.presignedUrl,
                        fileKey = result.data.key
                    )
                }
            }

            is ApiResultV2.ServerError -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pageErrorMessage = result.message
                    )
                }
            }

            is ApiResultV2.SessionExpired -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pageErrorMessage = result.message
                    )
                }
                // TODO 로그인 이동
            }

            is ApiResultV2.NetworkError,
            is ApiResultV2.UnknownError -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pageErrorMessage = result.uiMessage
                    )
                }
            }
        }
    }



    fun selectLearningMethod(type: GoalType) {
        _uiState.update {
            it.copy(goalType = type)
        }
    }

    fun onMinuteSelected(minute: Int) {
        _uiState.update {
            it.copy(selectedMinute = minute)
        }
    }

    fun updateCommuteTime() {
        viewModelScope.launch {
            val current = _uiState.value

            // 1️⃣ 로딩 시작
            _uiState.value = current.copy(
                isLoading = true,
                errorMessage = ""
            )

            val result = updateCommuteTimeUseCase(
                start = current.workInTime,
                end = current.workOutTime,
                usageTime = 10 // 필요 시 uiState 값으로 교체
            )

            // 2️⃣ 결과 처리
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        isSetWorkInTime = true,
                        isSetWorkOutTime = true
                    )
                }

                is ApiResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pageErrorMessage = result.message
                    )
                    // 🔔 여기서 로그인 화면 이동 이벤트 트리거 가능
                }

                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pageErrorMessage = "네트워크 연결을 확인해주세요."
                    )
                }

                is ApiResult.ServerError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pageErrorMessage = result.message
                            ?: "요청 처리 중 오류가 발생했습니다."
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pageErrorMessage = "알 수 없는 오류가 발생했습니다."
                    )
                }
            }
        }
    }


    /**
     * 🔹 화면 진입 시 호출
     * 🔹 권한 상태만 조회 (팝업 ❌)
     */
    fun syncNotificationPermission(isGranted: Boolean) {
        _uiState.update {
            it.copy(
                isNotificationGranted = isGranted,
                isNotificationChecked = isGranted
            )
        }
    }

    /**
     * 🔹 체크박스 / 버튼 클릭 시 호출
     */
    fun onNotificationOptionClicked() {
        val state = _uiState.value

        // 이미 허용됨 → 바로 체크
        if (state.isNotificationGranted) {
            _uiState.update {
                it.copy(isNotificationChecked = true)
            }
            return
        }

        // 아직 허용 안 됨 → 권한 요청 트리거
        _uiState.update {
            it.copy(requestNotificationPermission = true)
        }
    }

    /**
     * 🔹 OneSignal 권한 요청 결과 콜백
     */
    fun onNotificationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                isNotificationGranted = granted,
                isNotificationChecked = granted,
                requestNotificationPermission = false
            )
        }
    }

    /**
     * 🔹 이벤트 소비 (중복 호출 방지)
     */
    fun consumeNotificationPermissionRequest() {
        _uiState.update {
            it.copy(requestNotificationPermission = false)
        }
    }

    /* -----------------------------
     * 체크박스 토글
     * ----------------------------- */

    fun onAgreementCheckedChange(checked: Boolean) {
        _uiState.update {
            it.copy(isCheckedAgreement = checked)
        }
    }

    // 필요 시 토글 방식도 가능
    fun toggleAgreement() {
        _uiState.update {
            it.copy(isCheckedAgreement = !it.isCheckedAgreement)
        }
    }

    /* -----------------------------
     * BottomSheet 제어
     * ----------------------------- */

    fun openTimeSheet(type: TimeType) {
        _uiState.update {
            it.copy(
                showBottomSheet = true,
                currentTimeType = type
            )
        }
    }

    fun closeTimeSheet() {
        _uiState.update {
            it.copy(
                showBottomSheet = false,
                currentTimeType = TimeType.NOTTING
            )
        }
    }

    /* -----------------------------
     * 시간 변경
     * ----------------------------- */

    fun onTimeChanged(newTime: TimeState) {
        _uiState.update { state ->
            when (state.currentTimeType) {
                TimeType.OUT -> state.copy(
                    workOutTime = newTime,
                    isSetWorkOutTime = true
                )

                TimeType.IN -> state.copy(
                    workInTime = newTime,
                    isSetWorkInTime = true
                )

                TimeType.NOTTING -> state.copy()
            }
        }
    }

    /* -----------------------------
     * BottomSheet에 주입할 현재 시간
     * ----------------------------- */

    fun getCurrentTime(): TimeState {
        return when (uiState.value.currentTimeType) {
            TimeType.OUT -> uiState.value.workOutTime
            TimeType.IN -> uiState.value.workInTime
            TimeType.NOTTING -> TimeState.amTime()
        }
    }

    /* -----------------------------
     * BottomSheet 타이틀
     * ----------------------------- */

    fun getSheetTitle(): String {
        return when (uiState.value.currentTimeType) {
            TimeType.OUT -> "집을 나오는 시간"
            TimeType.IN -> "집을 들어가는 시간"
            TimeType.NOTTING -> "시간"
        }
    }


    fun onNameTextChanged(input: String) {
        viewModelScope.launch {
            // ✅ 입력은 최대 10자까지만 "받는다"(저장)
            val trimmedToMax = if (input.length > MAX_LENGTH) input.take(MAX_LENGTH) else input

            // ✅ 유효성은 별도로 판단 (입력은 되지만 invalid 가능)
            val violation = when {
                trimmedToMax.isEmpty() -> NameViolation.Empty
                trimmedToMax.length < MIN_LENGTH -> NameViolation.Empty // 사실상 동일
                trimmedToMax.contains(" ") -> NameViolation.HasSpace
                !trimmedToMax.matches(ALLOWED_REGEX) -> NameViolation.HasSpecialChar
                else -> NameViolation.None
            }

            val isValid =
                violation == NameViolation.None && trimmedToMax.length in MIN_LENGTH..MAX_LENGTH

            val message = when (violation) {
                NameViolation.None -> ""
                NameViolation.Empty -> "1자 이상 입력해주세요"
                NameViolation.HasSpace -> "공백은 사용할 수 없어요"
                NameViolation.HasSpecialChar -> "특수문자는 사용할 수 없어요 (한글/영문/숫자만)"
                NameViolation.TooLong -> "10자 이하로 입력해주세요" // 현재 take(MAX)라 실제로는 잘 안 옴
            }

            _uiState.update {
                it.copy(
                    charName = trimmedToMax,
                    isNameValid = isValid,
                    errorMessage = message,
                    violation = violation
                )
            }
        }
    }

    fun onConfirmClick() {
        val state = _uiState.value
        if (!state.isNameValid || state.isLoading) return

        viewModelScope.launch {
            Log.d("VM", "▶️ onConfirmClick start")

            val result = registerUserNameUseCase(state.charName)

            Log.d("VM", "✅ result type = ${result::class.simpleName}")
            Log.d("VM", "✅ result.uiMessage = ${result.uiMessage}")

            when (result) {
                is ApiResultV2.ServerError -> {
                    Log.d("VM", "❌ ServerError.code = ${result.code}")
                    Log.d("VM", "❌ ServerError.message = ${result.message}")
                    Log.d("VM", "❌ ServerError.errorType = ${result.errorType}")
                }

                else -> result.uiMessage
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result =
                registerUserNameUseCase(state.charName)
            ) {
                is ApiResultV2.Success -> {
                    // ✅ 성공 시: 에러 메시지 완전 제거
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNameValid = true,
                            errorMessage = "" // ← 핵심
                        )
                    }
                }

                is ApiResultV2.ServerError -> {
                    // 특정 필드(name)에 대한 커스텀 처리가 필요하다면 여기서 로직 수행
                    // uiMessage는 전체 에러를 문자열로 합쳐주므로,
                    // 특정 필드만 콕 집어서 UI에 빨간불을 켜야 한다면 아래처럼 직접 접근하는 게 좋습니다.

                    val errorMessage = when (val error = result.errorType) {
                        is DomainError.Validation -> {
                            // "name" 필드 에러만 찾아내기
                            val nameError = error.errors.find { it.field == "name" }
                            nameError?.message ?: result.uiMessage // 없으면 전체 메시지
                        }
                        // 그 외(Message, None)는 확장 프로퍼티가 주는 메시지 그대로 사용
                        else -> result.uiMessage
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNameValid = false,
                            errorMessage = errorMessage
                        )
                    }
                }

                else -> {
                    // 나머지 모든 에러(네트워크, 세션, 알 수 없음 등)는
                    // 이미 정의해둔 확장 프로퍼티 uiMessage가 알아서 메시지를 꺼내줍니다.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNameValid = false,
                            errorMessage = result.uiMessage // 깔끔하게 해결!
                        )
                    }
                }
            }
        }
    }


    fun nextPage() {
        if (currentPage < totalPage) {
            Log.d("1증가 전", "증가함, ${currentPage}/${totalPage}")
            viewModelScope.launch {
                _uiState.update { currentState ->
                    currentState.copy(
                        currentPage = currentPage + 1
                    )
                }
            }
            Log.d("1증가 후", "증가함, ${currentPage}/${totalPage}")
        }
    }

    fun prevPage() {
        if (currentPage > 0) {
            Log.d("1감소 전", "감소함, ${currentPage}/${totalPage}")
            viewModelScope.launch {
                _uiState.update { currentState ->
                    currentState.copy(
                        currentPage = currentPage - 1
                    )
                }
            }
            Log.d("1감소 후", "감소함, ${currentPage}/${totalPage}")
        }
    }
}
