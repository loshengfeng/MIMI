package com.dabenxiang.mimi.view.club.topic_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class TopicViewModel : BaseViewModel() {

    private var _getClubInfo = MutableLiveData<ApiResult<MemberClubItem>>()
    val getClubInfo: LiveData<ApiResult<MemberClubItem>> = _getClubInfo

    private var _followClubResult = MutableLiveData<ApiResult<Boolean>>()
    val followClubResult: LiveData<ApiResult<Boolean>> = _followClubResult

    fun getMembersClub(clubId: Long) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getMembersClub(clubId)

                Timber.i("getMembersClub clubId=$clubId result=$result")

                if (!result.isSuccessful) throw HttpException(result)

                emit(ApiResult.success(result.body()?.content))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _getClubInfo.value = it }
        }
    }

    fun followClub(item: MemberClubItem, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followClub(item.id)
                    else -> apiRepository.cancelFollowClub(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFollow = isFollow
                emit(ApiResult.success(isFollow))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _followClubResult.value = it }
        }
    }
}