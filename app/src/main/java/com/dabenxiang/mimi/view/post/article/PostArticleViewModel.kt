package com.dabenxiang.mimi.view.post.article

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostMemberRequest
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PostArticleViewModel: BaseViewModel() {

    private val _postArticleResult = MutableLiveData<ApiResult<Long>>()
    val postArticleResult: LiveData<ApiResult<Long>> = _postArticleResult

    private val _clubItemResult = MutableLiveData<ApiResult<ArrayList<MemberClubItem>>>()
    val clubItemResult: LiveData<ApiResult<ArrayList<MemberClubItem>>> = _clubItemResult

    private val _bitmapResult = MutableLiveData<ApiResult<String>>()
    val bitmapResult: LiveData<ApiResult<String>> = _bitmapResult

    fun updateArticle(title: String, content: String, tags: ArrayList<String>, item: MemberPostItem) {
        viewModelScope.launch {
            flow {
                val request = PostMemberRequest(
                    title = title,
                    content = content,
                    type = PostType.TEXT.value,
                    tags = tags
                )

                val resp = domainManager.getApiRepository().updatePost(item.id, request)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postArticleResult.value = it }
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

    fun getBitmap(id: String) {
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