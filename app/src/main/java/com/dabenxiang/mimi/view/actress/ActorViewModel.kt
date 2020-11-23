package com.dabenxiang.mimi.view.actress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ActorViewModel : BaseViewModel() {
    private val _actorVideosResult = MutableLiveData<ApiResult<ArrayList<ActorVideosItem>>>()
    val actorVideosResult: LiveData<ApiResult<ArrayList<ActorVideosItem>>> = _actorVideosResult

    fun getActorList() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getActors()
                if (!result.isSuccessful) throw HttpException(result)
                val actorVideosList = result.body()?.content?.actorVideos
                val actorCategoriesList = result.body()?.content?.actorCategories
                emit(ApiResult.success(actorVideosList))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _actorVideosResult.value = it }
        }
    }

}