package com.dabenxiang.mimi.view.ranking

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.RankingFuncItem
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.PostStatisticsItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.my_pages.pages.mimi_video.CollectionFuncItem
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_ranking.view.*

class RankingClipAdapter(
    private val context: Context,
    private val rankingFuncItem: RankingFuncItem = RankingFuncItem(),
    private val funcItem: CollectionFuncItem,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: ArrayList<VideoItem> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RankingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        )
    }

    fun updateData(data: ArrayList<VideoItem>) {
        dataList.clear()
        dataList.addAll(data)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as RankingViewHolder
        val item = dataList[position]
        holder.bind(
            context,
            position,
            item,
            rankingFuncItem,
            funcItem
        )
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
            item: VideoItem,
            rankingFuncItem: RankingFuncItem,
            funcItem: CollectionFuncItem
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

            funcItem.getDecryptSetting(item.source ?: "")?.takeIf { it.isImageDecrypt }
                ?.let { decryptSettingItem ->
                    funcItem.decryptCover(item.cover ?: "", decryptSettingItem) {
                        Glide.with(picture.context)
                            .load(it).placeholder(R.drawable.img_nopic_03).into(picture)
                    }
                } ?: run {
                Glide.with(picture.context)
                    .load(item.cover).placeholder(R.drawable.img_nopic_03).into(picture)
            }

            title.text = item.title
            hot.text = item.timesWatched.toString()
            layout.setOnClickListener {
                rankingFuncItem.onClipItemClick(item)
            }
        }
    }

    override fun getItemCount() = dataList.size
}