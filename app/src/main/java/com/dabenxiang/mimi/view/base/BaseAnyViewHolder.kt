package com.dabenxiang.mimi.view.base

import android.view.View

abstract class BaseAnyViewHolder<M : Any>(itemView: View) : BaseViewHolder(itemView) {

    protected var data: M? = null

    fun bind(bind: M?) {
        data = bind
        updated()
    }

    fun bind(bind: M?, position: Int) {
        data = bind
        updated(position)
    }

    abstract fun updated()
    open fun updated(position: Int) {}
}