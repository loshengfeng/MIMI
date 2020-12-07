package com.dabenxiang.mimi.view.adapter


import android.annotation.SuppressLint
import android.content.Context
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

    private lateinit var mContext: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        mContext = parent.context
        return FansViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_fans, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FansViewHolder) {
            holder.onBind(getItem(position), listener, position, mContext)
        }
    }


    class FansViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon_fans: ImageView = itemView.icon_fans
        private val name_fans: TextView = itemView.name_fans
        private val decs_fans: TextView = itemView.decs_fans
        private val follow_fnas: TextView = itemView.follow_fans

        fun onBind(item: FansItem?, listener: FanListener, position: Int, context: Context) {
            name_fans.text = item?.friendlyName
            decs_fans.text = item?.friendlyName

            follow_fnas.setOnClickListener {
                listener.onFollow(item!!, position, !item.isFollow)
            }

            itemView.setOnClickListener {
                listener.onAvatarClick(item!!.userId, item.friendlyName!!)
            }

            if (item!!.isFollow) {
                follow_fnas.text = context.getString(R.string.followed)
                follow_fnas.background =
                    context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
                follow_fnas.setTextColor(context.getColor(R.color.color_black_1_60))
            } else {
                follow_fnas.text = context.getString(R.string.follow)
                follow_fnas.background =
                    context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
                follow_fnas.setTextColor(context.getColor(R.color.color_red_1))
            }

            listener.onGetAvatarAttachment(item.avatarAttachmentId, icon_fans)
        }
    }

    interface FanListener {
        fun onAvatarClick(userId: Long, name: String)
        fun onGetAvatarAttachment(id: Long?, view:ImageView)
        fun onFollow(item: FansItem, position: Int, isFollow: Boolean)
    }
}