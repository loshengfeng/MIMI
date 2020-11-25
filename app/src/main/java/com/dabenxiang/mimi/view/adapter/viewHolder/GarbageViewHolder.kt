package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.dabenxiang.mimi.view.player.GuessLikeVideoAdapter
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.nested_garbage_video_item.view.*

class GarbageViewHolder(
    itemView: View,
    val listener: GuessLikeVideoAdapter.OnGarbageItemClick
) : BaseAnyViewHolder<BaseVideoItem.Video>(itemView) {

    private val garbageIvPoster = itemView.iv_poster!!
    private val garbageVideoTitle = itemView.video_title
    private val garbageVideoTag = itemView.tag_group

    init {
        itemView.setOnClickListener { data?.let { data -> listener.onStatisticsDetail(data) } }
    }

    override fun updated() {}

    override fun updated(position: Int) {
        garbageVideoTitle.text = data?.title

        Glide.with(garbageIvPoster.context)
            .load(data?.imgUrl).placeholder(R.drawable.img_nopic_03).into(garbageIvPoster)

        garbageVideoTag.removeAllViews()

        if(data?.tags != null)
            setupChipGroup(data?.tags as List<String>)
    }

    private fun setupChipGroup(list: List<String>?) {
        garbageVideoTag.removeAllViews()

        if (list == null) {
            return
        }

        list.indices.mapNotNull {
            list[it]
        }.forEach {
            val chip = LayoutInflater.from(itemView.context).inflate(
                R.layout.chip_item,
                garbageVideoTag,
                false
            ) as Chip
            chip.text = it

            chip.setTextColor(itemView.context.getColor(R.color.color_black_1_50))

            chip.setOnClickListener {
                listener.onTagClick(chip.text.toString())
            }

            garbageVideoTag.addView(chip)
        }
    }
}