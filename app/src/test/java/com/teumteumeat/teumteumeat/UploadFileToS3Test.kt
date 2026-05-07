package com.teumteumeat.teumteumeat

import android.content.ContentResolver
import android.content.Context
import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.pdf_document.PdfDocumentRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * uploadFileToS3(ByteArray) 검증 테스트
 *
 * 검증 목표:
 *  1. Content-Type 헤더가 mimeType 과 일치하는지
 *  2. Content-Length 가 bytes.size 와 일치하는지 (-1 이면 Chunked → 서명 불일치 위험)
 *  3. 실제 전송 바이트가 입력 bytes 와 동일한지 (데이터 손실/변형 없음)
 *  4. HTTP 메서드가 PUT 인지
 *  5. S3 가 200 을 반환하면 Result.isSuccess, 실패 코드면 Result.isFailure
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE, application = TestApplication::class)
class UploadFileToS3Test {

    private lateinit var repository: PdfDocumentRepositoryImpl
    private lateinit var mockOkHttpClient: OkHttpClient
    private lateinit var capturedRequest: Request

    private val presignedUrl = "https://bucket.s3.amazonaws.com/test-key?X-Amz-Signature=abc"
    private val mimeType = "application/pdf"

    @Before
    fun setUp() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockContentResolver = mockk<ContentResolver>(relaxed = true)
        every { mockContext.contentResolver } returns mockContentResolver

        mockOkHttpClient = mockk()
        repository = PdfDocumentRepositoryImpl(
            documentApiService = mockk(relaxed = true),
            okHttpClient = mockOkHttpClient,
            context = mockContext,
            authApiService = mockk<AuthApiService>(relaxed = true),
            tokenLocalDataSource = mockk<TokenLocalDataSource>(relaxed = true),
        )
    }

    /** OkHttpClient 가 지정 응답을 반환하도록 설정하고 캡처된 Request 를 반환 */
    private fun mockS3Response(responseCode: Int): Request {
        val requestSlot = slot<Request>()
        val mockCall = mockk<Call>()
        every { mockOkHttpClient.newCall(capture(requestSlot)) } returns mockCall
        every { mockCall.execute() } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(responseCode)
                .message(if (responseCode == 200) "OK" else "Forbidden")
                .body("".toResponseBody(null))
                .build()
        }
        return requestSlot.captured  // slot 은 execute 후 채워지므로 호출 후 접근
    }

    // ── 1. HTTP 메서드 검증 ────────────────────────────────────────────────

    @Test
    fun `PUT 메서드로 요청하는지 검증`() = runTest {
        val bytes = "dummy pdf content".toByteArray()
        val requestSlot = slot<Request>()
        val mockCall = mockk<Call>()
        every { mockOkHttpClient.newCall(capture(requestSlot)) } returns mockCall
        every { mockCall.execute() } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200).message("OK")
                .body("".toResponseBody(null))
                .build()
        }

        repository.uploadFileToS3(presignedUrl, bytes, mimeType)

        assertEquals("PUT", requestSlot.captured.method)
    }

    // ── 2. Content-Type 헤더 검증 ─────────────────────────────────────────

    @Test
    fun `Content-Type 헤더가 mimeType 과 일치하는지 검증`() = runTest {
        val bytes = "dummy pdf content".toByteArray()
        val requestSlot = slot<Request>()
        val mockCall = mockk<Call>()
        every { mockOkHttpClient.newCall(capture(requestSlot)) } returns mockCall
        every { mockCall.execute() } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200).message("OK")
                .body("".toResponseBody(null))
                .build()
        }

        repository.uploadFileToS3(presignedUrl, bytes, mimeType)

        val contentType = requestSlot.captured.body?.contentType().toString()
        assertTrue(
            "Content-Type 에 application/pdf 가 포함되어야 함. 실제값: $contentType",
            contentType.contains("application/pdf")
        )
    }

    // ── 3. Content-Length 검증 (핵심: -1 이면 Chunked → Presigned 서명 불일치) ──

    @Test
    fun `Content-Length 가 bytes size 와 일치하는지 검증 — Chunked 전송 방지`() = runTest {
        val bytes = ByteArray(681_210) { it.toByte() }  // 실제 테스트 케이스와 동일한 크기
        val requestSlot = slot<Request>()
        val mockCall = mockk<Call>()
        every { mockOkHttpClient.newCall(capture(requestSlot)) } returns mockCall
        every { mockCall.execute() } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200).message("OK")
                .body("".toResponseBody(null))
                .build()
        }

        repository.uploadFileToS3(presignedUrl, bytes, mimeType)

        val contentLength = requestSlot.captured.body?.contentLength() ?: -1L
        assertNotEquals(
            "Content-Length 가 -1 이면 Chunked 전송 발생 → Presigned URL 서명 불일치 위험",
            -1L, contentLength
        )
        assertEquals(
            "Content-Length 가 bytes.size 와 일치해야 함",
            bytes.size.toLong(), contentLength
        )
    }

    // ── 4. 전송 바이트 무결성 검증 ────────────────────────────────────────

    @Test
    fun `전송 바이트가 입력 bytes 와 완전히 동일한지 검증 — 데이터 변형 없음`() = runTest {
        val originalBytes = "PDF binary content 1234567890".toByteArray()
        val requestSlot = slot<Request>()
        val mockCall = mockk<Call>()
        every { mockOkHttpClient.newCall(capture(requestSlot)) } returns mockCall
        every { mockCall.execute() } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200).message("OK")
                .body("".toResponseBody(null))
                .build()
        }

        repository.uploadFileToS3(presignedUrl, originalBytes, mimeType)

        val buffer = Buffer()
        requestSlot.captured.body?.writeTo(buffer)
        val actualBytes = buffer.readByteArray()

        assertArrayEquals(
            "전송 바이트가 원본과 달라지면 안 됨 (인코딩/변형 없음 확인)",
            originalBytes, actualBytes
        )
    }

    // ── 5. S3 200 → Result.isSuccess 검증 ────────────────────────────────

    @Test
    fun `S3 200 응답 시 Result isSuccess 반환`() = runTest {
        val bytes = "valid pdf".toByteArray()
        val requestSlot = slot<Request>()
        val mockCall = mockk<Call>()
        every { mockOkHttpClient.newCall(capture(requestSlot)) } returns mockCall
        every { mockCall.execute() } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200).message("OK")
                .body("".toResponseBody(null))
                .build()
        }

        val result = repository.uploadFileToS3(presignedUrl, bytes, mimeType)

        assertTrue("S3 200 OK 응답 시 Result.isSuccess 여야 함", result.isSuccess)
    }

    // ── 6. S3 403 → Result.isFailure 검증 (Presigned 서명 불일치 시뮬레이션) ──

    @Test
    fun `S3 403 응답 시 Result isFailure 반환 — Presigned 서명 불일치 시뮬레이션`() = runTest {
        val bytes = "mismatched size content".toByteArray()
        val requestSlot = slot<Request>()
        val mockCall = mockk<Call>()
        every { mockOkHttpClient.newCall(capture(requestSlot)) } returns mockCall
        every { mockCall.execute() } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(403).message("Forbidden")
                .body("".toResponseBody(null))
                .build()
        }

        val result = repository.uploadFileToS3(presignedUrl, bytes, mimeType)

        assertTrue("S3 403 응답 시 Result.isFailure 여야 함", result.isFailure)
    }
}