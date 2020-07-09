package com.dabenxiang.mimi.view.clip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileIOUtils
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.FileUtil
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File

class ClipViewModel: BaseViewModel() {

    private var _clipResult = MutableLiveData<ApiResult<Triple<Long, Int, File>>>()
    val clipResult: LiveData<ApiResult<Triple<Long, Int, File>>> = _clipResult

    fun getClip(id: Long, pos: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)

                val byteStream = result.body()?.byteStream()
                val filename = result.headers()["content-disposition"]
                    ?.split("; ")
                    ?.takeIf { it.size >= 2 }
                    ?.run { this[1].split("=") }
                    ?.takeIf { it.size >= 2 }
                    ?.run { this[1] }
                    ?: "$id.mov"
                val file = FileUtil.getClipFile(filename)
                FileIOUtils.writeFileFromIS(file, byteStream)

                emit(ApiResult.success(Triple(id, pos, file)))
            }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clipResult.value = it }
        }
    }

}