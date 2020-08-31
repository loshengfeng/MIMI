package com.dabenxiang.mimi.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_ADULT
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_NORMAL
import com.dabenxiang.mimi.view.favroite.FavoritePlayViewHolder
import com.dabenxiang.mimi.view.favroite.FavoritePostViewHolder

class FavoriteAdapter(
    private val listener: EventListener
) : PagedListAdapter<Any, RecyclerView.ViewHolder>(diffCallback) {
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

    interface EventListener {
        fun onVideoClick(item: Any, position: Int? = 0)
        fun onFunctionClick(
            type: FunctionType,
            view: View,
            item: Any,
            position: Int? = 0
        )

        fun onChipClick(text: String, type: Int? = -1)
        fun onGetAttachment(id: String, position: Int, type: AttachmentType)
        fun onAvatarClick(userId: Long, name: String)
    }

    private var isAdult = false
    private val TYPE_SHORT_VIDEO = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_NORMAL -> {
                FavoritePlayViewHolder(
                layoutInflater.inflate(
                    R.layout.item_general_normal,
                    parent,
                    false
                ), listener
            )}
            TYPE_ADULT ->
                FavoritePlayViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_favorite_normal,
                        parent,
                        false
                    ), listener)
            else ->
                FavoritePostViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_favorite_short_video,
                        parent,
                        false
                    ), listener
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is FavoritePlayViewHolder -> holder.bind(item as PlayItem)
            is FavoritePostViewHolder -> {
                holder.bind(item as MemberPostItem, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val isPlayItem = getItem(position) is PlayItem
        val item = if(isPlayItem) getItem(position) as PlayItem else getItem(position) as MemberPostItem
        return when (isPlayItem) {
            true -> if((item as PlayItem).isAdult!!) TYPE_ADULT else TYPE_NORMAL
            else -> TYPE_SHORT_VIDEO
        }
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }

    fun isAdult(): Boolean {
        return isAdult
    }

    fun setAdult(adult: Boolean = false) {
        this.isAdult = adult
    }
}