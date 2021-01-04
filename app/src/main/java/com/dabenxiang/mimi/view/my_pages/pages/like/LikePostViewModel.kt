package com.dabenxiang.mimi.view.my_pages.pages.like

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.model.enums.LikeType
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
import timber.log.Timber

class LikePostViewModel : ClubViewModel() {

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

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
        it.map { it.memberPostItem }
    }

    fun like(
            item: MemberPostItem,
            position: Int,
            isLike: Boolean,
            type:MyPagesType) {
        viewModelScope.launch {
            Timber.i("likePost item=$item")
            flow {
                val apiRepository = domainManager.getApiRepository()

                val likeType: LikeType = when {
                    isLike -> LikeType.LIKE
                    else -> LikeType.DISLIKE
                }
                val request = LikeRequest(likeType)
                val result = when {
                    isLike -> apiRepository.like(item.id, request)
                    else -> apiRepository.deleteLike(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.likeType = if (isLike) LikeType.LIKE else LikeType.DISLIKE
                item.likeCount = item.likeCount
                _postChangedResult.postValue(ApiResult.success(item))
                emit(ApiResult.success(position))
            }
                    .flowOn(Dispatchers.IO)
                    .catch { e -> emit(ApiResult.error(e)) }
                    .onCompletion {
                        mimiDB.postDBItemDao().getMemberPostItemById(item.id)?.let { memberPostItem->
                            val item = memberPostItem.apply {
                                when {
                                    isLike -> {
                                        this.likeType = LikeType.LIKE
                                        this.likeCount += 1
                                    }
                                    else-> {
                                        this.likeType = LikeType.DISLIKE
                                        this.likeCount -= 1
                                    }
                                }
                            }
                            val pageCode =MyPagesPostMediator::class.simpleName + type.toString()
                            mimiDB.postDBItemDao().insertMemberPostItem(item)
                            mimiDB.postDBItemDao().deleteItemByPageCode(
                                    pageCode= pageCode,
                                    postDBId = memberPostItem.id
                            )
                        }
                    }
                    .collect {}
        }
    }

    fun deleteAllLike(type:MyPagesType, items: List<MemberPostItem>) {
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
                .onCompletion {
                    val pageCode =MyPagesPostMediator::class.simpleName + type.toString()
                    mimiDB.postDBItemDao().deleteItemByPageCode(
                            pageCode= pageCode
                    )
                    mimiDB.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, 0))

                }
                .collect { _cleanResult.value = it }
        }
    }
}