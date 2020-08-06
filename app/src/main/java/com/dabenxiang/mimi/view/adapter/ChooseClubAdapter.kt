package com.dabenxiang.mimi.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.ClubListener
import com.dabenxiang.mimi.callback.PostAttachmentListener
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.item_choose_club.view.*

class ChooseClubAdapter(
    private val attachmentListener: PostAttachmentListener,
    private val clubListener: ClubListener
): PagedListAdapter<Any, BaseViewHolder>(diffCallback) {

    private lateinit var context: Context

    companion object {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_choose_club, parent, false)
        return ChooseClubViewHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder as ChooseClubViewHolder
        val item = getItem(position)
        item as MemberClubItem

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

        holder.itemView.setOnClickListener {
            clubListener.onClick(item)
        }
    }

    fun updateItem(position: Int) {
        notifyItemChanged(position)
    }

    class ChooseClubViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val avatar: ImageView = itemView.iv_avatar
        val clubName: TextView = itemView.txt_clubName
        val hashTag: TextView = itemView.txt_hashtagName
    }
}