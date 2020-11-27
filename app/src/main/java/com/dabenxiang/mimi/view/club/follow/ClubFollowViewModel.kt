package com.dabenxiang.mimi.view.club.follow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class ClubFollowViewModel : ClubViewModel() {



    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount

    fun getData(adapter: ClubPostFollowAdapter) {
        Timber.i("getData")
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            getPostItemList()
                    .collectLatest {
                        adapter.submitData(it)
                    }
        }
    }

    fun getPostItemList(): Flow<PagingData<MemberPostItem>> {
        return Pager(
                config = PagingConfig(pageSize = ClubPostFollowListDataSource.PER_LIMIT.toInt()),
                pagingSourceFactory = {
                    ClubPostFollowListDataSource(
                            domainManager,
                            pagingCallback,
                            adWidth,
                            adHeight

                    )
                }
        )
                .flow
                .onStart {  setShowProgress(true) }
                .onCompletion { setShowProgress(false) }
                .cachedIn(viewModelScope)
    }


    private val pagingCallback = object : PagingCallback {

        override fun onTotalCount(count: Long) {
            _postCount.postValue(count.toInt())
        }

    }

}