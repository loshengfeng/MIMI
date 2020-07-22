package com.dabenxiang.mimi.view.favroite

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.adapter.FavoriteAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.head_video_info.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.SimpleDateFormat
import java.util.*

class FavoritePostViewHolder(
    itemView: View,
    private val listener: FavoriteAdapter.EventListener
) : BaseAnyViewHolder<PostFavoriteItem>(itemView), KoinComponent {
    private val gson: Gson by inject()
    private val ivHead = itemView.findViewById(R.id.iv_head) as ImageView
    private val tvName = itemView.findViewById(R.id.tv_name) as TextView
    private val tvTime = itemView.findViewById(R.id.tv_time) as TextView
    private val tvFollow = itemView.findViewById(R.id.tv_follow) as TextView
    private val tvTitle = itemView.findViewById(R.id.tv_title) as TextView
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
        tvFollow.setOnClickListener {
            listener.onFunctionClick(
                    FunctionType.FOLLOW,
                    it,
                    data!!
            )
        }
        tvShare.visibility = View.GONE
        tvTitle.visibility = View.INVISIBLE
        tvMore.visibility = View.INVISIBLE
    }

    override fun updated() {
        data?.posterAvatarAttachmentId?.let {
            listener.onAvatarDownload(ivHead, it.toString())
        }

        tvName.text = data?.posterName
//        tvTitle.text = data?.title
        tvTime.text = data?.postDate.let { date ->
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
            ).format(date)
        }
        val contentItem = gson.fromJson(data?.content.toString(), MediaContentItem::class.java)
        tvLength.text = contentItem?.shortVideo?.length

        when (data?.isFollow) {
            true -> {
                tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_black_1_60))
                tvFollow.setBackgroundResource(R.drawable.bg_gray_6_radius_16)
                tvFollow.setText(R.string.followed)
            }
            else -> {
                tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_red_1))
                tvFollow.setBackgroundResource(R.drawable.bg_red_1_stroke_radius_16)
                tvFollow.setText(R.string.follow)
            }
        }

        setupChipGroup(data?.tags)

        tvLike.text = data?.likeCount.toString()
        val res = if (data?.likeType == LikeType.LIKE.value) {
            R.drawable.ico_nice_s
        } else {
            R.drawable.ico_nice_gray
        }
        tvLike.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)

        tvFavorite.text = data?.favoriteCount.toString()
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
            chip.isClickable = true
            chip.setOnClickListener {
                listener.onChipClick((it as Chip).text.toString())
            }
            reflowGroup.addView(chip)
        }
    }
}