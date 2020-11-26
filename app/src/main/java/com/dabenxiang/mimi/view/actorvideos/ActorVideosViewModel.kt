package com.dabenxiang.mimi.view.actorvideos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ActorVideosViewModel : BaseViewModel() {
    private val _actorVideosByIdResult = MutableLiveData<ApiResult<ActorVideosItem>>()
    val actorVideosByIdResult: LiveData<ApiResult<ActorVideosItem>> = _actorVideosByIdResult

    fun getActorVideosById(id: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getActorsList(id)
                if (!result.isSuccessful) throw HttpException(result)
                val actorVideosItem = result.body()?.content
                emit(ApiResult.success(actorVideosItem))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _actorVideosByIdResult.value = it }
        }
    }
}