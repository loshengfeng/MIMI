package com.dabenxiang.mimi.view.myfollow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.PagingCallback
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment.Companion.TYPE_CLUB
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment.Companion.TYPE_MEMBER
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MyFollowViewModel : BaseViewModel()