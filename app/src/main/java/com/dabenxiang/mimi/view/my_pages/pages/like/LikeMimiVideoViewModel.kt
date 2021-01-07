package com.dabenxiang.mimi.view.my_pages.pages.like

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.InteractiveHistoryItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.PlayListRequest
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LikeMimiVideoViewModel : ClubViewModel() {

    private val _deleteFavoriteResult = MutableLiveData<ApiResult<Boolean>>()
    val deleteFavoriteResult: LiveData<ApiResult<Boolean>> = _deleteFavoriteResult

    private val _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteMimiResult: LiveData<ApiResult<Int>> = _favoriteResult

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    var totalCount: Int = 0

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(pageCode: String, type: MyPagesType) =
        postItems(pageCode, type).cachedIn(viewModelScope)

    @OptIn(ExperimentalPagingApi::class)
    private fun postItems(pageCode: String, type: MyPagesType) = Pager(
        config = PagingConfig(pageSize = MyPagesPostMediator.PER_LIMIT),
        remoteMediator = MyPagesPostMediator(mimiDB, domainManager, type, pageCode, pagingCallback)
    ) {
        mimiDB.postDBItemDao().pagingSourceByPageCode(pageCode)


    }.flow.map { pagingData ->
        pagingData.map { dbItem ->
            dbItem.memberPostItem.toPlayItem()
        }
    }

    fun favorite(item: PlayItem, position: Int, type: MyPagesType, isFavorite: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFavorite -> apiRepository.postMePlaylist(
                        PlayListRequest(
                            item.videoId,
                            1
                        )
                    )
                    else -> apiRepository.deleteMePlaylist(item.videoId.toString())
                }
                if (!result.isSuccessful) throw HttpException(result)
                val countItem = result.body()?.content?.let {
                    when {
                        isFavorite -> it
                        else -> (it as ArrayList<*>)[0]
                    }
                } as InteractiveHistoryItem
                item.favorite = isFavorite
                countItem.favoriteCount?.let{ item.favoriteCount = it.toInt() }
                when (type) {
                    MyPagesType.LIKE_MIMI -> changeFavoriteMimiVideoInDb(item.videoId, item.favorite ?: false, item.favoriteCount ?: 0)
                    MyPagesType.LIKE_SHORT_VIDEO -> {
                        changeFavoriteSmallVideoInDb(item.videoId, item.favorite ?: false, item.favoriteCount ?: 0)
                    }
                }
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _favoriteResult.value = it }
        }
    }

    fun deleteAllLike(type: MyPagesType, items: List<PlayItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .deleteAllLike(
                        items.map { it.videoId }.joinToString(separator = ",")
                    )
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion {
                    val pageCode = MyPagesPostMediator::class.simpleName + type.toString()
                    mimiDB.postDBItemDao().deleteItemByPageCode(
                        pageCode = pageCode
                    )
                    mimiDB.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, 0))
                }
                .collect { _cleanResult.value = it }
        }
    }
}