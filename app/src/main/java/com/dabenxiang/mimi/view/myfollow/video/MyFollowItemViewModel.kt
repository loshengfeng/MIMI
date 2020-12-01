package com.dabenxiang.mimi.view.myfollow.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.MyFollowTabItemType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class MyFollowItemViewModel : ClubViewModel() {

    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount

    private val _deleteFavoriteResult = MutableLiveData<ApiResult<Boolean>>()
    val deleteFavoriteResult: LiveData<ApiResult<Boolean>> = _deleteFavoriteResult

    var totalCount: Int = 0

    fun getData(adapter: MyFollowItemAdapter, type: MyFollowTabItemType) {
        Timber.i("getData")
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            getPostItemList(type)
                .collectLatest {
                    adapter.submitData(it)
                }
        }
    }

    fun getPostItemList(type: MyFollowTabItemType): Flow<PagingData<PlayItem>> {
        return Pager(
            config = PagingConfig(pageSize = MyFollowItemDataSource.PER_LIMIT),
            pagingSourceFactory = {
                MyFollowItemDataSource(
                    domainManager,
                    pagingCallback,
                    adWidth,
                    adHeight,
                    type
                )
            }
        )
            .flow
            .onStart {  setShowProgress(true) }
            .onCompletion { setShowProgress(false) }
            .cachedIn(viewModelScope)
    }

    fun deleteMIMIVideoFavorite(videoId : String){
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.deleteMePlaylist(videoId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.isSuccessful))
            }
                    .flowOn(Dispatchers.IO)
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _deleteFavoriteResult.value = it }
        }
    }

    private val pagingCallback = object : PagingCallback {
        override fun onTotalCount(count: Long) {
            _postCount.postValue(count.toInt())
        }

    }
}