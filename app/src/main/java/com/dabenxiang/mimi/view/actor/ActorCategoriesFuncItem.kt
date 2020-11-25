package com.dabenxiang.mimi.view.actor

import android.widget.ImageView
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem

class ActorCategoriesFuncItem(
    val getActorAvatarAttachment: ((Long?, ImageView) -> Unit) = { _, _ -> },
    val onClickListener: ((ActorCategoriesItem, Int) -> Unit) = { _, _ -> },
)