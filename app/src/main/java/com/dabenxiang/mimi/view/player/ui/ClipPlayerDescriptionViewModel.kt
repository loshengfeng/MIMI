package com.dabenxiang.mimi.view.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.extension.throttleFirst
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ClipPlayerDescriptionViewModel : BaseViewModel() {

    private val _getAdResult = MutableLiveData<ApiResult<AdItem>>()
    val getAdResult: LiveData<ApiResult<AdItem>> = _getAdResult

    private val _updateFollow = MutableLiveData<Int>()
    val updateFollow: LiveData<Int> = _updateFollow

    private var _likeResult = MutableLiveData<ApiResult<MemberPostItem>>()
    val likeResult: LiveData<ApiResult<MemberPostItem>> = _likeResult

    private var _favoriteResult = MutableLiveData<ApiResult<MemberPostItem>>()
    val favoriteResult: LiveData<ApiResult<MemberPostItem>> = _favoriteResult

    fun followPost(postId: Long, isDelete: Boolean) {
        viewModelScope.launch {
            flow {
                val rep = domainManager.getApiRepository()
                val result = when (isDelete) {
                    false -> rep.followPost(postId)
                    true -> rep.cancelFollowPost(postId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .throttleFirst(500)
                .collect { _updateFollow.value = 0 }
        }
    }

    fun favoritePost(item: MemberPostItem) {
        viewModelScope.launch {
            flow {
                val originFavorite = item.isFavorite
                val originFavoriteCnt = item.favoriteCount
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    !originFavorite -> apiRepository.addFavorite(item.id)
                    else -> apiRepository.deleteFavorite(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFavorite = !originFavorite
                item.favoriteCount = if (originFavorite) originFavoriteCnt - 1
                else originFavoriteCnt + 1
                changeFavoritePostInDb(item.id)
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _favoriteResult.value = it
                }
        }
    }

    fun likePost(item: MemberPostItem, type: LikeType) {
        viewModelScope.launch {
            flow {
                val originType = item.likeType
                val apiRepository = domainManager.getApiRepository()
                if (type != originType) {
                    val request = LikeRequest(type)
                    val result = apiRepository.like(item.id, request)
                    if (!result.isSuccessful) throw HttpException(result)
                    item.likeType = type
                    if (type == LikeType.LIKE) {
                        if (originType == LikeType.DISLIKE) item.dislikeCount -= 1
                        item.likeCount += 1
                    } else {
                        if (originType == LikeType.LIKE) item.likeCount -= 1
                        item.dislikeCount += 1
                    }
                } else {
                    val result = apiRepository.deleteLike(item.id)
                    if (!result.isSuccessful) throw HttpException(result)
                    item.likeType = null
                    if (type == LikeType.LIKE) item.likeCount -= 1
                    else item.dislikeCount -= 1
                }
                mimiDB.postDBItemDao().insertMemberPostItem(item)
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _likeResult.value = it
                }
        }
    }

}