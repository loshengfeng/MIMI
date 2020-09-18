package com.dabenxiang.mimi.extension

import com.dabenxiang.mimi.model.api.vo.DownloadResult
import com.dabenxiang.mimi.model.api.vo.DownloadResult.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.URI

suspend infix fun HttpClient.downloadFile(url: String): Flow<DownloadResult> {
    return flow {
        try {
            val REMOVE_HEADER_COUNT = 2
            val response = call {
                url(url)
                method = HttpMethod.Get
            }.response

            var count = 0
            val tsPattern = ".ts"
            val indexPattern = "index.m3u8"
            val ENCRYPTION_KEY = "encryption.key"
            val mp4Pattern = ".mp4/"

            val data = response.readBytes()

            response.close()

            if (response.status.isSuccess()) {
                val file = File.createTempFile("dbx", ".m3u8")
                val outputStream = file.outputStream()
                var redirector = false
                withContext(Dispatchers.IO) {
                    data.inputStream().bufferedReader().useLines { lines ->
                        var domainUrl = url.replace(indexPattern, "")
                        domainUrl = domainUrl.split(mp4Pattern)[0].plus(mp4Pattern)
                        lines.forEach {
                            if (count <= REMOVE_HEADER_COUNT) {
                                if (redirector) emit(Redirect(url.replace(indexPattern, it)))
                                when {
                                    it.contains("#EXT-X-STREAM-INF") -> {
                                        file.delete()
                                        redirector = true
                                    }
                                    it.contains("EXT-X-MEDIA-SEQUENCE") -> {
                                        val splitMediaPattern = it.split(":").last()
                                        val newMediaSequence = it.replace(
                                            splitMediaPattern,
                                            (splitMediaPattern.toInt() + 2).toString()
                                        )
                                        outputStream.write(
                                            newMediaSequence.plus("\n").toByteArray()
                                        )
                                        return@forEach
                                    }
                                    it.contains(ENCRYPTION_KEY) -> {
                                        val newKey = it.replace(
                                            ENCRYPTION_KEY,
                                            domainUrl.plus(ENCRYPTION_KEY)
                                        )
                                        outputStream.write(newKey.plus("\n").toByteArray())
                                        return@forEach
                                    }
                                    it.contains("#EXTINF") -> {
                                        count++
                                        return@forEach
                                    }
                                    it.contains(tsPattern) -> {
                                        // count == REMOVE_HEADER_COUNT need +1, avoid the tag #EXTINF lost
                                        if (count == REMOVE_HEADER_COUNT) count++
                                        return@forEach
                                    }
                                }
                            }
                            when {
                                it.contains(tsPattern) -> {
                                    when (URI(it).scheme) {
                                        "http",
                                        "https" -> {
                                            outputStream.write(it.plus("\n").toByteArray())
                                        }
                                        else -> outputStream.write(
                                            domainUrl.plus(it.plus("\n")).toByteArray()
                                        )
                                    }
                                }
                                else -> outputStream.write(it.plus("\n").toByteArray())
                            }
                        }
                    }
                }
                emit(Success(file.absolutePath))
            } else emit(Error("download failed"))
        } catch (e: TimeoutCancellationException) {
            emit(Error("connection time out", e))
        } catch (e: Exception) {
            Timber.d("${e.printStackTrace()}")
            emit(Success(url))
//            emit(DownloadResult.Error("connection time out", e))
        }
    }
}