package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.PROJECT_NAME
import com.dabenxiang.mimi.model.api.vo.LogApiItem
import com.dabenxiang.mimi.model.pref.Pref
import com.google.gson.Gson
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import org.koin.core.KoinComponent
import org.koin.core.inject
import tw.gov.president.manager.submanager.logmoniter.di.SendLogManager
import java.io.EOFException
import java.nio.charset.Charset

class ApiLogInterceptor : Interceptor, KoinComponent {

    companion object {
        private const val PATH_SEND_LOG = "publicLog/v2/applog"
        private const val PATH_UPLOAD_IMAGE = "v1/Attachments"
    }

    private val pref: Pref by inject()

    private val utf8 = Charset.forName("UTF-8")

    private fun push(data: LogApiItem) {
        SendLogManager.v(PROJECT_NAME, "", "ab_test_2", Gson().toJson(data))
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val logData = LogApiItem()

        logData.userId = "${pref.profileItem.userId}"

        val request = chain.request()
        val requestBody = request.body
        val hasRequestBody = requestBody != null

        val path = request.url.encodedPath

        if (path.contains(PATH_SEND_LOG)) {
            return chain.proceed(request)
        }

        logData.method = request.method
        logData.url = request.url.toString()

        val requestHeaderNames = request.headers.names()
            .filter { it == ApiRepository.AUTHORIZATION || it == ApiRepository.X_DEVICE_ID }
        val requestHeaders = arrayListOf<String>()
        requestHeaderNames.forEach { name ->
            requestHeaders.add("$name: ${request.headers[name]}")
        }
        logData.requestHeaders = requestHeaders

        if (hasRequestBody && path.contains(PATH_UPLOAD_IMAGE)) {
            logData.requestBody = "base64 image"
        } else if (hasRequestBody && !bodyHasUnknownEncoding(request.headers)) {
            val buffer = Buffer()
            requestBody!!.writeTo(buffer)
            var charset = utf8
            val contentType = requestBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(utf8)
            }
            if (isPlaintext(buffer)) {
                logData.requestBody = buffer.readString(charset).replace(Regex("[\\s]"), "")
            }
        }

        val response: Response
        response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            logData.exception = e.toString()
            throw e
        }
        val responseBody = response.body
        val contentLength = responseBody!!.contentLength()
        logData.responseCode =
            "code: ${response.code}${if (response.message.isEmpty()) "" else " " + response.message}"

        if (response.body != null && !bodyHasUnknownEncoding(response.headers)) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer
            var charset = utf8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(utf8)
            }
            if (!isPlaintext(buffer)) {
                logData.responseBody = "binary ${buffer.size}-byte body omitted"
                return response
            }
            if (contentLength != 0L) {
                logData.responseBody = buffer.clone().readString(charset)
            }
        }
        push(logData)
        return response
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private fun isPlaintext(buffer: Buffer): Boolean {
        return try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            true
        } catch (e: EOFException) {
            false // Truncated UTF-8 sequence.
        }
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return (contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
                && !contentEncoding.equals("gzip", ignoreCase = true))
    }
}
