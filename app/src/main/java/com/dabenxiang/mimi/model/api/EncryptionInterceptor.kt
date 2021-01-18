package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.widget.utility.CryptUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.koin.core.component.KoinComponent
import timber.log.Timber

class EncryptionInterceptor() : Interceptor, KoinComponent {

    override fun intercept(chain: Interceptor.Chain): Response  {

        var response: Response? = null
        val request = chain.request()
        val url = request.url

        val oldBody = request.body
        val newRequest = oldBody?.let {
            val buffer = Buffer()
            oldBody.writeTo(buffer)
            val strOldBody: String = buffer.readUtf8()
            Timber.i("Encryption intercept: strOldBody:$strOldBody")
            val mediaType: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
            Timber.i("Encryption intercept: mediaType:$mediaType")
            val encryptBodyStr: String = CryptUtils.cEcbEncrypt(strOldBody) ?: ""
            Timber.i("Encryption intercept: encryptBodyStr:$encryptBodyStr")
            encryptBodyStr.toRequestBody(mediaType)
        }?.let {
            Timber.i("Encryption intercept: newBody:$it")
            buildRequest(request, it)
        }

        Timber.i("Encryption intercept: newRequest:$newRequest")

        try {
            if(newRequest == null ) {
                throw Exception("Request is null")
            }
            response = chain.proceed(newRequest)
            Timber.d("Response Code: ${response.code}")

            if(response.isSuccessful){
                val newResponse = response.newBuilder()
                var contentType = response.header("Content-Type")  ?:  "application/json"

                val responseBodyStr = response.body!!.string()
                val decrypted = CryptUtils.cEcbDecrypt(responseBodyStr)
                Timber.i("Encryption intercept: decrypted:$decrypted")
                if (decrypted.isNullOrEmpty()) {
                    throw IllegalArgumentException("No decryption strategy!")
                }

                decrypted.toRequestBody(contentType!!.toMediaTypeOrNull())
                return newResponse.build()
            }
            return response

        } catch (e: Exception) {
            response?.close()
            return throw IllegalArgumentException(e)
        }
    }

    private fun buildRequest(request: Request, body: RequestBody): Request {
        return request.newBuilder()
                .header("Content-Type", body.contentType().toString())
                .header("Content-Length", body.contentLength().toString())
                .method(request.method, body).build()
    }

}
