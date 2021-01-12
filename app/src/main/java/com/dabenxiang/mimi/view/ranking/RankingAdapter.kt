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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.RankingFuncItem
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.PostStatisticsItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.my_pages.pages.mimi_video.CollectionFuncItem
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_ranking.view.*
import timber.log.Timber

class RankingAdapter(
    private val context: Context,
    private val rankingFuncItem: RankingFuncItem = RankingFuncItem(),
    private val funcItem: CollectionFuncItem
) : PagedListAdapter<PostStatisticsItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<PostStatisticsItem>() {
            override fun areItemsTheSame(
                oldItem: PostStatisticsItem,
                newItem: PostStatisticsItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PostStatisticsItem,
                newItem: PostStatisticsItem
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
        holder.bind(
            context,
            position,
            currentList?.toMutableList()!!,
            getItem(position)!!,
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
            items: MutableList<PostStatisticsItem>,
            item: PostStatisticsItem,
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

            when (PostType.getTypeByValue(item.type)) {
                PostType.SMALL_CLIP -> {
                    funcItem.getDecryptSetting(item.source)
                        ?.takeIf { it.isImageDecrypt }
                        ?.let { decryptSettingItem ->
                            funcItem.decryptCover(item.cover, decryptSettingItem) {
                                Glide.with(picture.context)
                                    .load(it).placeholder(R.drawable.img_nopic_03).into(picture)
                            }
                        } ?: run {
                        Glide.with(picture.context)
                            .load(item.cover).placeholder(R.drawable.img_nopic_03).into(picture)
                    }
                }
                PostType.IMAGE -> {
                    try {
                        val contentItem = Gson().fromJson(item.content, MediaContentItem::class.java)
                        contentItem.images?.also { images ->
                            if (images.isEmpty()) {
                                return@also
                            } else if (!TextUtils.isEmpty(images[0].url)) {
                                Glide.with(picture.context)
                                    .load(images[0].url)
                                    .placeholder(R.drawable.img_nopic_03)
                                    .error(R.drawable.img_nopic_03)
                                    .into(picture)
                            } else {
                                rankingFuncItem.getBitmap(
                                    images[0].id.toLongOrNull(),
                                    picture
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                        Glide.with(picture.context)
                            .load(item.cover)
                            .placeholder(R.drawable.img_nopic_03)
                            .error(R.drawable.img_nopic_03)
                            .into(picture)
                    }
                }
                else -> {
                    Glide.with(picture.context)
                        .load(item.cover).placeholder(R.drawable.img_nopic_03).into(picture)
                }
            }

            title.text = item.title
            hot.text = item.count.toString()
            layout.setOnClickListener {
                rankingFuncItem.onItemClick(items, position)
            }
        }
    }
}