package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.MemberPostItem

interface AdultListener {
    fun followPost(item: MemberPostItem, position: Int, isFollow: Boolean)
    fun doLike(item: MemberPostItem, position: Int, isLike: Boolean)
    fun comment()
    fun more()
}