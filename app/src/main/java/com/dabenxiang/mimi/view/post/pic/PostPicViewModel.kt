package com.dabenxiang.mimi.view.post.pic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PostPicViewModel: BaseViewModel() {

    private val _clubItemResult = MutableLiveData<ApiResult<ArrayList<MemberClubItem>>>()
    val clubItemResult: LiveData<ApiResult<ArrayList<MemberClubItem>>> = _clubItemResult

    private val _bitmapResult = MutableLiveData<ApiResult<String>>()
    val bitmapResult: LiveData<ApiResult<String>> = _bitmapResult

    fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                LruCacheUtils.putLruCache(id, bitmap)
                emit(ApiResult.success(id))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when(it) {
                        is ApiResult.Success -> {
                            update(it.result)
                        }
                    }
                }
        }
    }

    fun getClub(tag: String) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersClub(tag)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clubItemResult.value = it }
        }
    }

    fun getBitmapForClub(id: String) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                LruCacheUtils.putLruCache(id, bitmap)
                emit(ApiResult.success(id))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _bitmapResult.value = it }
        }
    }
}