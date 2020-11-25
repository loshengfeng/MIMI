package com.dabenxiang.mimi.view.actor

import com.dabenxiang.mimi.model.api.vo.ActorVideoItem

class ActorVideoFuncItem(
    val onVideoClickListener: ((ActorVideoItem, Int) -> Unit) = { _, _ -> },
)