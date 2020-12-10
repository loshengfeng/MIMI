package com.dabenxiang.mimi.view.my_pages.pages.follow_list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.view.adapter.viewHolder.MemberFollowViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.coroutines.CoroutineScope

class MemberFollowPeopleAdapter(
    val context: Context,
    private val listener: BaseItemListener,
    private val viewModelScope: CoroutineScope
) : PagingDataAdapter<MemberFollowItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<MemberFollowItem>() {
            override fun areItemsTheSame(
                    oldItem: MemberFollowItem,
                    newItem: MemberFollowItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                    oldItem: MemberFollowItem,
                    newItem: MemberFollowItem
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
        return MemberFollowViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_follow_personal, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.also {

            (holder as MemberFollowViewHolder)
//                    holder.pictureRecycler.tag = position
            holder.onBind(
                    it, listener, viewModelScope
            )

        }
    }
}