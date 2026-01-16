package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_request.UpdateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GoalsData
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toServerTime
import com.teumteumeat.teumteumeat.domain.usecase.GetGoalListUseCase
import com.teumteumeat.teumteumeat.domain.usecase.document.GetDocumentsUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.CreateGoalUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.CreateGoalUseCaseV1
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.GetCategoriesUseCase
import com.teumteumeat.teumteumeat.domain.usecase.document.IssuePresignedUrlUseCase
import com.teumteumeat.teumteumeat.domain.usecase.document.UploadDocumentUseCase
import com.teumteumeat.teumteumeat.domain.usecase.goal.UpdateGoalUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.UpdateCommuteTimeUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.RegisterUserNameUseCase
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.BottomSheetType
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OnBoardingFlow
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.PromptViolation
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.UiStateOnboardingState
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.UiStateOnboardingScreenState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.utils.Utils.PrefsUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.collections.forEach

@HiltViewModel
class AddGoalViewModel @Inject constructor(
    val updateCommuteTimeUseCase: UpdateCommuteTimeUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    private val getGoalListUseCase: GetGoalListUseCase,
    val uploadDocumentUseCase: UploadDocumentUseCase,
    val getDocumentsUseCase: GetDocumentsUseCase,
    application: Application,
) : ViewModel() {
    private val appContext = application.applicationContext

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
            _uiState.update{
                it.copy(
                    goalTypeUiState = GoalTypeUiState.valueOf(type.name)
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


    fun onFileDeleted(
    ) {
        _uiState.update {
            it.copy(
                selectedFileUri = null,
                selectedFileName = ""
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
            PrefsUtil.saveGoalType(appContext, state.goalTypeUiState)
            when(state.goalTypeUiState){
                GoalTypeUiState.DOCUMENT -> {
                    // 1. 목표 생성
                    val createResult = createGoalRequest()
                    if (createResult !is ApiResultV2.Success) {
                        moveToError(createResult)
                        return@launch
                    }

                    // ✅ 생성된 goalId 추출
                    val goalId = createResult.data.toLong()

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

                    // 3. 문서 업로드
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

                    // 4. 문서 등록
                    val fetchDocumentResult = fetchCompletedDocument(goalId.toInt())
                    if (fetchDocumentResult !is ApiResultV2.Success) {
                        moveToError(uploadDocumentResult)
                        return@launch
                    }
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
                }
            }

            // 🔹 최소 로딩 1.8초 보장
            val elapsed = System.currentTimeMillis() - startTime
            val remain = 1800L - elapsed
            if (remain > 0) delay(remain)


            _mainState.value = UiStateAddGoalScreenState.Success

            // todo. 테스트 코드!
//            _mainState.value = UiStateOnBoardingMainState.Error(
//                message = "테스트 에러 페이지입니다.\n잠시 후 다시 시도해주세요."
//            )
        }
    }

    private fun moveToError(result: ApiResultV2<*>) {
        _mainState.value = UiStateAddGoalScreenState.Error(
            message = result.uiMessage
        )
    }

    private fun moveToFrontError(msg: String) {
        _mainState.value = UiStateAddGoalScreenState.Error(
            message = msg
        )
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
            request = request
        )
    }

    private suspend fun fetchLatestGoalId(): ApiResultV2<Unit> {
        return when (val result = getGoalListUseCase()) {

            is ApiResultV2.Success -> {
                val data : GoalsData = result.data
                val list: List<GetGoalResponse> = data.goalResponses

                Log.d("OnBoardingVM", "goal list size = ${list.size}")

                val latestGoalId = list[0].goalId
                    ?: return ApiResultV2.ServerError(
                        code = "EMPTY_GOAL_LIST",
                        message = "목표가 존재하지 않습니다.",
                        errorType = DomainError.Message("goal list is empty")
                    )

                Log.d("DEBUG", "state.goalId(before save) = ${latestGoalId}")

                PrefsUtil.saveGoalId(appContext, latestGoalId)

                Log.d("DEBUG", "saved goalId = ${PrefsUtil.getGoalId(appContext)}")


                // ⭐ 성공 시 내부에서 상태 반영
                _uiState.update {
                    it.copy(goalId = latestGoalId)
                }

                Log.d("OnBoardingVM", "최신 목표 ID: ${_uiState.value.goalId}")

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
    private suspend fun uploadDocumentInternal(
        goalId: Int,
        uri: Uri,
        fileName: String,
        mimeType: String
    ): ApiResultV2<Unit> {

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

            is ApiResultV2.ServerError -> result
            is ApiResultV2.NetworkError -> result
            is ApiResultV2.SessionExpired -> result
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

            is ApiResultV2.ServerError -> result
            is ApiResultV2.NetworkError -> result
            is ApiResultV2.SessionExpired -> result
            is ApiResultV2.UnknownError -> result
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
        _uiState.update { currentState ->
            val nextScreen = AddGoalFlow.next(currentState.currentScreen)

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
            val prevScreen = AddGoalFlow.prev(currentState.currentScreen)

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

}
