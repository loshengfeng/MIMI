package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.AdItem

interface PostPagingCallBack : PagingCallback {
    fun onGetAd(adItem: AdItem)
}