package com.dabenxiang.mimi.view.actor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class ActorViewModel : BaseViewModel() {
    private val _actorVideosResult =
        MutableLiveData<Pair<ApiResult<ArrayList<ActorVideosItem>>, ApiResult<ArrayList<ActorCategoriesItem>>>>()
    val actorVideosResult: LiveData<Pair<ApiResult<ArrayList<ActorVideosItem>>, ApiResult<ArrayList<ActorCategoriesItem>>>> =
        _actorVideosResult

    private val _actorsCount = MutableLiveData<Int>()
    val actorsCount: LiveData<Int> = _actorsCount

    fun getActors() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getActors()
                if (!result.isSuccessful) throw HttpException(result)
                val actorVideosList = result.body()?.content?.actorVideos
                val actorCategoriesList = result.body()?.content?.actorCategories
                emit(
                    Pair(
                        ApiResult.success(actorVideosList),
                        ApiResult.success(actorCategoriesList)
                    )
                )
            }
                .onStart { emit(Pair(ApiResult.loading(), ApiResult.loading())) }
                .catch { e -> emit(Pair(ApiResult.error(e), ApiResult.error(e))) }
                .onCompletion { emit(Pair(ApiResult.loaded(), ApiResult.loaded())) }
                .collect { _actorVideosResult.value = it }
        }
    }

    fun getData(adapter: ActorListAdapter) {
        Timber.i("getData")
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            getActorList()
                .collectLatest {
                    adapter.submitData(it)
                }
        }
    }

    fun getActorList(): Flow<PagingData<ActorCategoriesItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = ActorListDataSource.PER_LIMIT,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ActorListDataSource(domainManager, pagingCallback) }
        ).flow.cachedIn(viewModelScope)
    }

    private val pagingCallback = object : PagingCallback {
        override fun onTotalCount(count: Long) {
            _actorsCount.postValue(count.toInt())
        }
    }
}