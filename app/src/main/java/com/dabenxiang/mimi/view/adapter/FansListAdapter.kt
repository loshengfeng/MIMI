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
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.vo.FansItem
import com.dabenxiang.mimi.model.enums.ClickType
import kotlinx.android.synthetic.main.item_fans.view.*

class FansListAdapter(
    private val listener: BaseItemListener
) : PagingDataAdapter<FansItem, FansListAdapter.ViewHolder>(diffCallback) {
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

    override fun getItemCount(): Int {
        var realsize = super.getItemCount()
        if (super.getItemCount() == 0) {
            return 2
        } else {
            return realsize + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position === 0) {
            return R.layout.item_club_item_header
        } else {
            if (super.getItemCount() == 0) {
                return R.layout.item_empty
            } else {
                return R.layout.item_fans
            }
            return R.layout.item_fans
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FansListAdapter.ViewHolder {
        val itemview = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        when (viewType) {
            R.layout.item_club_item_header -> return HeaderViewHolder(itemview)
            R.layout.item_empty -> return EmptyViewHolder(itemview)
            R.layout.item_fans -> return FansViewHolder(itemview)
        }
        throw IllegalArgumentException("no such view type...")
    }

    override fun onBindViewHolder(holder: FansListAdapter.ViewHolder, position: Int) {
        if (holder is FansViewHolder) {
            if (super.getItemCount() > 0) {
                holder.onBind(itemCount - 1, getItem(position - 1), listener)
            }
        } else if (holder is HeaderViewHolder) {
            if (super.getItemCount() == 0) {
                holder.onBind(0, null, listener)
            } else {
                holder.onBind(itemCount - 1, null, listener)
            }
        } else {
            holder.onBind(super.getItemCount(), null, listener)
        }
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun onBind(total: Int, item: FansItem?, listener: BaseItemListener)
    }

    class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        val total_count_fans: TextView = itemView.findViewById(R.id.tv_title) as TextView
        override fun onBind(total: Int, item: FansItem?, listener: BaseItemListener) {
            total_count_fans.text = String.format(
                itemView.context.getString(R.string.total_count_fans),
                total.toString()
            )
        }
    }

    class EmptyViewHolder(itemView: View) : ViewHolder(itemView) {
        val text_empty: TextView = itemView.findViewById(R.id.text_page_empty) as TextView
        override fun onBind(total: Int, item: FansItem?, listener: BaseItemListener) {
            text_empty.text = ""
        }
    }

    class FansViewHolder(itemView: View) : ViewHolder(itemView) {
        private val icon_fans: ImageView = itemView.icon_fans
        private val name_fans: TextView = itemView.name_fans
        private val decs_fans: TextView = itemView.decs_fans
        private val follow_fnas: TextView = itemView.follow_fnas
        override fun onBind(total: Int, item: FansItem?, listener: BaseItemListener) {
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
        }
    }
}