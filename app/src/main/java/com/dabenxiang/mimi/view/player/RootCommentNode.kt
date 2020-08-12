package com.dabenxiang.mimi.view.player

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem

class RootCommentNode(val data: MembersPostCommentItem) : BaseExpandNode() {

    init {
        isExpanded = false
    }

    val nestedCommentList = mutableListOf<BaseNode>()

    override val childNode: MutableList<BaseNode>? = nestedCommentList
}
