package com.dabenxiang.mimi.view.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dabenxiang.mimi.R

class PlayerInfoAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_banner), LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: String) {
        
    }
}