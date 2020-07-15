package com.dabenxiang.mimi.view.favroite

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.view.adapter.FavoriteAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.head_video_info.view.*

class FavoritePlayViewHolder(
    itemView: View,
    val listener: FavoriteAdapter.EventListener
) : BaseAnyViewHolder<PlayItem>(itemView) {

    private val tvTitle = itemView.findViewById(R.id.tv_title) as TextView
    private val tvDesc = itemView.findViewById(R.id.tv_desc) as TextView
    private val ivPhoto = itemView.findViewById(R.id.iv_photo) as ImageView
    private val tvLength = itemView.findViewById(R.id.tv_length) as TextView
    private val reflowGroup = itemView.findViewById(R.id.reflow_group) as ChipGroup
    private val tvFavorite = itemView.findViewById(R.id.tv_favorite) as TextView
    private val tvLike = itemView.findViewById(R.id.tv_like) as TextView
    private val tvMsg = itemView.findViewById(R.id.tv_msg) as TextView
    private val tvShare = itemView.findViewById(R.id.tv_share) as TextView
    private val tvMore = itemView.findViewById(R.id.tv_more) as TextView

    init {
        ivPhoto.setOnClickListener { listener.onVideoClick(data!!) }
        tvLike.setOnClickListener {
            listener.onFunctionClick(
                FunctionType.LIKE,
                it,
                data!!
            )
        }
        tvFavorite.setOnClickListener {
            listener.onFunctionClick(
                FunctionType.FAVORITE,
                it,
                data!!
            )
        }
        tvMsg.setOnClickListener {
            listener.onFunctionClick(
                FunctionType.MSG,
                it,
                data!!
            )
        }
        tvShare.setOnClickListener {
            listener.onFunctionClick(
                FunctionType.SHARE,
                it,
                data!!
            )
        }
        tvMore.visibility = View.GONE
//        tvMore.setOnClickListener {
//            listener.onFunctionClick(
//                FunctionType.MORE,
//                it,
//                data!!
//            )
//        }
    }

    override fun updated() {
        tvTitle.text = data?.title
        tvDesc.text = data?.description

        Glide.with(ivPhoto.context)
            .load(data?.cover)
            .into(ivPhoto)

        // todo: no length data...
        tvLength.text = "09:00:00"

        if (!data?.tags.isNullOrEmpty()) {
            setupChipGroup(data?.tags)
        }

        tvFavorite.text = data?.favoriteCount.toString()
        tvLike.text = data?.likeCount.toString()
        val res = if (data?.like == true) {
            R.drawable.ico_nice_s
        } else {
            R.drawable.ico_nice_gray
        }
        tvLike.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)

        tvMsg.text = data?.commentCount.toString()

    }

    private fun setupChipGroup(list: List<String>?) {
        reflowGroup.reflow_group.removeAllViews()

        if (list == null) {
            return
        }

        list.indices.mapNotNull {
            list[it]
        }.forEach {
            val chip = LayoutInflater.from(reflowGroup.context)
                .inflate(R.layout.chip_item, reflowGroup, false) as Chip
            chip.text = it
            chip.setTextColor(chip.context.getColor(R.color.color_black_1_50))
            chip.chipBackgroundColor =
                ColorStateList.valueOf(chip.context.getColor(R.color.color_black_1_10))
            reflowGroup.addView(chip)
        }
    }
}