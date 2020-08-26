package com.dabenxiang.mimi.view.myfollow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment.Companion.TYPE_CLUB
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment.Companion.TYPE_MEMBER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class MyFollowViewModel : BaseViewModel() {
    private val _clubList = MutableLiveData<PagedList<ClubFollowItem>>()
    val clubList: LiveData<PagedList<ClubFollowItem>> = _clubList

    private val _clubCount = MutableLiveData<Int>()
    val clubCount: LiveData<Int> = _clubCount

    private val _memberList = MutableLiveData<PagedList<MemberFollowItem>>()
    val memberList: LiveData<PagedList<MemberFollowItem>> = _memberList

    private val _memberCount = MutableLiveData<Int>()
    val memberCount: LiveData<Int> = _memberCount

    private var _attachmentResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentResult: LiveData<ApiResult<AttachmentItem>> = _attachmentResult

    private val _clubDetail = MutableLiveData<ApiResult<MemberClubItem>>()
    val clubDetail: LiveData<ApiResult<MemberClubItem>> = _clubDetail

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

    fun initData(type: Int) {
        viewModelScope.launch {
            when (type) {
                TYPE_MEMBER -> {
                    val dataSrc = MemberFollowListDataSource(
                        viewModelScope,
                        domainManager,
                        memberPagingCallback
                    )
                    dataSrc.isInvalid
                    val factory = MemberFollowListFactory(dataSrc)
                    val config = PagedList.Config.Builder()
                        .setPageSize(MemberFollowListDataSource.PER_LIMIT.toInt())
                        .build()

                    LivePagedListBuilder(factory, config).build().asFlow().collect {
                        _memberList.value = it
                    }
                }

                TYPE_CLUB -> {
                    val dataSrc = ClubFollowListDataSource(
                        viewModelScope,
                        domainManager,
                        clubPagingCallback
                    )
                    dataSrc.isInvalid
                    val factory = ClubFollowListFactory(dataSrc)
                    val config = PagedList.Config.Builder()
                        .setPageSize(ClubFollowListDataSource.PER_LIMIT.toInt())
                        .build()

                    LivePagedListBuilder(factory, config).build().asFlow().collect {
                        _clubList.value = it
                    }
                }
            }
        }
    }

    private val memberPagingCallback = object : MyFollowPagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
            Timber.e(throwable)
        }

        override fun onTotalCount(count: Long, isInitial: Boolean) {
            _memberCount.postValue(count.toInt())
            if(isInitial) _cleanMemberRemovedPosList.postValue(null)
        }

        override fun onIdList(list: ArrayList<Long>, isInitial: Boolean) {
            if (isInitial) _userIdList.removeAll(list)
            _userIdList.addAll(list)
        }
    }

    private val clubPagingCallback = object : MyFollowPagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
            Timber.e(throwable)
        }

        override fun onTotalCount(count: Long, isInitial:Boolean) {
            _clubCount.postValue(count.toInt())
            if(isInitial) _cleanClubRemovedPosList.postValue(null)
        }

        override fun onIdList(list: ArrayList<Long>, isInitial: Boolean) {
            if (isInitial) _clubIdList.clear()
            _clubIdList.addAll(list)
        }
    }

    fun getAttachment(id: String, position: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                val item = AttachmentItem(
                    id = id,
                    bitmap = bitmap,
                    position = position
                )
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _attachmentResult.value = it }
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
                initData(TYPE_MEMBER)
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
                    if(result.isSuccessful) tmpList.add(clubId)
                }
                _clubIdList.removeAll(tmpList)
                emit(ApiResult.success(null))
                initData(TYPE_CLUB)
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

    fun getClub(clubId: Long) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersClub(clubId)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clubDetail.value = it }
        }
    }

}
