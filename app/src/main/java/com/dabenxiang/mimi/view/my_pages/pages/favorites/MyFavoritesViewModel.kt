package com.dabenxiang.mimi.view.my_pages.pages.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import timber.log.Timber

class MyFavoritesViewModel : ClubViewModel() {

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    private val _deleteFavorites = MutableLiveData<Int>()
    val deleteFavorites: LiveData<Int> = _deleteFavorites

    private val clearListCh = Channel<Unit>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(type: MyPagesType) = flowOf(
            clearListCh.receiveAsFlow().map { PagingData.empty() },
            postItems(type)

    ).flattenMerge(2).cachedIn(viewModelScope)

    private fun postItems(type: MyPagesType) = Pager(
            config = PagingConfig(pageSize = MyPagesPostMediator.PER_LIMIT),
            remoteMediator = MyPagesPostMediator(mimiDB, domainManager, type, pagingCallback)
    ) {
        mimiDB.postDBItemDao().pagingSourceByPageCode( MyPagesPostMediator::class.simpleName+ type.toString())


    }.flow.map {
        it.map {
            it.memberPostItem
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

    fun favoritePost(
            item: MemberPostItem,
            position: Int,
            isFavorite: Boolean,
            type:MyPagesType
    ) {

        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFavorite -> apiRepository.addFavorite(item.id)
                    else -> apiRepository.deleteFavorite(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFavorite = isFavorite
                _postChangedResult.postValue(ApiResult.success(item))
                emit(ApiResult.success(position))
            }
                    .flowOn(Dispatchers.IO)
                    .catch { e -> emit(ApiResult.error(e)) }
                    .onCompletion {
                        mimiDB.postDBItemDao().getMemberPostItemById(item.id)?.let { memberPostItem->
                            val item = memberPostItem.apply {
                                this.isFavorite = isFavorite
                                this.favoriteCount = when(isFavorite) {
                                    true -> this.favoriteCount+1
                                    else -> this.favoriteCount-1
                                }
                            }
                            val pageCode = MyPagesPostMediator::class.simpleName + type.toString()
                            mimiDB.postDBItemDao().insertMemberPostItem(item)
                            mimiDB.postDBItemDao().deleteItemByPageCode(
                                    pageCode= MyPagesPostMediator::class.simpleName + type.toString(),
                                    postDBId = memberPostItem.id
                            )
                            mimiDB.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, 0))
                        }
                    }
                    .collect {}
        }
    }
}