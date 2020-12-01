package com.dabenxiang.mimi.view.myfollow.follow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class MyFollowInterestViewModel : ClubViewModel() {

    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount

    fun getData(adapter: MyFollowInterestAdapter) {
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
                config = PagingConfig(pageSize = MyFollowInterestListDataSource.PER_LIMIT.toInt()),
                pagingSourceFactory = {
                    MyFollowInterestListDataSource(
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