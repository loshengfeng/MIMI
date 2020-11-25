package com.dabenxiang.mimi.view.actor

import android.widget.ImageView
import androidx.paging.PagedList
import androidx.paging.PagingData
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.OrderType
import kotlinx.coroutines.CoroutineScope

class ActorCategoriesFuncItem(
    val getActorAvatarAttachment: ((Long?, ImageView) -> Unit) = { _, _ -> },
    val onClickListener: ((ActorCategoriesItem, Int) -> Unit) = { _, _ -> },
)