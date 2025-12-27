package com.teumteumeat.teumteumeat.data.repository.document

import android.content.Context
import android.net.Uri
import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.document.DocumentApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResult // 현재 반환 타입이 ApiResult로 되어 있어 추가
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2 // safeApiVer2가 반환하는 타입
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_request.PresignedRequest
import com.teumteumeat.teumteumeat.data.network.model_request.RegisterDocumentRequest
import com.teumteumeat.teumteumeat.data.network.model_response.PresignedResponse
import com.teumteumeat.teumteumeat.data.repository.BaseRepository // safeApiVer2 사용을 위해 필요
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class DocumentRepositoryImpl @Inject constructor(
    private val documentApiService: DocumentApiService,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : BaseRepository(authApiService, tokenLocalDataSource), DocumentRepository{

    override suspend fun issuePresignedUrl(
        fileName: String
    ): ApiResultV2<PresignedResponse> {

        return safeApiVer2<PresignedResponse, PresignedResponse>(
            apiCall = {
                documentApiService.issuePresignedUrl(
                    PresignedRequest(fileName = fileName)
                )
            },
            // 🔹 수정된 부분: 데이터가 null이면 예외를 던져서 safeApiVer2가 에러로 처리하게 함
            mapper = { it ?: error("서버로부터 Presigned 정보를 받지 못했습니다.") }

        )
    }

    override suspend fun uploadFileToS3(
        presignedUrl: String,
        uri: Uri,
        mimeType: String
    ): Result<Unit> = runCatching {

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("파일을 열 수 없습니다.")

        val requestBody = inputStream.readBytes()
            .toRequestBody(mimeType.toMediaType())

        val request = Request.Builder()
            .url(presignedUrl)
            .put(requestBody)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("S3 업로드 실패")
            }
        }
    }

    override suspend fun registerDocument(
        goalId: Long,
        fileName: String,
        fileKey: String
    ): ApiResultV2<Unit> {

        return safeApiVer2(
            apiCall = {
                documentApiService.registerDocument(
                    goalId = goalId,
                    request = RegisterDocumentRequest(
                        fileName = fileName,
                        fileKey = fileKey
                    )
                )
            },
            // 🔹 서버 data가 의미 없으므로 Unit으로 매핑
            mapper = { Unit }
        )
    }
}
