package com.dabenxiang.mimi.view.my_pages.pages.follow_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

class MyFollowListViewModel : ClubViewModel() {

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult
    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount

    fun getClubFollowData(adapter: ClubFollowPeopleAdapter) {
        Timber.i("getClubFollowData")
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            getClubFollowList()
                    .collectLatest {
                        adapter.submitData(it)
                    }
        }
    }


    private fun getClubFollowList(): Flow<PagingData<ClubFollowItem>> {
        return Pager(
                config = PagingConfig(pageSize = ClubFollowListDataSource.PER_LIMIT),
                pagingSourceFactory = {
                    ClubFollowListDataSource(
                            domainManager,
                            pagingCallback
                    )
                }
        )
                .flow
                .onStart { setShowProgress(true) }
                .onCompletion { setShowProgress(false) }
                .cachedIn(viewModelScope)
    }

    fun getMemberData(adapter: MemberFollowPeopleAdapter) {
        Timber.i("getMemberData")
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            getMemberFollowList()
                    .collectLatest {
                        adapter.submitData(it)
                    }
        }
    }

    private fun getMemberFollowList(): Flow<PagingData<MemberFollowItem>> {
        return Pager(
                config = PagingConfig(pageSize = MemberFollowListDataSource.PER_LIMIT),
                pagingSourceFactory = {
                    MemberFollowListDataSource(
                            domainManager,
                            pagingCallback
                    )
                }
        )
                .flow
                .onStart { setShowProgress(true) }
                .onCompletion { setShowProgress(false) }
                .cachedIn(viewModelScope)
    }

    private val pagingCallback = object : PagingCallback {
        override fun onTotalCount(count: Long) {
            _postCount.postValue(count.toInt())
        }
    }

    fun cleanAllFollowMember(items: List<MemberFollowItem>) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().cancelMyMemberFollow(
                        items.map { it.userId }.joinToString(separator = ","))
                if (!result.isSuccessful) throw HttpException(result)
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

    fun cleanAllFollowClub(items: List<ClubFollowItem>) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().cancelMyClubFollow(
                        items.map { it.clubId }.joinToString(separator = ","))
                if (!result.isSuccessful) throw HttpException(result)
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
}