package com.dabenxiang.mimi.view.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.FansItem
import com.dabenxiang.mimi.model.enums.ClickType
import kotlinx.android.synthetic.main.item_fans.view.*

class FansListAdapter(
    private val listener: FanListener
) : PagingDataAdapter<FansItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<FansItem>() {
            override fun areItemsTheSame(
                oldItem: FansItem,
                newItem: FansItem
            ): Boolean = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: FansItem,
                newItem: FansItem
            ): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return FansViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_fans, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FansViewHolder) {
            holder.onBind(itemCount, getItem(position ), listener)
        }
    }


    class FansViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon_fans: ImageView = itemView.icon_fans
        private val name_fans: TextView = itemView.name_fans
        private val decs_fans: TextView = itemView.decs_fans
        private val follow_fnas: TextView = itemView.follow_fnas

        fun onBind(total: Int, item: FansItem?, listener: FanListener) {
            name_fans.text = item?.friendlyName
            decs_fans.text = item?.friendlyName
            val fan = item!!
            follow_fnas.setOnClickListener {
                listener.onItemClick(fan, ClickType.TYPE_FOLLOW)
            }
            name_fans.setOnClickListener {
                listener.onItemClick(fan, ClickType.TYPE_AUTHOR)
            }
            icon_fans.setOnClickListener {
                listener.onItemClick(fan, ClickType.TYPE_AUTHOR)
            }

            listener.onGetAvatarAttachment(item.avatarAttachmentId, icon_fans)
        }
    }

    interface FanListener {
        fun onItemClick(item: Any, type: ClickType)
        fun onGetAvatarAttachment(id: Long?, view:ImageView)
    }
}