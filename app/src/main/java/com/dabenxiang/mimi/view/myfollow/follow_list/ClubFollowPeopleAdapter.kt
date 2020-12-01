package com.dabenxiang.mimi.view.myfollow.follow_list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.vo.BaseItem
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import timber.log.Timber

class ClubFollowPeopleAdapter(
        val context: Context,
        private val listener: BaseItemListener
) : PagingDataAdapter<ClubFollowItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<ClubFollowItem>() {
            override fun areItemsTheSame(
                    oldItem: ClubFollowItem,
                    newItem: ClubFollowItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                    oldItem: ClubFollowItem,
                    newItem: ClubFollowItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    var removedPosList = ArrayList<Int>()

//    override fun getItemViewType(position: Int): Int {
//        val item = getItem(position)
//        return when (item?.type) {
//            PostType.VIDEO -> VIEW_TYPE_CLIP
//            PostType.IMAGE -> VIEW_TYPE_PICTURE
//            PostType.AD -> VIEW_TYPE_AD
//            else -> VIEW_TYPE_TEXT
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ClubFollowViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_follow_club, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.also {

            (holder as ClubFollowViewHolder)
//                    holder.pictureRecycler.tag = position
            holder.onBind(
                    it, listener
            )

        }
    }
}