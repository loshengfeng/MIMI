package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.view.adapter.SearchVideoAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.head_video_info.view.*

class SearchVideoViewHolder(
    itemView: View,
    val listener: SearchVideoAdapter.EventListener
) : BaseAnyViewHolder<VideoItem>(itemView) {

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
        tvMore.setOnClickListener {
            listener.onFunctionClick(
                FunctionType.MORE,
                it,
                data!!
            )
        }
        tvTitle.setOnClickListener { listener.onVideoClick(data!!) }
        tvMore.visibility = View.GONE
    }

    override fun updated() {
        var textColor = android.R.color.black
        var descTextColor = R.color.color_black_1_50

        val title = SpannableString(data?.title)
        tvTitle.run {
            text = if (data?.searchingStr != null && data?.searchingStr?.isNotBlank()!!) {
                val firstChar = data?.searchingStr?.toLowerCase() ?: "".substring(0, 1)
                val firstIndex = title.toString().toLowerCase().indexOf(firstChar)
                if (firstIndex != -1) {
                    title.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                itemView.context,
                                R.color.color_red_1
                            )
                        ),
                        firstIndex,
                        firstIndex + firstChar.length,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    title
                } else
                    title
            } else {
                data?.title
            }
            setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }

        tvDesc.run {
            text = data?.description
            setTextColor(ContextCompat.getColor(itemView.context, descTextColor))
        }

        Glide.with(ivPhoto.context)
            .load(data?.cover)
            .into(ivPhoto)

        // todo: no length data...
        tvLength.visibility = View.INVISIBLE

        if (data?.tags is String && (data?.tags as String).isNotEmpty()) {
            setupChipGroup((data?.tags as String).split(","))
        }

        tvFavorite.run {
            text = data?.favoriteCount.toString()
            val resFavorite = when (data?.favorite) {
                true -> R.drawable.btn_favorite_white_s
                else -> R.drawable.btn_favorite_n
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(resFavorite, 0, 0, 0)
            setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }

        tvShare.run {
            val resShare = when (data?.isAdult) {
                true -> R.drawable.btn_share_white_n
                else -> R.drawable.btn_share_gray_n
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(resShare, 0, 0, 0)
        }

        tvLike.run {
            text = data?.likeCount.toString()
            val res = when (data?.like) {
                true -> R.drawable.ico_nice_s
                else -> R.drawable.ico_nice_gray
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
            setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }

        tvMsg.run {
            text = data?.commentCount.toString()
            val resMsg = R.drawable.ico_messege_adult_gray
            setCompoundDrawablesRelativeWithIntrinsicBounds(resMsg, 0, 0, 0)
            setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }

        tvMore.run {
            val resMore = R.drawable.btn_more_gray_n
            setCompoundDrawablesRelativeWithIntrinsicBounds(resMore, 0, 0, 0)
        }
    }

    private fun setupChipGroup(list: List<String>?) {
        reflowGroup.reflow_group.removeAllViews()
        if (list == null) {
            return
        }

        list.indices.mapNotNull {
            list[it]
        }.forEach { tag ->
            val chip = LayoutInflater.from(reflowGroup.context)
                .inflate(R.layout.chip_item, reflowGroup, false) as Chip
            chip.text = tag

            if (tag == data?.searchingTag) {
                chip.setTextColor(chip.context.getColor(R.color.color_red_1))
            } else {
                chip.setTextColor(chip.context.getColor(R.color.color_black_1_50))
            }
            chip.chipBackgroundColor =
                ColorStateList.valueOf(chip.context.getColor(R.color.color_black_1_10))
            chip.isClickable = true

            chip.setOnClickListener {
                listener.onChipClick((it as Chip).text.toString())
            }
            reflowGroup.addView(chip)
        }
    }
}