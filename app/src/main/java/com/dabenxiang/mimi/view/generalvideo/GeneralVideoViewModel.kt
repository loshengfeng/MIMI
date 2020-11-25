package com.dabenxiang.mimi.view.generalvideo

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.model.api.ApiRepository.Companion.NETWORK_PAGE_SIZE
import com.dabenxiang.mimi.model.api.vo.VideoByCategoryItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.Flow

class GeneralVideoViewModel : BaseViewModel() {

    fun getVideoByCategory(category: String): Flow<PagingData<VideoByCategoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { VideoPagingSource(domainManager, category) }
        ).flow.cachedIn(viewModelScope)
    }

}