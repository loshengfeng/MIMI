package com.dabenxiang.mimi.view.club.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ClubItemViewModel : ClubViewModel() {

    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount

    var totalCount: Int = 0

    fun getData(adapter: ClubItemAdapter, type: ClubTabItemType) {
        Timber.i("getData")
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            getPostItemList(type)
                .collectLatest {
                    adapter.submitData(it)
                }
        }
    }

    fun getPostItemList(type: ClubTabItemType): Flow<PagingData<MemberPostItem>> {
        return Pager(
            config = PagingConfig(pageSize = ClubItemDataSource.PER_LIMIT),
            pagingSourceFactory = {
                ClubItemDataSource(
                    domainManager,
                    pagingCallback,
                    adWidth,
                    adHeight,
                    type
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