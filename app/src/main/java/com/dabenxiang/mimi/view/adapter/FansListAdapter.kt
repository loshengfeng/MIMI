package com.dabenxiang.mimi.view.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.dabenxiang.mimi.R
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.model.api.vo.error.FansItem
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_header.view.tv_title
import kotlinx.android.synthetic.main.item_header_fans.view.*


class FansListAdapter(
    private val listener: EventListener
) : PagedListAdapter<FansItem, FansListAdapter.ViewHolder>(diffCallback) {
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

    interface EventListener {
        fun onClickListener(item: FansItem, position: Int)
        fun onGetAttachment(id: Long?, view: ImageView)
    }

    override fun getItemCount(): Int {
        var realsize = 0
        if (currentList.isNullOrEmpty()) {
            realsize = 1
        } else {
            currentList?.size.let {
                realsize = it!!
            }
        }
        return realsize + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (currentList.isNullOrEmpty()) {
            if (position === 0) {
                return R.layout.item_club_item_header
            } else {
                return R.layout.item_empty
            }
        } else {
            if (position === 0) {
                return R.layout.item_club_item_header
            } else {
                return R.layout.item_fans
            }
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FansListAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        when (viewType) {
            R.layout.item_club_item_header -> return HeaderViewHolder(
                layoutInflater.inflate(
                    viewType,
                    parent,
                    false
                )
            )

            R.layout.item_empty -> return EmptyViewHolder(
                layoutInflater.inflate(
                    viewType,
                    parent,
                    false
                )
            )

            R.layout.item_fans -> return FansViewHolder(
                layoutInflater.inflate(
                    viewType,
                    parent,
                    false
                )
            )
        }
        throw IllegalArgumentException("no such view type...")
    }

    override fun onBindViewHolder(holder: FansListAdapter.ViewHolder, position: Int) {
        var total = 0
        currentList?.size.let {
            if (it != null) {
                total = it
            }
        }
        holder.onBind(total, position)
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun onBind(total: Int, position: Int)
    }

    class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        val total_count_fans: TextView = itemView.findViewById(R.id.tv_title) as TextView
        override fun onBind(total: Int, position: Int) {
            total_count_fans.text = String.format(
                itemView.context.getString(R.string.total_count_fans),
                total.toString()
            )
        }
    }

    class EmptyViewHolder(itemView: View) : ViewHolder(itemView) {
        val text_empty: TextView = itemView.findViewById(R.id.text_page_empty) as TextView
        override fun onBind(total: Int, position: Int) {
            text_empty.text =""
        }
    }

    class FansViewHolder(itemView: View) : ViewHolder(itemView) {
        override fun onBind(total: Int, position: Int) {
        }
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }
}