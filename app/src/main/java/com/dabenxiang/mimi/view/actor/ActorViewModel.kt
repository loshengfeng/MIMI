package com.dabenxiang.mimi.view.actor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ActorViewModel : BaseViewModel() {
    private val _actorVideosResult = MutableLiveData<Pair<ApiResult<ArrayList<ActorVideosItem>>,ApiResult<ArrayList<ActorCategoriesItem>>>>()
    val actorVideosResult: LiveData<Pair<ApiResult<ArrayList<ActorVideosItem>>,ApiResult<ArrayList<ActorCategoriesItem>>>> = _actorVideosResult

    fun getActorList() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getActors()
                if (!result.isSuccessful) throw HttpException(result)
                val actorVideosList = result.body()?.content?.actorVideos
                val actorCategoriesList = result.body()?.content?.actorCategories
                emit(Pair(ApiResult.success(actorVideosList), ApiResult.success(actorCategoriesList)))
            }
                .onStart { emit(Pair(ApiResult.loading(), ApiResult.loading())) }
                .catch { e -> emit(Pair(ApiResult.error(e), ApiResult.error(e))) }
                .onCompletion { emit(Pair(ApiResult.loaded(), ApiResult.loaded())) }
                .collect { _actorVideosResult.value = it }
        }
    }

}