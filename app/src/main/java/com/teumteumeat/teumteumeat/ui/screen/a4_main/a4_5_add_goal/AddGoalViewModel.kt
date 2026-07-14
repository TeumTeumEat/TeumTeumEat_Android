package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.datastore.GoalTrackingDataStore
import com.teumteumeat.teumteumeat.data.datastore.LastCompletedGoal
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_request.UpdateGoalRequest
import com.teumteumeat.teumteumeat.domain.model.RequestPromptOption
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.model.sse.DocumentProcessingEvent
import com.teumteumeat.teumteumeat.domain.usecase.document.GetDocumentsUseCase
import com.teumteumeat.teumteumeat.domain.usecase.document.GetPdfPageCountUseCase
import com.teumteumeat.teumteumeat.domain.usecase.document.StreamDocumentProcessingUseCase
import com.teumteumeat.teumteumeat.domain.usecase.goal.EmitGoalRefreshUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.CreateGoalUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.GetCategoriesUseCase
import com.teumteumeat.teumteumeat.domain.usecase.document.UploadDocumentUseCase
import com.teumteumeat.teumteumeat.domain.usecase.goal.UpdateGoalUseCase
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.BottomSheetType
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.CategorySelectionState
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.PromptViolation
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.utils.Utils.PrefsUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.collections.forEach

@HiltViewModel
class AddGoalViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    val uploadDocumentUseCase: UploadDocumentUseCase,
    val getDocumentsUseCase: GetDocumentsUseCase,
    private val getPdfPageCountUseCase: GetPdfPageCountUseCase,
    application: Application,
    val sessionManager: SessionManager,
    val goalRepository: GoalRepository,
    private val analyticsLogger: TeumAnalyticsLogger,
    private val goalTrackingDataStore: GoalTrackingDataStore,
    private val streamDocumentProcessingUseCase: StreamDocumentProcessingUseCase,
    private val emitGoalRefreshUseCase: EmitGoalRefreshUseCase,
    application: Application,
    val sessionManager: SessionManager,
) : ViewModel() {
    private val appContext = application.applicationContext

    private var sseInitialRemainMs: Long = 0L

    // Flow 값으로 currentPage 읽기
    private val currentPage get() = uiState.value.currentPage
    private val totalPage get() = uiState.value.totalPage

    private val _uiState = MutableStateFlow<UiStateAddGoalState>(UiStateAddGoalState())
    val uiState = _uiState.asStateFlow()

    // 2️⃣ 플로우 상태 (Idle / Loading / Success / Error)
    private val _mainState =
        MutableStateFlow<UiStateAddGoalScreenState>(
            UiStateAddGoalScreenState.Idle
        )
    val mainState = _mainState.asStateFlow()

    /** Activity 에서 1회 호출 */
    fun initGoalType(type: DomainGoalType) {
        if (_uiState.value.goalTypeUiState == GoalTypeUiState.NONE) {
            val startScreen = when (type) {
                DomainGoalType.CATEGORY -> AddGoalScreens.CategorySelectScreen
                DomainGoalType.DOCUMENT -> AddGoalScreens.FileUploadScreen
                else -> AddGoalScreens.SelectInputMethodScreen
            }
            _uiState.update{
                it.copy(
                    goalTypeUiState = GoalTypeUiState.valueOf(type.name),
                    isSkipTypeSelect = startScreen != AddGoalScreens.SelectInputMethodScreen,
                    totalPage = if (startScreen == AddGoalScreens.SelectInputMethodScreen) 5 else 4,
                    currentPage = 1
                )
            }

        }
    }

    /** GOAL-002 next_course_start 발화용 직전 완주 목표 스냅샷 캐시 */
    private var lastCompletedGoal: LastCompletedGoal? = null

    /** GOAL-002 next_course_start 중복 발송 방지 플래그 */
    private var hasNextCourseStartLogged = false

    /**
     * GOAL-002 next_course_start 트래킹 초기화. Activity onCreate에서 진입 경로와 무관하게 항상 호출한다.
     * 직전 완주 스냅샷을 조회해 캐시하고, 목표 타입이 이미 정해져 있는 진입(Home "+"·GuideExpiredGoalActivity,
     * SelectInputMethodScreen을 건너뜀)이면 그 자리에서 바로 발화를 시도한다.
     */
    fun initNextCourseStartTracking() {
        viewModelScope.launch {
            lastCompletedGoal = goalTrackingDataStore.getLastCompletedGoal()
            if (_uiState.value.isSkipTypeSelect) {
                logNextCourseStartIfEligible(_uiState.value.goalTypeUiState)
            }
        }
    }

    /**
     * 완주 이력(스냅샷)이 있고 nextGoalType이 유효할 때만 next_course_start를 발화한다.
     * SelectInputMethodScreen "다음" 탭 시 직접 호출되거나, [initNextCourseStartTracking]에서
     * 타입 사전 지정 진입 시 자동 호출된다. 발화 성공 시 스냅샷을 소비(제거)해 동일 완주 건의
     * 중복 발화를 막는다 — 타입 미선택 이탈 시에는 소비하지 않아 다음 진짜 시도에서 재시도할 수 있다.
     */
    fun logNextCourseStartIfEligible(nextGoalType: GoalTypeUiState) {
        if (hasNextCourseStartLogged) return
        val goal = lastCompletedGoal ?: return
        val nextLearningType = when (nextGoalType) {
            GoalTypeUiState.CATEGORY -> "category"
            GoalTypeUiState.DOCUMENT -> "pdf"
            GoalTypeUiState.NONE -> return
        }

        hasNextCourseStartLogged = true
        analyticsLogger.logNextCourseStart(
            prevGoalId = goal.goalId,
            prevCategoryId = goal.categoryId,
            prevLearningType = goal.learningType,
            nextLearningType = nextLearningType,
            isFirstComplete = goal.isFirstComplete,
        )
        viewModelScope.launch { goalTrackingDataStore.clearLastCompletedGoal() }
    }

    fun selectLearningMethod(type: GoalTypeUiState) {
        _uiState.update {
            it.copy(
                goalTypeUiState = type,
            )
        }
    }

    fun resetCategorySelection() {
        _uiState.update {
            it.copy(
                categorySelection = CategorySelectionState(),
                selectedCategoryId = null,
                isCategorySelectionComplete = false,
                targetCategoryPage = 0,
            )
        }
    }

    fun toggleCategory(category: Category, page: Int) {
        val currentPath = _uiState.value.categorySelection.selectedPath
        val isUnselecting = currentPath.getOrNull(page)?.id == category.id
        val isLeaf = !isUnselecting && category.children.isEmpty() && category.serverCategoryId != null

        val newPath = if (isUnselecting) {
            currentPath.take(page)
        } else {
            currentPath.take(page) + category
        }

        val newTargetPage = when {
            isUnselecting -> maxOf(0, page - 1)
            isLeaf -> page
            else -> page + 1
        }

        _uiState.update { state ->
            state.copy(
                categorySelection = CategorySelectionState(selectedPath = newPath),
                selectedCategoryId = if (isLeaf) category.serverCategoryId else null,
                isCategorySelectionComplete = isLeaf,
                targetCategoryPage = newTargetPage
            )
        }

        Log.d(
            "AddGoalVM",
            "page=$page, category=${category.name}, serverId=${category.serverCategoryId}, " +
                    "path=${newPath.map { it.name }}"
        )
    }

    fun navigateBackInCategoryDepth() {
        _uiState.update { state ->
            if (state.targetCategoryPage <= 0) return@update state
            val newPath = state.categorySelection.selectedPath.take(state.targetCategoryPage)
            state.copy(
                categorySelection = CategorySelectionState(selectedPath = newPath),
                selectedCategoryId = null,
                isCategorySelectionComplete = false,
                targetCategoryPage = state.targetCategoryPage - 1
            )
        }
    }

    fun onFileDeleted() {
        _uiState.update {
            it.copy(
                selectedFileUri = null,
                selectedFileName = "",
                isFileUploadComplete = false
            )
        }
    }

    fun openBottomSheet(type: BottomSheetType) {
        _uiState.update { state ->
            val syncedPromptId = if (type == BottomSheetType.PROMPT) {
                state.promptOptions.find { it.label == state.promptInput }?.id
            } else {
                state.selectedPromptId
            }
            state.copy(
                bottomSheetType = type,
                showBottomSheet = true,
                selectedPromptId = syncedPromptId,
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


    fun onDifficultySelected(difficulty: Difficulty) {
        _uiState.update {
            it.copy(
                difficulty = difficulty,
                bottomSheetType = BottomSheetType.NONE,
            )
        }
    }

    fun onPromptSelected(option: RequestPromptOption) {
        _uiState.update { state ->
            val newId = if (state.selectedPromptId == option.id) null else option.id
            state.copy(selectedPromptId = newId)
        }
    }

    /** 확인 버튼 → 현재 selectedPromptId를 그대로 확정 (null이면 선택 해제) */
    fun onConfirmPromptOption() {
        val state = _uiState.value
        val label = state.promptOptions.find { it.id == state.selectedPromptId }?.label ?: ""
        _uiState.update { it.copy(promptInput = label) }
        closeBottomSheet()
    }

    fun updateCategorySelectionComplete(isComplete: Boolean) {
        _uiState.update {
            it.copy(isCategorySelectionComplete = isComplete)
        }
    }

    fun showFileError(title: String, message: String) {
        _uiState.update {
            it.copy(
                popoUpErrorTitle = title,
                popUpErrorMessage = message
            )
        }
    }

    fun clearFileError() {
        _uiState.update {
            it.copy(
                popoUpErrorTitle = null,
                popUpErrorMessage = null
            )
        }
    }

    fun resetMainState() {
        _mainState.value = UiStateAddGoalScreenState.Idle
    }

    fun clearPageErrorMessage() {
        _uiState.update {
            it.copy(pageErrorMessage = null)
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

    fun submitOnBoarding() {
        // 중복 클릭 방지
        if (_mainState.value == UiStateAddGoalScreenState.Loading) return

        viewModelScope.launch {
            _mainState.value = UiStateAddGoalScreenState.Loading

            val state = _uiState.value

            val startTime = System.currentTimeMillis()


            // 5. 문서 업로드 documentID 생성
            Log.d("OnBoardingVM", "타입: ${state.goalTypeUiState}의 퀴즈 생성")
            when(state.goalTypeUiState){
                GoalTypeUiState.DOCUMENT -> {
                    // 1. 목표 생성
                    val createResult = createGoalRequest()
                    if (createResult !is ApiResultV2.Success) {
                        moveToError(createResult)
                        return@launch
                    }

                    // ✅ 생성된 goalId 추출 후 uiState에 저장 (재시도 시 재사용)
                    val goalId = createResult.data.toLong()
                    _uiState.update { it.copy(goalId = goalId.toInt()) }

                    // 1-1. 생성한 목표 ID로 수정
                    val updateGoalResult = updateGoalRequest(goalId)
                    if (updateGoalResult !is ApiResultV2.Success) {
                        moveToError(updateGoalResult)
                        return@launch
                    }

                    // 2. 문서 확인
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

                    // 3. 문서 업로드 (Presigned S3 + 서버 등록)
                    val uploadDocumentResult = uploadDocumentInternal(
                        goalId = goalId.toInt(),
                        uri = state.selectedFileUri,
                        fileName = state.selectedFileName,
                        mimeType = state.selectedFileMimeType
                    )
                    if (uploadDocumentResult !is ApiResultV2.Success) {
                        moveToError(uploadDocumentResult)
                        return@launch
                    }

                    // 4. documentId 조회 후 uiState에 저장
                    val fetchDocumentResult = fetchCompletedDocument(goalId.toInt())
                    if (fetchDocumentResult !is ApiResultV2.Success) {
                        moveToError(fetchDocumentResult)
                        return@launch
                    }

                    val documentId = _uiState.value.documentId.toLong()

                    // 5. SSE로 처리 상태 모니터링 (Success/Error 전환은 내부에서 처리)
                    connectDocumentSSE(goalId, documentId)
                    return@launch
                }

                GoalTypeUiState.CATEGORY -> {
                    // 1. 카테고리 목표 생성
                    val goalResult = createGoalRequestForCategory(state.selectedCategoryId)
                    if (goalResult !is ApiResultV2.Success) {
                        moveToError(goalResult)
                        return@launch
                    }

                    val goalId = goalResult.data.toLong()

                    // 1-1. 생성한 목표 ID로 수정
                    val updateGoalResult = updateGoalRequest(goalId)
                    if (updateGoalResult !is ApiResultV2.Success) {
                        moveToError(updateGoalResult)
                        return@launch
                    }
                }

                GoalTypeUiState.NONE -> {
                    moveToFrontError("목표 타입이 선택되지 않았습니다.")
                    return@launch
                }
            }

            // 🔹 최소 로딩 1.8초 보장
            val elapsed = System.currentTimeMillis() - startTime
            val remain = 1800L - elapsed
            if (remain > 0) delay(remain)

            emitGoalRefreshUseCase()
            _mainState.value = UiStateAddGoalScreenState.Success

        }
    }

    private suspend fun moveToError(result: ApiResultV2<*>) {
        if (result is ApiResultV2.SessionExpired){
            sessionManager.expireSession()
        }

        _mainState.value = UiStateAddGoalScreenState.Error(
            message = result.uiMessage
        )
    }

    private fun moveToFrontError(msg: String) {
        _mainState.value = UiStateAddGoalScreenState.Error(
            message = msg
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

    /**
     * 목표 수정 요청
     * - 기존 createGoalRequest() 구조와 동일
     */
    private suspend fun updateGoalRequest(
        goalId: Long
    ): ApiResultV2<Unit> {

        val state = _uiState.value

        val studyPeriodStr =
            state.studyPeriod?.toString()?.plus("주") ?: "기간 설정 안함"

        val request = UpdateGoalRequest(
            studyPeriod = studyPeriodStr,
            difficulty = state.difficulty,
            prompt = state.promptInput.takeIf { it.isNotBlank() },
        )

        return updateGoalUseCase(
            goalId = goalId,
        )
    }


    private suspend fun uploadDocumentInternal(
        goalId: Int,
        uri: Uri,
        fileName: String,
        mimeType: String
    ): ApiResultV2<Unit> {

        // 📊 ONB-PDF-1: pdf_upload_start 이벤트 + pdf_upload_attempt_count User Property
        val fileSizeKb = _uiState.value.selectedFileSize / 1024
        val pageCount = getPdfPageCountUseCase(uri).getOrDefault(0)
        analyticsLogger.logPdfUploadStart(fileSizeKb = fileSizeKb, pageCount = pageCount)

        return when (
            val result = uploadDocumentUseCase(
                goalId = goalId,
                uri = uri,
                fileName = fileName,
                mimeType = mimeType
            )
        ) {

            is ApiResultV2.Success -> {
                // ✅ 성공 시 필요한 상태 변경이 있다면 여기서
                _uiState.update {
                    it.copy(
                        // todo. documentID 넘어올시 저장
                    )
                }
                result
            }

            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
                result
            }

            is ApiResultV2.ServerError -> result
            is ApiResultV2.NetworkError -> result
            is ApiResultV2.UnknownError -> result
        }
    }
    private suspend fun fetchCompletedDocument(goalId: Int): ApiResultV2<Unit> {

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
                PrefsUtil.saveDocumentId(context = appContext, documentId)
                // ✅ 성공 시 UiState에 documentId 저장
                _uiState.update {
                    it.copy(documentId = documentId)
                }

                ApiResultV2.Success(
                    message = result.message,
                    data = Unit
                )
            }

            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
                result
            }

            is ApiResultV2.ServerError -> result
            is ApiResultV2.NetworkError -> result
            is ApiResultV2.UnknownError -> result
        }
    }

    private suspend fun connectDocumentSSE(goalId: Long, documentId: Long) {
        sseInitialRemainMs = 0L
        _uiState.update { it.copy(isSseStarted = false, sseProgress = 0f, sseStatusText = null) }

        streamDocumentProcessingUseCase(goalId, documentId)
            .catch { e ->
                Log.e("SSE_LIFECYCLE", "Flow 예외 전파: ${e.javaClass.simpleName}(${e.message})")
                _mainState.value = UiStateAddGoalScreenState.Error(
                    e.message ?: "알 수 없는 오류가 발생했습니다."
                )
            }
            .collect { event ->
            when (event) {
                is DocumentProcessingEvent.Connected -> {
                    _uiState.update { it.copy(isSseStarted = true) }
                }
                is DocumentProcessingEvent.Pending -> {
                    _uiState.update { it.copy(isSseStarted = true, sseProgress = 0.05f, sseRemainMs = null) }
                }
                is DocumentProcessingEvent.Processing -> {
                    val remainMs = event.remainMs
                    if (sseInitialRemainMs == 0L && remainMs > 0L) {
                        sseInitialRemainMs = remainMs
                    }
                    val progress = when {
                        remainMs <= 0L -> 0.99f
                        sseInitialRemainMs > 0L ->
                            (0.05f + (1f - remainMs.toFloat() / sseInitialRemainMs) * 0.94f)
                                .coerceIn(0.05f, 0.99f)
                        else -> 0.05f
                    }
                    val statusText = if (remainMs <= 0L) "잠시만 기다려주세요"
                        else "${(remainMs + 999L) / 1000L}초 남았어요."
                    val progressText = if (remainMs > 0L) "${(progress * 100).toInt()}% 완료" else null
                    _uiState.update { it.copy(sseProgress = progress, sseRemainMs = remainMs, sseStatusText = statusText, sseProgressText = progressText) }
                }
                is DocumentProcessingEvent.Completed -> {
                    Log.d("SSE_LIFECYCLE", "OCR 처리 완료 → SSE 연결 정상 종료")
                    _uiState.update { it.copy(sseProgress = 1.0f, sseRemainMs = 0L, sseStatusText = null) }
                    delay(600L)
                    emitGoalRefreshUseCase()
                    _mainState.value = UiStateAddGoalScreenState.Success
                }
                is DocumentProcessingEvent.Failed -> {
                    _mainState.value = when (event.reason) {
                        DocumentProcessingEvent.FailureReason.TIMEOUT ->
                            UiStateAddGoalScreenState.SseTimeout
                        DocumentProcessingEvent.FailureReason.SERVER_ERROR ->
                            UiStateAddGoalScreenState.SseServerError
                        DocumentProcessingEvent.FailureReason.ENCRYPTED_FILE ->
                            UiStateAddGoalScreenState.SseEncryptedFile
                        DocumentProcessingEvent.FailureReason.UNKNOWN ->
                            UiStateAddGoalScreenState.Error("알 수 없는 오류가 발생했습니다.")
                    }
                }
                is DocumentProcessingEvent.StreamError -> {
                    _mainState.value = UiStateAddGoalScreenState.Error(
                        event.throwable.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun retryDocumentSSE() {
        viewModelScope.launch {
            sseInitialRemainMs = 0L
            _uiState.update { it.copy(isSseStarted = false, sseProgress = 0f, sseRemainMs = null, sseStatusText = null) }
            _mainState.value = UiStateAddGoalScreenState.Loading

            val goalId = _uiState.value.goalId.toLong()
            val documentId = _uiState.value.documentId.toLong()

            connectDocumentSSE(goalId, documentId)
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

    fun nextPage() {
        _uiState.update { it.copy(currentPage = it.currentPage + 1) }
        /*_uiState.update { currentState ->
            val nextScreen = AddGoalFlow.next(currentState.currentScreen, currentState.goalTypeUiState)

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
        }*/
    }

    fun prevPage() {
        _uiState.update {
            val prev = (it.currentPage - 1).coerceAtLeast(1)
            it.copy(currentPage = prev)
        }
        /*_uiState.update { currentState ->
            val prevScreen = AddGoalFlow.prev(currentState.currentScreen, currentState.goalTypeUiState)

            if (prevScreen != null && currentState.currentPage > 1) {
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
        }*/
    }

}
