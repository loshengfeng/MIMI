package com.dabenxiang.mimi.view.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PostViewModel: BaseViewModel() {

    private val _clubItemResult = MutableLiveData<ApiResult<ArrayList<MemberClubItem>>>()
    val clubItemResult: LiveData<ApiResult<ArrayList<MemberClubItem>>> = _clubItemResult

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
}