package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem

interface OnMeMoreDialogListener {
    fun onEdit(item: BaseMemberPostItem)
    fun onDelete(item: BaseMemberPostItem)
    fun onCancel()
}