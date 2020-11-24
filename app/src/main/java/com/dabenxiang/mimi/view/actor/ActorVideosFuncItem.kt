package com.dabenxiang.mimi.view.actor

import android.widget.ImageView
import androidx.paging.PagedList
import androidx.paging.PagingData
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.OrderType
import kotlinx.coroutines.CoroutineScope

class ActorVideosFuncItem(
    val getActorAvatarAttachment: ((Long?, ImageView) -> Unit) = { _, _ -> },
    val onClickListener: ((ActorVideosItem, Int) -> Unit) = { _, _ -> },
)