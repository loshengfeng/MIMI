package com.dabenxiang.mimi.view.actor

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_actor_categories.view.*
import kotlinx.android.synthetic.main.item_actor_videos.view.tv_name
import org.koin.core.component.KoinComponent
import timber.log.Timber

class ActorListViewHolder(
    itemView: View
) : BaseViewHolder(itemView), KoinComponent{
    val name: TextView = itemView.tv_name
    val ivAvatar: ImageView = itemView.iv_photo
    val clCategory: ConstraintLayout = itemView.cl_category

    fun onBind(
        item: ActorCategoriesItem,
        actorListFuncItem: ActorListFuncItem,
        position: Int
    ){
        name.text = item.name
        actorListFuncItem.getActorAvatarAttachment(item.attachmentId, ivAvatar)
        clCategory.setOnClickListener {
            actorListFuncItem.onActorClickListener(item.id, position)
        }

    }
}