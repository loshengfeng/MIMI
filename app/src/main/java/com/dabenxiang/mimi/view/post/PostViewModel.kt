package com.dabenxiang.mimi.view.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ApiBaseItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class PostViewModel: BaseViewModel() {

    private val _clubItemResult = MutableLiveData<ApiResult<ArrayList<MemberClubItem>>>()
    val clubItemResult: LiveData<ApiResult<ArrayList<MemberClubItem>>> = _clubItemResult

    private var _postDetailResult = MutableLiveData<ApiResult<ApiBaseItem<MemberPostItem>>>()
    val postDetailResult: LiveData<ApiResult<ApiBaseItem<MemberPostItem>>> = _postDetailResult

    fun getClub(tag: String) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.
                getApiRepository().getMembersClub(tag, null, null)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clubItemResult.value = it }
        }
    }

    fun getPostDetail(item: MemberPostItem) {
        Timber.i("getPostDetail: item:$item")
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getMemberPostDetail(item.id)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postDetailResult.value = it }
        }
    }
}