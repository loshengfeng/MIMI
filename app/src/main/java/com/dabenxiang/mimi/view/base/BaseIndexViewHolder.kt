package com.dabenxiang.mimi.view.base

import android.view.View

abstract class BaseIndexViewHolder<M : Any>(
    itemView: View,
    protected val listener: IndexViewHolderListener
) : BaseViewHolder(itemView) {

    interface IndexViewHolderListener {
        fun onClickItemIndex(view: View, index: Int)
    }

    var index: Int = -1
        private set

    fun bind(bind: M?, dataIndex: Int) {
        index = dataIndex

        updated(bind)
    }

    abstract fun updated(model: M?)
}