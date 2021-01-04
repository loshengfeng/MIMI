package com.dabenxiang.mimi.view.actor

import android.widget.ImageView
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem

class ActorListFuncItem(
    val getActorAvatarAttachment: ((Long?, ImageView) -> Unit) = { _, _ -> },
    val onActorClickListener: ((Long, Int) -> Unit) = { _, _ -> },
)