package com.dabenxiang.mimi.view.dialog.chooseclub

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class ChooseClubDialogViewModel : BaseViewModel() {

    private val _clubList = MutableLiveData<ArrayList<MemberClubItem>>()
    val clubList: LiveData<ArrayList<MemberClubItem>> = _clubList

    private val _loadingStatus = MutableLiveData<Boolean>()
    val loadingStatus: LiveData<Boolean> = _loadingStatus

    private val _totalCount = MutableLiveData<Long>()
    val totalCount: MutableLiveData<Long> = _totalCount

    fun getClubList() {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersClub(
                    ""
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                val item = resp.body()
                val clubItems = item?.content

                emit(clubItems)
            }
                .flowOn(Dispatchers.IO)
                .onStart { _loadingStatus.value = true }
                .catch { e -> Timber.d("e: $e") }
                .onCompletion {  _loadingStatus.value = false }
                .collect { _clubList.value = it }
        }
    }
}