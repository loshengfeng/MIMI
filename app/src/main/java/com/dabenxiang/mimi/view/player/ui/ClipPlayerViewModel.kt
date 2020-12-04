package com.dabenxiang.mimi.view.player.ui

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class ClipPlayerViewModel : BaseViewModel() {

    private val _memberPostContentSource = MutableLiveData<ApiResult<MemberPostItem>>()
    val memberPostContentSource: LiveData<ApiResult<MemberPostItem>> = _memberPostContentSource

    private val _videoStreamingUrl = MutableLiveData<String>()
    val videoStreamingUrl: LiveData<String> = _videoStreamingUrl

    var videoContentId: Long = -1
    var m3u8SourceUrl: String = ""

    private var _attachmentResult = MutableLiveData<ApiResult<String>>()
    val attachmentResult: LiveData<ApiResult<String>> = _attachmentResult

    fun getPostDetail() {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMemberPostDetail(videoContentId)
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    Timber.d(e)
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _memberPostContentSource.value = it
                }
        }
    }

    fun parsingM3u8Source(context: Context, item: MediaContentItem) {
        val clipUrl = item.shortVideo?.url
        val clipId = item.shortVideo?.id
        if (!TextUtils.isEmpty(clipUrl)) {
            _videoStreamingUrl.value = item.shortVideo?.url
        } else if (!TextUtils.isEmpty(clipId)) {
            getAttachment(context, clipId!!)
        }
    }

    fun getAttachment(context: Context, id: String) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                var fileName = result.headers()["Content-Disposition"]
                    ?.substringAfter("UTF-8''")
                    .toString()
                val extension = fileName.split(".").last()
                if (extension.isEmpty() || byteArray == null) throw Exception("File name or array error")

                fileName = "tmp_clip.$extension"

                val path = "${FileUtil.getVideoFolderPath(context)}$fileName"
                File(path).delete()
                if (!File(path).exists()) {
                    convertByteToVideo(context, byteArray, fileName)
                }
                Timber.d("getAttachment($id) filePath=$path")
                emit(ApiResult.success(path))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _attachmentResult.value = it }
        }
    }

    private fun convertByteToVideo(
        context: Context,
        streamArray: ByteArray,
        fileName: String
    ): String {
        val path = getVideoPath(context, fileName)
        val out = FileOutputStream(path)
        out.write(streamArray)
        out.close()
        return path
    }

    fun getVideoPath(context: Context, fileName: String, ext: String = ""): String {
        return "${FileUtil.getVideoFolderPath(context)}$fileName$ext"
    }
}