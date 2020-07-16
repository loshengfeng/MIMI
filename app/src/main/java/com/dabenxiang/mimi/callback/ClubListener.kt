package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.MemberClubItem

interface ClubListener {
    fun onClick(item: MemberClubItem)
}