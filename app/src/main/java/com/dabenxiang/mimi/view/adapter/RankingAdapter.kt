package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.RankingFuncItem
import com.dabenxiang.mimi.model.api.vo.RankingItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.item_ranking.view.*

class RankingAdapter(private val context: Context,
                     private val rankingFuncItem: RankingFuncItem = RankingFuncItem())
    : PagedListAdapter<RankingItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<RankingItem>() {
            override fun areItemsTheSame(
                oldItem: RankingItem,
                newItem: RankingItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: RankingItem,
                newItem: RankingItem
            ): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RankingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as RankingViewHolder
        holder.bind(context, position, getItem(position)!!, rankingFuncItem)
    }

    class RankingViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val layout:ConstraintLayout= itemView.cl_ranking
        val picture: ImageView = itemView.iv_photo
        val title :TextView = itemView.tv_title
        val hot:TextView = itemView.tv_hot
        val ranking:TextView = itemView.tv_no

        private fun updatePicture(id: String) {
            val bitmap = LruCacheUtils.getLruCache(id)
            Glide.with(picture.context).load(bitmap).into(picture)
        }

        fun bind(
            context: Context,
            position: Int,
            item: RankingItem,
            rankingFuncItem: RankingFuncItem
        ) {

            ranking.let {
                ranking.text =""
                when(position){
                    0-> it.background = context.getDrawable(R.drawable.ico_fire_01)
                    1-> it.background = context.getDrawable(R.drawable.ico_fire_02)
                    2-> it.background = context.getDrawable(R.drawable.ico_fire_03)
                    else->{
                        it.background = context.getDrawable(R.drawable.ico_fire)
                        ranking.text =(position+1).toString()

                    }
                }
            }

            val avatarId = item.avatarAttachmentId.toString()
            if (avatarId != LruCacheUtils.ZERO_ID) {
                if (LruCacheUtils.getLruCache(avatarId) == null) {
                    rankingFuncItem.getBitmap(avatarId, position)
                } else {
                    updatePicture(avatarId)
                }
            } else {
                Glide.with(picture.context).load(R.color.color_white_1_50).circleCrop().into(picture)
            }

            title.text = item.title
            hot.text = item.count.toString()
            layout.setOnClickListener {
                rankingFuncItem.onItemClick(item)
            }
        }
    }




}