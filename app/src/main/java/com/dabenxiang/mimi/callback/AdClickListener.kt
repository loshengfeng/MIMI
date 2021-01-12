package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.AdItem

interface AdClickListener {
    fun onAdClick(adItem: AdItem)
}