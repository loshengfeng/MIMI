package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.widget.utility.AESEncryptor
import com.dabenxiang.mimi.widget.utility.CryptUtils
import io.ktor.util.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.koin.core.component.KoinComponent
import timber.log.Timber

@InternalAPI
class EncryptionInterceptor() : Interceptor, KoinComponent {

    companion object{
        val exclusionList = arrayListOf(
            "business/ads",
            "business/statistics",
            "attachments"
        )

    }

    override fun intercept(chain: Interceptor.Chain): Response  {

        var response: Response? =null
        val request = chain.request()
        val domainCheck = request.url.let {url->
            val exclusion =exclusionList.filter {
                url.toString().toLowerCase().contains(it)
            }
            exclusion.isNotEmpty()
        }
        Timber.v("Encryption intercept: domainCheck:$domainCheck")
        val newRequest = if (domainCheck) request
        else {
            val oldBody = request.body
            oldBody?.let {
                val buffer = Buffer()
                oldBody.writeTo(buffer)
                val strOldBody: String = buffer.readUtf8()
                val mediaType: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
                val encryptBodyStr: String = CryptUtils.encrypt(strOldBody) ?: ""
                Timber.i("Encryption intercept: encryptBodyStr:$encryptBodyStr")
                encryptBodyStr.toRequestBody(mediaType)
            }?.let {
                buildRequest(request, it)
            } ?: request
        }

        try {
            response = chain.proceed(newRequest)

            if(domainCheck){
                return response
            }

            val newResponse = response.newBuilder()
            var contentType = "application/json; charset=utf-8"

            response.body!!.string().chunked(2).map {
                it.toInt(16).toByte()
            }.toByteArray().encodeBase64().takeIf{ it.isNotEmpty() }?.let { responseBodyStr->
                Timber.i("Encryption intercept: decryptBase64:${responseBodyStr}")
                val decrypted = AESEncryptor.decryptWithAES(strToDecrypt=responseBodyStr)
                Timber.i("Encryption intercept: decrypted:$decrypted")
                if (decrypted.isNullOrEmpty()) {
                    throw IllegalArgumentException("No decryption strategy!")
                }
                newResponse.body(decrypted.toResponseBody(contentType!!.toMediaTypeOrNull()))
                return newResponse.build()
            }

            return response


        } catch (e: Exception) {
            response?.close()
            Timber.d("Exception: $e ")
            throw e
        }
    }

    private fun buildRequest(request: Request, body: RequestBody): Request {
        return request.newBuilder()
                .header("Content-Type", body.contentType().toString())
                .header("Content-Length", body.contentLength().toString())
                .method(request.method, body).build()
    }

}
