package com.dabenxiang.mimi.view.chathistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.adapter.ChatHistoryAdapter
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatHistoryViewModel : BaseViewModel() {

    private val _fakeChatHistory = MutableLiveData<ArrayList<ChatHistoryAdapter.FakeChatHistory>>()
    val fakeChatHistory: LiveData<ArrayList<ChatHistoryAdapter.FakeChatHistory>> = _fakeChatHistory

    fun getFakeChatHistory() {
        viewModelScope.launch {
            flow {
                // TODO Api? Room?
                val array = arrayListOf<ChatHistoryAdapter.FakeChatHistory>()

                array.add(ChatHistoryAdapter.FakeChatHistory("", "可拉可拉", "安安 **娘", "https://7.share.photo.xuite.net/fishyang33/175a993/6279035/1097556504_l.jpg", "2020-3-3", false))
                array.add(ChatHistoryAdapter.FakeChatHistory("", "袋龍", "安安 幹**", "https://7.share.photo.xuite.net/fishyang33/175a97b/6279035/1097557504_l.jpg", "2020-3-2", true))
                array.add(ChatHistoryAdapter.FakeChatHistory("", "毛球", "**********", "https://7.share.photo.xuite.net/fishyang33/175a90d/6279035/1097657746_o.jpg", "2020-3-2", false))
                array.add(ChatHistoryAdapter.FakeChatHistory("", "巴大蝴", "小智在哪裏？", "https://7.share.photo.xuite.net/fishyang33/175a90d/6279035/1097658258_o.jpg", "2020-3-2", false))
                array.add(ChatHistoryAdapter.FakeChatHistory("", "鐵甲蛹", "硬啦", "https://7.share.photo.xuite.net/fishyang33/175a9fd/6279035/1097656194_l.jpg", "2020-3-1", true))
                array.add(ChatHistoryAdapter.FakeChatHistory("", "鬼斯", "安安 **娘", "https://7.share.photo.xuite.net/fishyang33/175a915/6279035/1097656218_l.jpg", "2020-3-1", false))
                array.add(ChatHistoryAdapter.FakeChatHistory("", "皮卡丘", "皮卡皮卡", "https://attach.setn.com/newsimages/2015/08/31/328024-XXL.jpg", "2020-2-28", false))

                emit(ApiResult.success(array))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { resp ->
                    when (resp) {
                        is ApiResult.Success -> {
                            _fakeChatHistory.value = resp.result
                        }
                        is ApiResult.Error -> Timber.e(resp.throwable)
                        is ApiResult.Loading -> Timber.d("Loading")
                        is ApiResult.Loaded -> Timber.d("Loaded")
                    }
                }
        }
    }
}