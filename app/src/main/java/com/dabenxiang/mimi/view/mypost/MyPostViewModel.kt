package com.dabenxiang.mimi.view.mypost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
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

class MyPostViewModel : ClubViewModel() {

    companion object {
        const val TYPE_PIC = "type_pic"
        const val TYPE_COVER = "type_cover"
        const val TYPE_VIDEO = "type_video"
        const val USER_ID_ME: Long = -1
    }

    private val clearListCh = Channel<Unit>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(userId: Long) = flowOf(
            clearListCh.receiveAsFlow().map { PagingData.empty() },
            postItems(userId)

    ).flattenMerge(2).cachedIn(viewModelScope)

    private fun postItems(userId: Long) = Pager(
            config = PagingConfig(pageSize = MyPostMediator.PER_LIMIT),
            remoteMediator = MyPostMediator(mimiDB, domainManager, userId)
    ) {
        mimiDB.postDBItemDao().pagingSourceByPageCode( MyPostMediator::class.simpleName+ userId.toString())


    }.flow

//    fun getMyPostPagingItems(
//        userId: Long
//    ): Flow<PagingData<MemberPostItem>> {
//        return Pager(
//            config = PagingConfig(
//                pageSize = ApiRepository.NETWORK_PAGE_SIZE,
//                enablePlaceholders = false
//            ),
//            pagingSourceFactory = { MyPostPagingSource(userId, domainManager) }
//        ).flow.cachedIn(viewModelScope)
//    }
//
//    fun likePost(item: MemberPostItem, position: Int, isLike: Boolean) {
//        viewModelScope.launch {
//            flow {
//                val apiRepository = domainManager.getApiRepository()
//                val likeType = when {
//                    isLike -> LikeType.LIKE
//                    else -> LikeType.DISLIKE
//                }
//                val request = LikeRequest(likeType)
//                val result = apiRepository.like(item.id, request)
//                if (!result.isSuccessful) throw HttpException(result)
//                emit(ApiResult.success(position))
//            }
//                .flowOn(Dispatchers.IO)
//                .catch { e -> emit(ApiResult.error(e)) }
//                .collect { _likePostResult.value = it }
//        }
//    }

//    fun favoritePost(
//        item: MemberPostItem,
//        position: Int,
//        isFavorite: Boolean
//    ) {
//        viewModelScope.launch {
//            flow {
//                val apiRepository = domainManager.getApiRepository()
//                val result = when {
//                    isFavorite -> apiRepository.addFavorite(item.id)
//                    else -> apiRepository.deleteFavorite(item.id)
//                }
//                if (!result.isSuccessful) throw HttpException(result)
//                emit(ApiResult.success(position))
//            }
//                .flowOn(Dispatchers.IO)
//                .catch { e -> emit(ApiResult.error(e)) }
//                .collect { _favoriteResult.value = it }
//        }
//    }

}