package com.dabenxiang.mimi.view.my_pages.pages.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class MyFavoritesViewModel : ClubViewModel() {

    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    private val _deleteFavorites = MutableLiveData<Int>()
    val deleteFavorites: LiveData<Int> = _deleteFavorites

    fun getData(adapter: FavoritesAdapter, isLike: Boolean) {

        Timber.i("getData")
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            getPostItemList(isLike)
                    .collectLatest {
                        adapter.submitData(it)
                    }
        }
    }

    fun getPostItemList(isLike: Boolean): Flow<PagingData<MemberPostItem>> {
        return Pager(
                config = PagingConfig(pageSize = FavoritesListDataSource.PER_LIMIT.toInt()),
                pagingSourceFactory = {
                    FavoritesListDataSource(
                            domainManager,
                            pagingCallback,
                            adWidth,
                            adHeight,
                            isLike
                    )
                }
        )
                .flow
                .onStart {  setShowProgress(true) }
                .onCompletion { setShowProgress(false) }
                .cachedIn(viewModelScope)
    }


    private val pagingCallback = object : PagingCallback {

        override fun onTotalCount(count: Long) {
            _postCount.postValue(count.toInt())
        }

    }

    fun deleteFavorites(items: List<MemberPostItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                        .deletePostFavorite(
                                items.map {it.postId}.joinToString(separator = ",")
                        )
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .collect { _cleanResult.value = it }
        }
    }

    fun deleteAllLike(items: List<MemberPostItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .deleteAllLike(
                        items.map {it.id}.joinToString(separator = ",")
                    )
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _cleanResult.value = it }
        }
    }
}