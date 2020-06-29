package com.dabenxiang.mimi.view.player

import com.chad.library.adapter.base.entity.node.BaseNode
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import java.lang.ref.WeakReference

class NestedCommentNode(parentNode: RootCommentNode, val data: MembersPostCommentItem) : BaseNode() {
    override val childNode: MutableList<BaseNode>? = null

    val parentNodeRef = WeakReference(parentNode)
}
