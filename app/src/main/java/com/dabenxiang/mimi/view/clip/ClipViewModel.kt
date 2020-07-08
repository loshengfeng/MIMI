package com.dabenxiang.mimi.view.clip

import android.widget.ImageView
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class ClipViewModel: BaseViewModel() {

    fun getAttachment(view: ImageView, id: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)

                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                if (bitmap != null) {
                    lruCacheManager.putLruCache(id, bitmap)
                }
                emit(ApiResult.success(Pair(view, id)))
            }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { resp ->
                    when (resp) {
                        is ApiResult.Error -> Timber.e(resp.throwable)
                        is ApiResult.Success -> {
                        }
                    }
                }
        }
    }

}