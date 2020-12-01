package com.dabenxiang.mimi.callback

import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

interface BaseItemListener {
    fun onItemClick(item: JvmType.Object, type: Int)
}