package com.dabenxiang.mimi.view.like

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.callback.MyLikePagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LikeViewModel : BaseViewModel() {

    private val _clubCount = MutableLiveData<Int>()
    val clubCount: LiveData<Int> = _clubCount

    private val _memberCount = MutableLiveData<Int>()
    val memberCount: LiveData<Int> = _memberCount

    private val _clubDetail = MutableLiveData<ApiResult<MemberPostItem>>()
    val clubDetail: LiveData<ApiResult<MemberPostItem>> = _clubDetail

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    private val _cancelOneClub = MutableLiveData<ApiResult<Int>>()
    val cancelOneClub: LiveData<ApiResult<Int>> = _cancelOneClub

    private val _cleanClubRemovedPosList = MutableLiveData<Nothing>()
    val cleanClubRemovedPosList: LiveData<Nothing> = _cleanClubRemovedPosList

    private val _cancelOneMember = MutableLiveData<ApiResult<Int>>()
    val cancelOneMember: LiveData<ApiResult<Int>> = _cancelOneMember

    private val _cleanMemberRemovedPosList = MutableLiveData<Nothing>()
    val cleanMemberRemovedPosList: LiveData<Nothing> = _cleanMemberRemovedPosList

    private val _clubIdList = ArrayList<Long>()
    private val _userIdList = ArrayList<Long>()

    fun getMemberList(): Flow<PagingData<MemberPostItem>> {
        return Pager(
            config = PagingConfig(pageSize = MemberLikeListDataSource.PER_LIMIT.toInt()),
            pagingSourceFactory = {
                MemberLikeListDataSource(
                    domainManager,
                    memberPagingCallback
                )
            }
        )
            .flow
            .cachedIn(viewModelScope)
    }

    fun getClubList(): Flow<PagingData<MemberPostItem>> {
        return Pager(
            config = PagingConfig(pageSize = ClubLikeListDataSource.PER_LIMIT.toInt()),
            pagingSourceFactory = {
                ClubLikeListDataSource(
                    domainManager,
                    clubPagingCallback
                )
            }
        )
            .flow
            .cachedIn(viewModelScope)
    }

    private val memberPagingCallback = object : MyLikePagingCallback {
        override fun onTotalCount(count: Long) {
            _memberCount.postValue(count.toInt())
            _cleanMemberRemovedPosList.postValue(null)
        }

        override fun onIdList(list: ArrayList<Long>, isInitial: Boolean) {
            if (isInitial) _userIdList.removeAll(list)
            _userIdList.addAll(list)
        }
    }

    private val clubPagingCallback = object : MyLikePagingCallback {
        override fun onTotalCount(count: Long) {
            _clubCount.postValue(count.toInt())
            _cleanClubRemovedPosList.postValue(null)
        }

        override fun onIdList(list: ArrayList<Long>, isInitial: Boolean) {
            if (isInitial) _clubIdList.clear()
            _clubIdList.addAll(list)
        }
    }

    fun cancelFollowMember(userId: Long, position: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().cancelMyMemberFollow(userId)
                if (!result.isSuccessful) throw HttpException(result)
                _userIdList.remove(userId)
                val count = _memberCount.value?.minus(1)
                _memberCount.postValue(count)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _cancelOneMember.value = it
                }
        }
    }

    fun cancelFollowClub(
        clubId: Long,
        position: Int
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().cancelMyClubFollow(clubId)
                if (!result.isSuccessful) throw HttpException(result)
                _clubIdList.remove(clubId)
                val count = _clubCount.value?.minus(1)
                _clubCount.postValue(count)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _cancelOneClub.value = it
                }
        }
    }

    fun cleanAllFollowMember() {
        viewModelScope.launch {
            flow {
                val tmpList = ArrayList<Long>()
                _userIdList.forEach { userId ->
                    val result = domainManager.getApiRepository().cancelMyMemberFollow(userId)
                    if (result.isSuccessful) tmpList.add(userId)
                }
                _userIdList.removeAll(tmpList)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _cleanResult.value = it
                }
        }
    }

    fun cleanAllFollowClub() {
        viewModelScope.launch {
            flow {
                val tmpList = ArrayList<Long>()
                _clubIdList.forEach { clubId ->
                    val result = domainManager.getApiRepository().cancelMyClubFollow(clubId)
                    if (result.isSuccessful) tmpList.add(clubId)
                }
                _clubIdList.removeAll(tmpList)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _cleanResult.value = it
                }
        }
    }

//    fun getClub(clubId: Long) {
//        viewModelScope.launch {
//            flow {
//                val resp = domainManager.getApiRepository().getMembersClub(clubId)
//                if (!resp.isSuccessful) throw HttpException(resp)
//                emit(ApiResult.success(resp.body()?.content))
//            }
//                .flowOn(Dispatchers.IO)
//                .catch { e -> emit(ApiResult.error(e)) }
//                .collect { _clubDetail.value = it }
//        }
//    }

}
