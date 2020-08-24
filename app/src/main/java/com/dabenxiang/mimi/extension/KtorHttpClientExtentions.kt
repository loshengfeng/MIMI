package com.dabenxiang.mimi.extension

import com.dabenxiang.mimi.model.api.vo.DownloadResult
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.io.readUTF8Line
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.URI
import kotlin.math.roundToInt

suspend fun HttpClient.downloadFile(url: String): Flow<DownloadResult> {
    return flow {
        try {
            val REMOVE_HEADER_COUNT: Int = 2
            val response = call {
                url(url)
                method = HttpMethod.Get
            }.response

            val data = ByteArray(
                when(response.contentLength() != null) {
                    true -> response.contentLength()!!.toInt()
                    else -> {
                        256 * 256
                    }
            })

            var offset = 0
            var count = 0
            val tsPattern = ".ts"
            val indexPattern = "index.m3u8"
            val ENCRYPTION_KEY = "encryption.key"

//            val d = arrayListOf<String>()
//
//            while (true) {
//                val info = response.content.readUTF8Line()
//                Timber.d("1111 ${info.toString()}")
//                if(!info.isNullOrEmpty()) {
//                    d.add(info.toString())
//                } else break
//            }
//
//            Timber.d("$d")

            do {
                val currentRead = response.content.readAvailable(data, offset, data.size)
                offset += currentRead
                val progress = (offset * 100f / data.size).roundToInt()
                emit(DownloadResult.Progress(progress))
                if(offset == data.size) break
            } while (currentRead > 0)

            response.close()

            if (response.status.isSuccess()) {
                val file = File.createTempFile("dbx", ".m3u8")
                val outputStream = file.outputStream()
                var redirector = false
                withContext(Dispatchers.IO) {
                    data.inputStream().bufferedReader().useLines {lines ->
                        val domainUrl = url.replace(indexPattern, "")
                        lines.forEach {
                            if(count <= REMOVE_HEADER_COUNT) {
                                if(redirector) emit(DownloadResult.Redirect(url.replace(indexPattern, it)))
                                when {
                                    it.contains("#EXT-X-STREAM-INF") -> {
                                        file.delete()
                                        redirector = true
                                    }
                                    it.contains("EXT-X-MEDIA-SEQUENCE") -> {
                                        val splitMediaPattern = it.split(":").last()
                                        val newMediaSequence = it.replace(splitMediaPattern, (splitMediaPattern.toInt() + 2).toString())
                                        outputStream.write(newMediaSequence.plus("\n").toByteArray())
                                        return@forEach
                                    }
                                    it.contains(ENCRYPTION_KEY) -> {
                                        val newKey = it.replace(ENCRYPTION_KEY, domainUrl.plus(ENCRYPTION_KEY))
                                        outputStream.write(newKey.plus("\n").toByteArray())
                                        return@forEach
                                    }
                                    it.contains("#EXTINF") -> {
                                        count++
                                        return@forEach
                                    }
                                    it.contains(tsPattern) -> {
                                        // count == REMOVE_HEADER_COUNT need +1, avoid the tag #EXTINF lost
                                        if(count == REMOVE_HEADER_COUNT) count++
                                        return@forEach
                                    }
                                }
                            }
                            when {
                                it.contains(tsPattern) -> {
                                    when(URI(it).scheme) {
                                        "http",
                                        "https" -> {
                                            outputStream.write(it.plus("\n").toByteArray())
                                        }
                                        else ->
                                            outputStream.write(domainUrl.plus(it.plus("\n")).toByteArray())
                                    }
                                }
                                else ->
                                    outputStream.write(it.plus("\n").toByteArray())
                            }
                        }
                    }
                }
                emit(DownloadResult.Success(file.absolutePath))
            } else emit(DownloadResult.Error("download failed"))
        } catch (e: TimeoutCancellationException) {
            emit(DownloadResult.Error("connection time out", e))
        } catch (e: Exception) {
            Timber.d("${e.printStackTrace()}")
            emit(DownloadResult.Success(url))
//            emit(DownloadResult.Error("connection time out", e))
        }
    }
}