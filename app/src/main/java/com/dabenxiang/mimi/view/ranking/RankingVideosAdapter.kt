package com.dabenxiang.mimi.view.ranking

import android.content.Context
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dabenxiang.mimi.JOEY
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.RankingFuncItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_ranking.view.*

class RankingVideosAdapter(
    private val context: Context,
    private val rankingFuncItem: RankingFuncItem = RankingFuncItem()
) : PagedListAdapter<StatisticsItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<StatisticsItem>() {
            override fun areItemsTheSame(
                oldItem: StatisticsItem,
                newItem: StatisticsItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: StatisticsItem,
                newItem: StatisticsItem
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
        val layout: ConstraintLayout = itemView.cl_ranking
        val picture: ImageView = itemView.iv_photo
        val title: TextView = itemView.tv_title
        val hot: TextView = itemView.tv_hot
        val ranking: TextView = itemView.tv_no

        fun bind(
            context: Context,
            position: Int,
            item: StatisticsItem,
            rankingFuncItem: RankingFuncItem
        ) {

            ranking.let {
                ranking.text = ""
                when (position) {
                    0 -> it.background = context.getDrawable(R.drawable.ico_fire_01)
                    1 -> it.background = context.getDrawable(R.drawable.ico_fire_02)
                    2 -> it.background = context.getDrawable(R.drawable.ico_fire_03)
                    else -> {
                        it.background = context.getDrawable(R.drawable.ico_fire)
                        ranking.text = (position + 1).toString()

                    }
                }
            }

            Glide.with(picture.context)
                .load(item.cover)
                .centerCrop()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        rankingFuncItem.getDecryptSetting(JOEY)?.takeIf { it.isImageDecrypt }
                            ?.let { decryptSettingItem ->
                                rankingFuncItem.decryptCover(item.cover ?: "", decryptSettingItem) {
                                    Glide.with(picture.context)
                                        .load(it)
                                        .into(picture)
                                }
                            }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(picture)

            title.text = item.title
            hot.text = item.count?.toString() ?: "0"
            layout.setOnClickListener {
                rankingFuncItem.onVideoItemClick(item)
            }
        }
    }
}