package com.dabenxiang.mimi.view.post.article

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.PostMemberRequest
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PostArticleViewModel: BaseViewModel() {

    companion object {
        private const val TYPE_TEXT = 1
    }

    private val _postArticleResult = MutableLiveData<ApiResult<Long>>()
    val postArticleResult: LiveData<ApiResult<Long>> = _postArticleResult

    fun postArticle(title: String, content: String, tags: ArrayList<String>) {
        viewModelScope.launch {
            flow {
                val request = PostMemberRequest(
                    title = title,
                    content = content,
                    type = TYPE_TEXT,
                    tags = tags
                )

                val resp = domainManager.getApiRepository().postMembersPost(request)
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
}