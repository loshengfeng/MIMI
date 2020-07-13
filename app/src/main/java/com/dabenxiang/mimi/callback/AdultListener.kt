package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.MemberPostItem

interface AdultListener {
    fun followPost(item: MemberPostItem, position: Int, isFollow: Boolean)
    fun doLike()
    fun comment()
    fun more()
}