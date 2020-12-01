package com.dabenxiang.mimi.view.fans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.FansItem
import com.dabenxiang.mimi.view.adapter.FansListAdapter
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class FansListViewModel : BaseViewModel() {
    private val _fansCount = MutableLiveData<Int>()
    val fansCount: LiveData<Int> = _fansCount
    private val pagingCallback = object : PagingCallback {}

    fun getData(adapter: FansListAdapter) {
        Timber.i("getData")
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            getFansList().collectLatest {
                adapter.submitData(it)
            }
        }
    }

    fun getFansList(): Flow<PagingData<FansItem>> {
        return Pager(
            config = PagingConfig(pageSize = FansDataSource.PER_LIMIT),
            pagingSourceFactory = {
                FansDataSource(
                    domainManager,
                    pagingCallback
                )
            }
        )
            .flow
//            .onStart {  setShowProgress(true) }
//            .onCompletion { setShowProgress(false) }
            .cachedIn(viewModelScope)
    }
}