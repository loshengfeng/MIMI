package com.dabenxiang.mimi.view.actor

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_actor_categories.view.*
import kotlinx.android.synthetic.main.item_actor_videos.view.tv_name
import org.koin.core.component.KoinComponent

class ActorCategoriesViewHolder(
    itemView: View
) : BaseViewHolder(itemView), KoinComponent{
    val name: TextView = itemView.tv_name
    val ivAvatar: ImageView = itemView.iv_photo
    val clCategory: ConstraintLayout = itemView.cl_category

    fun onBind(
        item: ActorCategoriesItem,
        actorCategoriesFuncItem: ActorCategoriesFuncItem,
        position: Int
    ){
        name.text = item.name
        actorCategoriesFuncItem.getActorAvatarAttachment(item.attachmentId, ivAvatar)
        clCategory.setOnClickListener {
            actorCategoriesFuncItem.onActorClickListener(item.id, position)
        }

    }
}