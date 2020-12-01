package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.enums.ClickType

interface BaseItemListener {
    fun onItemClick(item: Any, type: ClickType)
}