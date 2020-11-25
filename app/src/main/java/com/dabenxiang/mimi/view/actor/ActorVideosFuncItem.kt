package com.dabenxiang.mimi.view.actor

import android.widget.ImageView
import com.dabenxiang.mimi.model.api.vo.ActorVideoItem

class ActorVideosFuncItem(
    val getActorAvatarAttachment: ((Long?, ImageView) -> Unit) = { _, _ -> },
    val onVideoClickListener: ((ActorVideoItem, Int) -> Unit) = { _, _ -> },
)