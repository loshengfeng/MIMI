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
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.search.video.SearchVideoAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.head_video_info.view.*

class SearchVideoViewHolder(
    itemView: View,
) : BaseViewHolder(itemView) {

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

    fun onBind(
        item: VideoItem,
        listener: SearchVideoAdapter.EventListener,
        searchStr: String = "",
        searchTag: String = ""
    ) {
        ivPhoto.setOnClickListener { listener.onVideoClick(item) }
        tvTitle.setOnClickListener { listener.onVideoClick(item) }
        tvMore.visibility = View.GONE
        val title = SpannableString(item.title)
        tvTitle.run {
            text = if (searchStr.isNotBlank()) {
                val firstChar = searchStr.toLowerCase()
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
                item.title
            }
        }

        tvDesc.run {
            text = item.description
        }

        listener.getDecryptSetting(item.source ?: "")?.takeIf { it.isImageDecrypt }
            ?.let { decryptSettingItem ->
                listener.decryptCover(item.cover ?: "", decryptSettingItem) {
                    Glide.with(ivPhoto.context)
                        .load(it).placeholder(R.drawable.img_nopic_03).into(ivPhoto)
                }
            } ?: run {
            Glide.with(ivPhoto.context)
                .load(item.cover).placeholder(R.drawable.img_nopic_03).into(ivPhoto)
        }

        // todo: no length data...
        tvLength.visibility = View.INVISIBLE

        if (item.tags is String && item.tags.isNotEmpty()) {
            setupChipGroup(item.tags.split(","), searchStr, searchTag, listener)
        }

        tvFavorite.run {
            text = item.favoriteCount.toString()
            val resFavorite = when (item.favorite) {
                true -> R.drawable.btn_favorite_white_s
                else -> R.drawable.btn_favorite_n
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(resFavorite, 0, 0, 0)
        }

        tvFavorite.setOnClickListener {
            listener.onFunctionClick(
                FunctionType.FAVORITE,
                it,
                item,
                position
            )
        }

        tvShare.run {
            val resShare = R.drawable.btn_share_gray_n
            setCompoundDrawablesRelativeWithIntrinsicBounds(resShare, 0, 0, 0)
        }

        tvShare.setOnClickListener {
            listener.onFunctionClick(
                FunctionType.SHARE,
                it,
                item,
                position
            )
        }

        tvLike.run {
            text = item.likeCount.toString()
            val res = when (item.like) {
                true -> R.drawable.ico_nice_s
                else -> R.drawable.ico_nice_gray
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
        }

        tvLike.setOnClickListener {
            listener.onFunctionClick(
                FunctionType.LIKE,
                it,
                item,
                position
            )
        }

        tvMsg.run {
            text = item.commentCount.toString()
            val resMsg = R.drawable.ico_messege_adult_gray
            setCompoundDrawablesRelativeWithIntrinsicBounds(resMsg, 0, 0, 0)
        }

        tvMsg.setOnClickListener {
            listener.onFunctionClick(
                FunctionType.MSG,
                it,
                item,
                position
            )
        }

        tvMore.run {
            val resMore = R.drawable.btn_more_gray_n
            setCompoundDrawablesRelativeWithIntrinsicBounds(resMore, 0, 0, 0)
        }

        tvMore.setOnClickListener {
            listener.onFunctionClick(
                FunctionType.MORE,
                it,
                item,
                position
            )
        }
    }

    private fun setupChipGroup(
        list: List<String>?,
        searchStr: String = "",
        searchTag: String = "",
        listener: SearchVideoAdapter.EventListener
    ) {
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

            if (tag == searchTag || tag == searchStr) {
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


    fun updateLike(item: VideoItem) {
        tvLike.run {
            text = item.likeCount.toString()
            val res = when (item.like) {
                true -> R.drawable.ico_nice_s
                else -> R.drawable.ico_nice_gray
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
        }
    }

    fun updateFavorite(item: VideoItem) {
        tvFavorite.run {
            text = item.favoriteCount.toString()
            val resFavorite = when (item.favorite) {
                true -> R.drawable.btn_favorite_white_s
                else -> R.drawable.btn_favorite_n
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(resFavorite, 0, 0, 0)
        }
    }
}