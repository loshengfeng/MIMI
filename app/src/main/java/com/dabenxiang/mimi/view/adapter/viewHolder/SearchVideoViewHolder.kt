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
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.search.video.SearchVideoAdapter
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.item_favorite_common.view.*
import kotlinx.android.synthetic.main.item_favorite_normal.view.*

class SearchVideoViewHolder(
    itemView: View,
) : BaseViewHolder(itemView) {

    private val tvTitle = itemView.tv_title
    private val tvDesc = itemView.tv_desc
    private val ivPhoto = itemView.iv_photo
    private val tvLength = itemView.tv_length
    private val reflowGroup = itemView.reflow_group
    private val tvFavorite = itemView.tv_favorite
    private val tvLike = itemView.tv_like
    private val tvMsg = itemView.tv_msg
    private val tvShare = itemView.tv_share
    private val tvMore = itemView.tv_more
    private val ivAd:ImageView = itemView.iv_ad

    fun onBind(
        item: MemberPostItem,
        position:Int,
        listener: SearchVideoAdapter.EventListener,
        searchStr: String = "",
        searchTag: String = "",
        adGap:Int? = null
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
            text = item.videoDescription
        }

        if (adGap != null && position % adGap == adGap - 1) {
            ivAd.visibility = View.VISIBLE
            val options = RequestOptions()
                .priority(Priority.NORMAL)
                .placeholder(R.drawable.img_ad_df)
                .error(R.drawable.img_ad)
            Glide.with(ivAd.context)
                .load(item.adItem?.href)
                .apply(options)
                .into(ivAd)
            ivAd.setOnClickListener {
                GeneralUtils.openWebView(ivAd.context, item.adItem?.target ?: "")
            }
        } else {
            ivAd.visibility = View.GONE
        }

        listener.getDecryptSetting(item.videoSource ?: "")?.takeIf { it.isImageDecrypt }
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

        setupChipGroup(item.tags, searchStr, searchTag, listener)

        tvFavorite.run {
            text = item.favoriteCount.toString()
            val resFavorite = when (item.isFavorite) {
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
            val res = when (item.likeType) {
                LikeType.LIKE -> R.drawable.ico_nice_s
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


    fun updateLike(item: MemberPostItem) {
        tvLike.run {
            text = item.likeCount.toString()
            val res = when (item.likeType) {
                LikeType.LIKE -> R.drawable.ico_nice_s
                else -> R.drawable.ico_nice_gray
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
        }
    }

    fun updateFavorite(item: MemberPostItem) {
        tvFavorite.run {
            text = item.favoriteCount.toString()
            val resFavorite = when (item.isFavorite) {
                true -> R.drawable.btn_favorite_white_s
                else -> R.drawable.btn_favorite_n
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(resFavorite, 0, 0, 0)
        }
    }
}