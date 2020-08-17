package com.dabenxiang.mimi.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.ClubListener
import com.dabenxiang.mimi.callback.PostAttachmentListener
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.dialog.chooseclub.ChooseClubDataSource
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.item_choose_club.view.*
import kotlinx.android.synthetic.main.item_list_loading.view.*

class ChooseClubAdapter(
    private val attachmentListener: PostAttachmentListener,
    private val clubListener: ClubListener
): PagedListAdapter<Any, BaseViewHolder>(diffCallback) {

    private lateinit var context: Context

    var totalCount = 0

    companion object {
        private const val VIEW_TYPE_CLUB = 1
        private const val VIEW_TYPE_LOAD = 2

        private val diffCallback = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(
                oldItem: Any,
                newItem: Any
            ): Boolean = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: Any,
                newItem: Any
            ): Boolean = oldItem == newItem
        }
    }

    override fun getItemCount(): Int {
        return if(currentList?.size ?: 0 >= ChooseClubDataSource.PER_LIMIT_LONG) {
            super.getItemCount() + 1
        } else {
            super.getItemCount()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < currentList?.size ?: 0) {
            VIEW_TYPE_CLUB
        } else {
            VIEW_TYPE_LOAD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context

        return when(viewType) {
            VIEW_TYPE_CLUB -> {
                ChooseClubViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_choose_club, parent, false))
            }
            VIEW_TYPE_LOAD -> {
                ListLoadingViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list_loading, parent, false)
                )
            }
            else -> {
                ChooseClubViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_choose_club, parent, false))

            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is ChooseClubViewHolder -> {
                val item = getItem(position) as MemberClubItem

                holder.clubName.text = item.title
                holder.hashTag.text = item.tag

                if (LruCacheUtils.getLruCache(item.avatarAttachmentId.toString()) == null) {
                    attachmentListener.getAttachment(item.avatarAttachmentId.toString(), position)
                } else {
                    val bitmap = LruCacheUtils.getLruCache(item.avatarAttachmentId.toString())
                    Glide.with(context)
                        .load(bitmap)
                        .circleCrop()
                        .into(holder.avatar)
                }

                holder.rootLayout.setOnClickListener {
                    clubListener.onClick(item)
                }
            }

            is ListLoadingViewHolder -> {
                if (position >= totalCount) holder.clLoading.visibility = View.GONE
            }
        }

    }

    fun updateItem(position: Int) {
        notifyItemChanged(position)
    }

    class ChooseClubViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val rootLayout:RelativeLayout = itemView.rootLayout
        val avatar: ImageView = itemView.iv_avatar
        val clubName: TextView = itemView.txt_clubName
        val hashTag: TextView = itemView.txt_hashtagName
    }

    class ListLoadingViewHolder(view: View) : BaseViewHolder(view) {
        val clLoading: ConstraintLayout = view.cl_loading
    }
}