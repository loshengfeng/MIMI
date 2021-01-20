package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.widget.utility.CryptUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.koin.core.component.KoinComponent
import timber.log.Timber
import kotlin.math.ceil

class EncryptionInterceptor() : Interceptor, KoinComponent {

    companion object{
        val exclusionList = arrayListOf(
            "business/ads",
            "business/statistics",
            "attachments"
        )

    }

    override fun intercept(chain: Interceptor.Chain): Response  {

        var response: Response? = null
        val request = chain.request()
        val domainCheck = request.url.let {url->
            val exclusion =exclusionList.filter {
                url.toString().toLowerCase().contains(it)
            }
            Timber.i("Encryption intercept: url:$url")
            exclusion.isNotEmpty()
        }
        Timber.i("Encryption intercept: domainCheck:$domainCheck")
        val newRequest = if (domainCheck) request
        else {
            val oldBody = request.body
            oldBody?.let {
                val buffer = Buffer()
                oldBody.writeTo(buffer)
                val strOldBody: String = buffer.readUtf8()
                Timber.i("Encryption intercept: strOldBody:$strOldBody")
                val mediaType: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
                Timber.i("Encryption intercept: mediaType:$mediaType")
                val encryptBodyStr: String = CryptUtils.encrypt(strOldBody) ?: ""
                Timber.i("Encryption intercept: encryptBodyStr:$encryptBodyStr")
                Timber.i(
                    "Encryption intercept: decryptBodyStr test:${
                        CryptUtils.decrypt(
                            encryptBodyStr
                        )
                    }"
                )
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

            val responseBodyStr = response.body!!.string()
            val decrypted = CryptUtils.decrypt(responseBodyStr)
            Timber.i("Encryption intercept: decrypted:$decrypted")
            if (decrypted.isNullOrEmpty()) {
                throw IllegalArgumentException("No decryption strategy!")
            }
            newResponse.body(decrypted.toResponseBody(contentType!!.toMediaTypeOrNull()))
            Timber.i("Encryption intercept: newResponse:$newResponse")
            return newResponse.build()

        } catch (e: Exception) {
            response?.close()
            Timber.d("Exception: $e ")
            return chain.proceed(newRequest)
        }
    }

    private fun buildRequest(request: Request, body: RequestBody): Request {
        return request.newBuilder()
                .header("Content-Type", body.contentType().toString())
                .header("Content-Length", body.contentLength().toString())
                .method(request.method, body).build()
    }

}
