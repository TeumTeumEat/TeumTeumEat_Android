package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onesignal.OneSignal
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_response.PresignedResponse
import com.teumteumeat.teumteumeat.data.repository.notification.NotificationRepository
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toServerTime
import com.teumteumeat.teumteumeat.domain.usecase.document.GetDocumentsUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.CreateGoalUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.GetCategoriesUseCase
import com.teumteumeat.teumteumeat.domain.usecase.document.IssuePresignedUrlUseCase
import com.teumteumeat.teumteumeat.domain.usecase.document.UploadDocumentUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.UpdateCommuteTimeUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.RegisterUserNameUseCase
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenStore
import com.teumteumeat.teumteumeat.utils.Utils.PrefsUtil
import com.teumteumeat.teumteumeat.utils.Utils.UiUtils.to24HourString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed interface UiEffect {
    object OpenNotificationSetting : UiEffect
}

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    val updateCommuteTimeUseCase: UpdateCommuteTimeUseCase,
    val registerUserNameUseCase: RegisterUserNameUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val issuePresignedUrlUseCase: IssuePresignedUrlUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    val uploadDocumentUseCase: UploadDocumentUseCase,
    val getDocumentsUseCase: GetDocumentsUseCase,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context,
    application: Application,
    val sessionManager: SessionManager,
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

    private val _uiState = MutableStateFlow<UiStateOnboardingState>(UiStateOnboardingState())
    val uiState = _uiState.asStateFlow()

    // 2️⃣ 플로우 상태 (Idle / Loading / Success / Error)
    private val _mainState =
        MutableStateFlow<UiStateOnboardingScreenState>(
            UiStateOnboardingScreenState.Idle
        )
    val mainState = _mainState.asStateFlow()

    private val _effect = MutableSharedFlow<UiEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<UiEffect> = _effect

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    init {
        Log.e("OnBoardingVM", "🔥 ViewModel CREATED ${this.hashCode()}")
    }

    fun submitOnBoarding() {
        // 중복 클릭 방지
        if (_mainState.value == UiStateOnboardingScreenState.Loading) return

        viewModelScope.launch {
            _mainState.value = UiStateOnboardingScreenState.Loading

            // ⭐ progress 애니메이션 시작 (1.8초)
            launch {
                val duration = 1800L
                val startTime = System.currentTimeMillis()

                while (true) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = (elapsed / duration.toFloat()).coerceIn(0f, 1f)

                    _progress.value = progress

                    if (progress >= 1f) break

                    delay(16L) // 약 60fps
                }

                _progress.value = 1f
            }

            val state = _uiState.value

            val startTime = System.currentTimeMillis()

            // 1️⃣ 이름 등록
            val nameResult = setUserNameInternal()
            if (nameResult !is ApiResultV2.Success) {
                moveToError(nameResult)
                return@launch
            }

            // 2️⃣ 출퇴근 정보 저장
            val commuteResult = saveCommuteInfoInternal()
            if (commuteResult !is ApiResultV2.Success) {
                moveToError(commuteResult)
                return@launch
            }

            // 3️⃣ 디바이스 토큰 등록
            val deviceTokenResult = registerDeviceTokenInternal()
            if (deviceTokenResult !is ApiResultV2.Success) {
                moveToError(deviceTokenResult)
                return@launch
            }

            // 4️⃣ 유저 푸시 설정 업데이트 ✅
            val pushSettingResult = updateUserPushSettingInternal()
            if (pushSettingResult !is ApiResultV2.Success) {
                moveToError(pushSettingResult)
                return@launch
            }

            // 5. 문서 업로드 documentID 생성
            Log.d("OnBoardingVM", "타입: ${state.goalTypeUiState}의 퀴즈 생성")
            when (state.goalTypeUiState) {
                GoalTypeUiState.DOCUMENT -> {
                    // 5-1. 목표 생성 및 반환되는 ID uiState에 저장
                    val goalResult = createGoalAndSaveId()
                    if (goalResult !is ApiResultV2.Success) {
                        moveToError(goalResult)
                        return@launch
                    }

                    // 5-2. 문서 확인
                    val uri = state.selectedFileUri
                    if (uri == null) {
                        moveToError(
                            ApiResultV2.ServerError(
                                code = "FILE_URI_MISSING",
                                message = "업로드할 파일을 선택해주세요.",
                                errorType = DomainError.Message("selectedFileUri is null")
                            )
                        )
                        return@launch
                    }

                    // 5-3. 문서 업로드
                    val uploadDocumentResult = uploadDocumentInternal(
                        uri = state.selectedFileUri,
                        fileName = state.selectedFileName,
                        mimeType = state.selectedFileMimeType
                    )
                    if (uploadDocumentResult !is ApiResultV2.Success) {
                        moveToError(uploadDocumentResult)
                        return@launch
                    }

                    // 5-4. 문서 업로드 결과 패치
                    val fetchDocumentResult = fetchCompletedDocument()
                    if (fetchDocumentResult !is ApiResultV2.Success) {
                        moveToError(fetchDocumentResult)
                        return@launch
                    }
                }

                GoalTypeUiState.CATEGORY -> {
                    Log.d("OnBoardingVM", "selectedCategoryID: ${state.selectedCategoryId}")
                    // 5-1. 목표 생성 및 반환되는 ID uiState에 저장
                    val goalResult = createGoalAndSaveId()
                    if (goalResult !is ApiResultV2.Success) {
                        moveToError(goalResult)
                        return@launch
                    }
                }

                GoalTypeUiState.NONE -> {}
            }

            // 🔹 최소 로딩 1.8초 보장
            val elapsed = System.currentTimeMillis() - startTime
            val remain = 1800L - elapsed
            if (remain > 0) delay(remain)

            _mainState.value = UiStateOnboardingScreenState.Success
        }
    }

    private suspend fun createGoalAndSaveId(): ApiResultV2<Int> {
        val result = createGoalRequest()
        if (result !is ApiResultV2.Success) return result

        _uiState.update { it.copy(goalId = result.data) }
        return result
    }

    private suspend fun moveToError(result: ApiResultV2<*>) {
        when (result) {
            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
            }

            else -> {
                _mainState.value = UiStateOnboardingScreenState.Error(
                    message = result.uiMessage
                )
            }
        }

    }

    private suspend fun updateUserPushSettingInternal(): ApiResultV2<Unit> {
        val state = _uiState.value

        // 예시: 온보딩에서 사용자가 선택한 푸시 여부
        val pushEnabled = state.isNotificationChecked

        return userRepository.updateUserPushEnableSettings(
            pushEnabled = pushEnabled
        )
    }

    private suspend fun registerDeviceTokenInternal(): ApiResultV2<Unit> {
        val current = _uiState.value

        val fcmToken = FcmTokenStore.get(context)
            ?: return ApiResultV2.UnknownError("디바이스 토큰이 없습니다.")

        val deviceType = "ANDROID"

        return notificationRepository.registerDeviceToken(
            token = fcmToken,
            deviceType = deviceType
        )
    }


    private suspend fun setUserNameInternal(): ApiResultV2<Any> {
        val state = _uiState.value

        if (!state.isNameValid) {
            return ApiResultV2.UnknownError(
                message = "이름이 올바르지 않습니다."
            )
        }

        return when (val result = registerUserNameUseCase(state.charName)) {

            is ApiResultV2.Success -> {
                _uiState.update {
                    it.copy(
                        isNameValid = true,
                        errorMessage = ""
                    )
                }
                ApiResultV2.Success(result.message, Unit)
            }

            is ApiResultV2.ServerError -> {
                val errorMessage = when (val error = result.errorType) {
                    is DomainError.Validation -> {
                        error.errors.find { it.field == "name" }?.message
                            ?: result.uiMessage
                    }

                    else -> result.uiMessage
                }

                _uiState.update {
                    it.copy(
                        isNameValid = false,
                        errorMessage = errorMessage
                    )
                }

                result
            }

            else -> result
        }
    }


    private suspend fun saveCommuteInfoInternal(): ApiResultV2<Unit> {
        val current = _uiState.value

        val usageTime = current.selectedMinute
            ?: return ApiResultV2.UnknownError("사용 시간을 선택해주세요.")

        Log.d("퇴근시간 디버깅", "출근시간: ${current.workInTime}, 퇴근시간: ${current.workOutTime}")
        return updateCommuteTimeUseCase(
            startTime = current.workInTime.toServerTime(),
            endTime = current.workOutTime.toServerTime(),
            usageTime = usageTime
        )
    }


    private suspend fun createGoalRequestForCategory(selectedCategoryId: Int?): ApiResultV2<Int> {
        val state = _uiState.value

        val studyPeriodStr =
            state.studyPeriod?.toString()?.plus("주") ?: "기간 설정 안함"

        val request = CreateGoalRequest(
            type = state.goalTypeUiState,
            studyPeriod = studyPeriodStr,
            difficulty = state.difficulty,
            prompt = state.promptInput.takeIf { it.isNotBlank() },
            categoryId = state.selectedCategoryId
        )

        return createGoalUseCase(request)
    }


    private suspend fun createGoalRequest(): ApiResultV2<Int> {
        val state = _uiState.value

        val studyPeriodStr =
            state.studyPeriod?.toString()?.plus("주") ?: "기간 설정 안함"

        val request = CreateGoalRequest(
            type = state.goalTypeUiState,
            studyPeriod = studyPeriodStr,
            difficulty = state.difficulty,
            prompt = state.promptInput.takeIf { it.isNotBlank() },
            categoryId = if (state.goalTypeUiState == GoalTypeUiState.CATEGORY) {
                state.selectedCategoryId
            } else {
                null                 // DOCUMENT → categoryId 미포함
            }
        )

        return createGoalUseCase(request)
    }

    private suspend fun uploadDocumentInternal(
        uri: Uri,
        fileName: String,
        mimeType: String
    ): ApiResultV2<Unit> {

        val goalId = _uiState.value.goalId

        return when (
            val result = uploadDocumentUseCase(
                goalId = goalId,
                uri = uri,
                fileName = fileName,
                mimeType = mimeType
            )
        ) {

            is ApiResultV2.Success -> result
            is ApiResultV2.ServerError -> result
            is ApiResultV2.NetworkError -> result
            is ApiResultV2.SessionExpired -> result
            is ApiResultV2.UnknownError -> result
        }
    }

    private suspend fun fetchCompletedDocument(): ApiResultV2<Unit> {

        val goalId = _uiState.value.goalId

        return when (val result = getDocumentsUseCase(goalId)) {

            is ApiResultV2.Success -> {
                val documents = result.data

                val documentId = documents
                    .firstOrNull()
                    ?.documentId
                    ?: return ApiResultV2.ServerError(
                        code = "DOCUMENT_NOT_FOUND",
                        message = "문서를 찾을 수 없습니다.",
                        errorType = DomainError.Message("no document")
                    )

                Log.d("OnBoardingVM", "문서 ID: $documentId")
                // 위 documentId를 SharedPreference에 저장
                // ✅ 성공 시 UiState에 documentId 저장
                _uiState.update {
                    it.copy(documentId = documentId)
                }

                ApiResultV2.Success(
                    message = result.message,
                    data = Unit
                )
            }

            is ApiResultV2.ServerError -> result
            is ApiResultV2.NetworkError -> result
            is ApiResultV2.SessionExpired -> result
            is ApiResultV2.UnknownError -> result
        }
    }


    fun submitOnBoardingTestError() {
        // 중복 실행 방지
        Log.d("OnBoardingVM", "온보딩UiState 상태: ${_mainState.value}")
        if (_mainState.value == UiStateOnboardingScreenState.Loading) return

        viewModelScope.launch {
            _mainState.value = UiStateOnboardingScreenState.Loading

            // ✅ 최소 1초 로딩 보장
            delay(1800)

            // ✅ 테스트용 에러 상태 진입
            _mainState.value = UiStateOnboardingScreenState.Error(
                message = "테스트 에러 페이지입니다.\n잠시 후 다시 시도해주세요."
            )
        }
    }

    fun getErrorState(
        message: String,
        onRetry: () -> Unit
    ): ErrorState {
        return ErrorState(
            title = "문제가 발생했어요",
            description = message,
            retryLabel = "다시 시도",
            onRetry = onRetry
        )
    }

    fun showTestError() {
        viewModelScope.launch {
            _mainState.value = UiStateOnboardingScreenState.Loading
            kotlinx.coroutines.delay(500)

            _mainState.value = UiStateOnboardingScreenState.Error(
                message = "테스트용 에러입니다.\n네트워크 상태를 확인해주세요."
            )
        }
    }

    fun resetMainState() {
        _mainState.value = UiStateOnboardingScreenState.Idle
    }

    private fun sendEffect(effect: UiEffect) {
        // suspend가 아니라도 보낼 수 있게 tryEmit 사용
        _effect.tryEmit(effect)
    }

    fun onCreateGoalClick() {
        viewModelScope.launch {
            val request = _uiState.value.run {
                val studyPeriodStr: String =
                    if (studyPeriod != null) studyPeriod.toString() + "주" else "기간 설정 안함"
                CreateGoalRequest(
                    type = goalTypeUiState,
                    studyPeriod = studyPeriodStr,
                    difficulty = difficulty,
                    prompt = promptInput.takeIf { it.isNotBlank() },
                    categoryId = if (goalTypeUiState == GoalTypeUiState.CATEGORY) {
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
                studyPeriod = week,
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

    fun logLeafCategories(categories: List<Category>) {
        fun traverse(category: Category) {
            if (category.children.isEmpty()) {
                Log.d(
                    "LeafCheck",
                    "LEAF name=${category.name}, serverId=${category.serverCategoryId}"
                )
            } else {
                category.children.forEach { traverse(it) }
            }
        }

        categories.forEach { traverse(it) }
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
                    Log.d("카테고리 로직: ", "저장된 카테고리 데이터: ${_uiState.value.categories}")
                    logLeafCategories(result.data)
                }

                is ApiResult.SessionExpired -> {
                    sessionManager.expireSession()
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
                // 🔁 2뎁스 해제 → 하위 전부 해제
                state.categorySelection.copy(
                    depth2 = null,
                    depth3 = null,
                    depth4 = null
                )
            } else {
                // ✅ 2뎁스 선택 → 하위 초기화
                state.categorySelection.copy(
                    depth2 = category,
                    depth3 = null,
                    depth4 = null
                )
            }

            state.copy(
                categorySelection = newSelection,
                selectedCategoryId = null,

                // ⭐ 핵심 규칙
                targetCategoryPage = if (isUnselecting) {
                    state.targetCategoryPage // ❗ 페이지 유지
                } else {
                    1 // 3뎁스 페이지
                }
            )
        }
    }

    fun toggleDepth3(category: Category) {
        Log.d(
            "OnBoardingVM",
            "toggleDepth3 input → " +
                    "name=${category.name}, " +
                    "serverId=${category.serverCategoryId}, " +
                    "children=${category.children.size}"
        )

        _uiState.update { state ->
            val currentDepth3 = state.categorySelection.depth3
            val isUnselecting = currentDepth3?.id == category.id

            val newDepth3 = if (isUnselecting) null else category

            state.copy(
                categorySelection = state.categorySelection.copy(
                    depth3 = newDepth3,
                    depth4 = null // ⭐ 3뎁스 변경 시 4뎁스 초기화
                ),
                selectedCategoryId = null,
                // ⭐ 핵심 규칙
                targetCategoryPage = if (isUnselecting) {
                    0 // 2뎁스 페이지
                } else {
                    2 // 4뎁스 페이지
                }
            )
        }
    }

    fun toggleDepth4(category: Category) {
        if (category.children.isNotEmpty()) return
        if (category.serverCategoryId == null) return

        _uiState.update { state ->
            val currentDepth4 = state.categorySelection.depth4
            val isUnselecting = currentDepth4?.id == category.id

            val newDepth4 = if (isUnselecting) null else category

            state.copy(
                categorySelection = state.categorySelection.copy(
                    depth4 = newDepth4
                ),

                selectedCategoryId = newDepth4?.serverCategoryId,

                // ⭐ 핵심 규칙
                targetCategoryPage = if (isUnselecting) {
                    1 // 3뎁스 페이지
                } else {
                    state.targetCategoryPage // ❗ 페이지 유지
                }
            )
        }

        Log.d(
            "OnBoardingVM",
            "depth4=${_uiState.value.categorySelection.depth4?.name}, " +
                    "selectedCategoryId=${_uiState.value.selectedCategoryId}"
        )
    }


    fun resetCategorySelection() {
        _uiState.update { state ->
            state.copy(
                categorySelection = CategorySelectionState(), // depth1,2,3 전부 초기화
                selectedCategoryId = null,
                targetCategoryPage = 0 // ⭐ 1뎁스 페이지로 이동
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
                        preSignedUrl = result.data.presignedUrl,
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


    fun selectLearningMethod(type: GoalTypeUiState) {
        _uiState.update {
            it.copy(goalTypeUiState = type)
        }
    }

    fun onMinuteSelected(minute: Int) {
        _uiState.update {
            it.copy(selectedMinute = minute)
        }
    }

    fun saveCommuteInfo() {
        viewModelScope.launch {

            val current = uiState.value

            // 🔐 방어 로직 (null / invalid 상태 차단)
            val usageTime = current.selectedMinute
                ?: run {
                    _uiState.update {
                        it.copy(pageErrorMessage = "사용 시간을 선택해주세요.")
                    }
                    return@launch
                }

            val result = updateCommuteTimeUseCase(
                startTime = current.workInTime.toServerTime(),
                endTime = current.workOutTime.toServerTime(),
                usageTime = usageTime
            )

            _uiState.update {
                it.copy(
                    pageErrorMessage = result.uiMessage
                )
            }
        }
    }


    /**
     * 🔹 화면 진입 시 호출
     * 🔹 권한 상태만 조회 (팝업 ❌)
     */
    fun syncNotificationPermission(isGranted: Boolean) {
        Log.d(
            "NotificationDebug",
            "onResume permission = ${OneSignal.Notifications.permission}"
        )

        if (!isGranted) {
            PrefsUtil.saveNotificationDeniedOnce(context)
        }

        _uiState.update {
            if (it.isNotificationGranted == isGranted) return@update it

            it.copy(
                isNotificationGranted = isGranted,
                isNotificationChecked = isGranted
            )
        }
    }

    fun hasDeniedBefore(): Boolean {
        val denied = PrefsUtil.hasNotificationDeniedOnce(context)
        Log.d("NotificationDebug", "hasDeniedBefore = $denied")
        return PrefsUtil.hasNotificationDeniedOnce(context)
    }

    /**
     * 🔹 체크박스 / 버튼 클릭 시 호출
     */
    fun onNotificationOptionClicked() {
        val state = _uiState.value

        val hasRequestedBefore = hasDeniedBefore()
        val deniedBefore = hasRequestedBefore && !state.isNotificationGranted

        // val deniedBefore = hasDeniedBefore()
        Log.d("NotificationDebug", "clicked, deniedBefore=$deniedBefore")

        when {
            // 1️⃣ 이미 권한 허용 → 해제 안내
            state.isNotificationGranted -> {
                _uiState.update {
                    it.copy(notificationGuideType = NotificationSettingGuideType.DISABLE)
                }
            }

            // 3️⃣ 한 번 거부 후 재시도 → 설정 이동 안내
            !state.isNotificationGranted && deniedBefore -> {
                _uiState.update {
                    it.copy(notificationGuideType = NotificationSettingGuideType.ENABLE)
                }
            }

            // 2️⃣ 최초 요청 → 시스템 기본 팝업
            else -> {
                _uiState.update {
                    it.copy(requestNotificationPermission = true)
                }
            }
        }
    }

    // ✅ ① 안내 팝업 닫기 처리 (필수)
    fun closeNotificationDisableGuide() {
        _uiState.update {
            it.copy(showNotificationDisableGuide = false)
        }
    }

    fun openNotificationSetting() {
        sendEffect(UiEffect.OpenNotificationSetting)
        closeNotificationDisableGuide()
    }

    /**
     * 🔹 OneSignal 권한 요청 결과 콜백
     */
    fun onNotificationPermissionResult(granted: Boolean) {
        Log.d("NotificationDebug", "permission result = $granted")

        if (!granted) {
            // 🔴 한 번이라도 거부했으면 저장
            PrefsUtil.saveNotificationDeniedOnce(context)
            Log.d("NotificationDebug", "saveNotificationDeniedOnce called")
        }

        _uiState.update {
            it.copy(
                isNotificationGranted = granted,
                requestNotificationPermission = false
            )
        }
    }

    fun closeNotificationSettingGuide() {
        _uiState.update {
            it.copy(
                showNotificationSettingGuide = false,
                notificationGuideType = NotificationSettingGuideType.NONE
            )
        }
    }

    fun showNotificationSettingGuideForEnable() {
        _uiState.update { it.copy(notificationGuideType = NotificationSettingGuideType.ENABLE) }
    }

    fun showNotificationSettingGuideForDisable() {
        _uiState.update { it.copy(notificationGuideType = NotificationSettingGuideType.DISABLE) }
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
        _uiState.update { state ->
            // ✅ 1. 어떤 시간을 보여줄지 결정
            val initialTime = when (type) {
                TimeType.IN -> {
                    if (state.isSetFirstAlarmTime) {
                        state.workInTime       // 🔵 이미 선택한 값
                    } else {
                        TimeState.firstTime()     // 🟡 초기 기본값
                    }
                }

                TimeType.OUT -> {
                    if (state.isSetSecondAlarmTime) {
                        state.workOutTime
                    } else {
                        TimeState.secondTime()
                    }
                }

                TimeType.NOTTING -> state.tempTime
            }

            println("🟣 [openTimeSheet]")
            println("🟣 initialTime(before normalize) = $initialTime")

            val normalized = initialTime
            println("🟣 initialTime(after normalize) = $normalized")


            state.copy(
                showBottomSheet = true,
                currentTimeType = type,
                tempTime = initialTime
            )
        }
    }

    fun closeTimeSheet() {
        _uiState.update {
            it.copy(
                showBottomSheet = false,
            )
        }
    }

    /* -----------------------------
 * 시간 확정 저장
 * ----------------------------- */
    fun confirmTime() {
        val before = uiState.value
        println("🟡 [confirmTime] START")
        println("🟡 currentTimeType = ${before.currentTimeType}")
        println("🟡 tempTime = ${before.tempTime}")
        println("🟡 workInTime(before) = ${before.workInTime}")
        println("🟡 workOutTime(before) = ${before.workOutTime}")

        _uiState.update { state ->

            // 🔍 1️⃣ 24시간 변환 시도 로그
            val time24 = state.tempTime.to24HourString()
            if (time24 == null) {
                println("🔴 [confirmTime] to24HourString() FAILED")
                println("🔴 tempTime = ${state.tempTime}")
                return@update state.copy(
                    pageErrorMessage = "00시부터 23시까지만 선택할 수 있어요."
                )
            }

            println("🟢 [confirmTime] to24HourString SUCCESS = $time24")

            when (state.currentTimeType) {
                TimeType.IN -> {
                    println("🟢 [confirmTime] BRANCH = TimeType.IN")
                    state.copy(
                        workInTime = state.tempTime,
                        isSetFirstAlarmTime = true,
                        currentTimeType = TimeType.NOTTING
                    )
                }

                TimeType.OUT -> {
                    println("🟢 [confirmTime] BRANCH = TimeType.OUT")
                    state.copy(
                        workOutTime = state.tempTime,
                        isSetSecondAlarmTime = true,
                        currentTimeType = TimeType.NOTTING
                    )
                }

                TimeType.NOTTING -> {
                    println("🔴 [confirmTime] BRANCH = TimeType.NOTTING (NO-OP)")
                    state
                }
            }
        }

        val after = uiState.value
        println("🟡 [confirmTime] END")
        println("🟡 workInTime(after) = ${after.workInTime}")
        println("🟡 workOutTime(after) = ${after.workOutTime}")
    }


    /* -----------------------------
     * 시간 변경
     * ----------------------------- */

    fun onTimeChanged(newTime: TimeState) {
        println("🔵 [onTimeChanged] newTime = $newTime")

        _uiState.update {
            it.copy(tempTime = newTime)
        }

        /*        _uiState.update { state ->
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
                }*/
    }

    /* -----------------------------
     * BottomSheet에 주입할 현재 시간
     * ----------------------------- */

    fun getCurrentTime(): TimeState {
        return when (uiState.value.currentTimeType) {
            TimeType.OUT -> uiState.value.workOutTime
            TimeType.IN -> uiState.value.workInTime
            TimeType.NOTTING -> TimeState.firstTime()
        }
    }

    /* -----------------------------
     * BottomSheet 타이틀
     * ----------------------------- */

    fun getSheetTitle(): String {
        return when (uiState.value.currentTimeType) {
            TimeType.IN -> "집에서 나오는 시간"
            TimeType.OUT -> "집에 돌아가는 시간"
            TimeType.NOTTING -> "시간"
        }
    }


    fun onNameTextChanged(input: String) {
        viewModelScope.launch {
            val isOverMaxLength = input.length > MAX_LENGTH

            val violation = when {
                input.isEmpty() -> NameViolation.Empty
                input.length < MIN_LENGTH -> NameViolation.Empty // 사실상 동일
                isOverMaxLength -> NameViolation.TooLong
                input.contains(" ") -> NameViolation.HasSpace
                !input.matches(ALLOWED_REGEX) -> NameViolation.HasSpecialChar
                else -> NameViolation.None
            }

            val isValid =
                violation == NameViolation.None && input.length in MIN_LENGTH..MAX_LENGTH

            val message = when (violation) {
                NameViolation.TooLong -> "10자 이하로 입력해주세요" // 현재 take(MAX)라 실제로는 잘 안 옴
                NameViolation.Empty -> "1자 이상 입력해주세요"
                NameViolation.HasSpace -> "공백은 사용할 수 없어요"
                NameViolation.HasSpecialChar -> "특수문자는 사용할 수 없어요 (한글/영문/숫자만)"
                NameViolation.None -> ""
            }


            _uiState.update {
                it.copy(
                    charName = input,
                    isNameValid = isValid,
                    errorMessage = message,
                    violation = violation
                )
            }
        }
    }

    fun setUserName() {
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

    fun navigateTo(screen: OnBoardingScreens) {
        val before = _uiState.value.currentScreen
        Log.d("OnBoardingNav", "navigateTo: ${before::class.simpleName}(page=${OnBoardingFlow.currentPage(before)}) → ${screen::class.simpleName}(page=${OnBoardingFlow.currentPage(screen)})")
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun nextPage() {
        _uiState.update { currentState ->
            val nextScreen = OnBoardingFlow.next(currentState.currentScreen)

            if (nextScreen != null) {
                Log.d(
                    "OnBoarding",
                    "nextPage: ${currentState.currentPage} → ${currentState.currentPage + 1}"
                )
                currentState.copy(
                    currentScreen = nextScreen
                )
            } else {
                currentState
            }
        }
    }

    fun prevPage() {
        _uiState.update { currentState ->
            val prevScreen = OnBoardingFlow.prev(currentState.currentScreen)

            if (prevScreen != null && currentState.currentPage > 0) {
                Log.d(
                    "OnBoarding",
                    "prevPage: ${currentState.currentPage} → ${currentState.currentPage - 1}"
                )
                currentState.copy(
                    currentScreen = prevScreen
                )
            } else {
                currentState
            }
        }
    }

    fun updateOfflineFlag() {
        viewModelScope.launch {
            PrefsUtil.setOnboardingCompleted(context, true)
        }
    }

}
