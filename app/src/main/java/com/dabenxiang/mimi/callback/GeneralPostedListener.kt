package com.dabenxiang.mimi.callback

interface GeneralPostedListener : GeneralPostedActionListener {
    fun onClipCommentClick(item: List<Any>, position: Int)
    fun onCommentClick(item: Any, type: Int)
    fun onChipClick(item: Any, tag: String)
}