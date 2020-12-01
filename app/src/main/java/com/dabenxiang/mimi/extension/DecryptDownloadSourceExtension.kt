package com.dabenxiang.mimi.extension

import com.dabenxiang.mimi.model.api.vo.DownloadResult
import com.dabenxiang.mimi.widget.utility.CryptUtils
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

suspend fun HttpClient.decryptSource(url: String, key: ByteArray): Flow<DownloadResult> {
    return flow {
        try {
            Timber.d("url !!! $url")
            val response = call {
                url(url)
                method = HttpMethod.Get
            }.response

            emit(DownloadResult.Success(CryptUtils.decryptWithCEBNoPadding(response.readBytes(), key)))
        } catch (e: Exception) {
            Timber.d("e ${e.printStackTrace()}")
            emit(DownloadResult.Success(""))
        }
    }
}