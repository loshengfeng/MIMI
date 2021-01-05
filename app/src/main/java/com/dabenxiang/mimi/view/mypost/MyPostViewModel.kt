package com.dabenxiang.mimi.view.mypost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MyPostViewModel : ClubViewModel() {

    companion object {
        const val TYPE_PIC = "type_pic"
        const val TYPE_COVER = "type_cover"
        const val TYPE_VIDEO = "type_video"
        const val USER_ID_ME: Long = -1
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(userId: Long) = postItems(userId).cachedIn(viewModelScope)

    @OptIn(ExperimentalPagingApi::class)
    private fun postItems(userId: Long) = Pager(
            config = PagingConfig(pageSize = MyPostMediator.PER_LIMIT),
            remoteMediator = MyPostMediator(mimiDB, domainManager, userId)
    ) {
        mimiDB.postDBItemDao().pagingSourceByPageCode(MyPostMediator::class.simpleName + userId.toString())

    }.flow.map {
        it.map { it.memberPostItem }
    }

}