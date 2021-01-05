package com.dabenxiang.mimi.view.my_pages.pages.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.room.withTransaction
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MyFavoritesViewModel : ClubViewModel() {

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    private val _deleteFavorites = MutableLiveData<Int>()
    val deleteFavorites: LiveData<Int> = _deleteFavorites

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(pageCode:String, type: MyPagesType) =  postItems(pageCode, type).cachedIn(viewModelScope)

    @OptIn(ExperimentalPagingApi::class)
    private fun postItems(pageCode:String, type: MyPagesType) = Pager(
        config = PagingConfig(pageSize = MyPagesPostMediator.PER_LIMIT),
        remoteMediator = MyPagesPostMediator(mimiDB, domainManager, type, pageCode, pagingCallback)
    ) {
        mimiDB.postDBItemDao()
            .pagingSourceByPageCode(pageCode)
    }.flow.map {
        it.map {
            it.memberPostItem
        }
    }

    suspend fun checkoutItemsSize(pageCode:String):Int{
        return mimiDB.withTransaction {
            mimiDB.postDBItemDao().getPostDBItems(pageCode)?.size ?: 0
        }
    }

    fun deleteFavorites(items: List<MemberPostItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .deletePostFavorite(
                        items.map { it.postId }.joinToString(separator = ",")
                    )
                if (!result.isSuccessful) throw HttpException(result)
                items.forEach {
                    changeFavoritePostInDb(it.id)
                }
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