package com.dabenxiang.mimi.view.myfollow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.callback.PagingCallback
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

    private val _clubDetail = MutableLiveData<ApiResult<ArrayList<MemberClubItem>>>()
    val clubDetail: LiveData<ApiResult<ArrayList<MemberClubItem>>> = _clubDetail

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

    private val memberPagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
            Timber.e(throwable)
        }

        override fun onTotalCount(count: Long) {
            _memberCount.postValue(count.toInt())
        }
    }

    private val clubPagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
            Timber.e(throwable)
        }

        override fun onTotalCount(count: Long) {
            _clubCount.postValue(count.toInt())
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

    fun cancelFollowMember(userId: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().cancelMyMemberFollow(userId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    initData(TYPE_MEMBER)
                }
        }
    }

    fun cancelFollowClub(clubId: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().cancelMyClubFollow(clubId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    initData(TYPE_CLUB)
                }
        }
    }

    fun getClub(tag:String) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersClub(tag)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clubDetail.value = it }
        }
    }

}
