package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_clip_post.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class ClipPostHolder(itemView: View) : BaseViewHolder(itemView), KoinComponent {

    private val accountManager: AccountManager by inject()

    private val ivAvatar: ImageView = itemView.img_avatar
    private val name: TextView = itemView.tv_name
    private val time: TextView = itemView.tv_time
    private val follow: TextView = itemView.tv_follow
    private val title: TextView = itemView.tv_title
    private val ivPhoto: ImageView = itemView.iv_photo
    private val tvLength: TextView = itemView.tv_length
    private val tagChipGroup: ChipGroup = itemView.chip_group_tag
    private val likeImage: ImageView = itemView.iv_like
    private val likeCount: TextView = itemView.tv_like_count


    private val favoriteImage: ImageView = itemView.iv_favorite
    private val favoriteCount: TextView = itemView.tv_favorite_count


    private val commentImage: ImageView = itemView.iv_comment
    private val commentCount: TextView = itemView.tv_comment_count
    private val moreImage: ImageView = itemView.iv_more

    fun onBind(
        item: MemberPostItem,
        itemList: List<MemberPostItem>?,
        position: Int,
        adultListener: AdultListener,
        tag: String,
        memberPostFuncItem: MemberPostFuncItem,
        isClipList: Boolean
    ) {
        name.text = item.postFriendlyName
        time.text = GeneralUtils.getTimeDiff(item.creationDate, Date())
        title.text = item.title
        follow.visibility =
            if (accountManager.getProfile().userId == item.creatorId) View.GONE else View.VISIBLE
        updateLikeAndFollowItem(item, itemList, memberPostFuncItem)

        val avatarId = item.avatarAttachmentId.toString()
        if (avatarId != LruCacheUtils.ZERO_ID) {
            if (LruCacheUtils.getLruCache(avatarId) == null) {
                memberPostFuncItem.getBitmap(avatarId) { id -> updateAvatar(id) }
            } else {
                updateAvatar(item.avatarAttachmentId.toString())
            }
        } else {
            Glide.with(ivAvatar.context).load(R.drawable.default_profile_picture).circleCrop()
                .into(ivAvatar)
        }

        tagChipGroup.removeAllViews()
        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(tagChipGroup.context, R.color.adult_color_status_bar)
            )
            if (TextUtils.isEmpty(tag)) {
                chip.setTextColor(tagChipGroup.context.getColor(R.color.color_white_1_50))
            } else {
                if (it == tag) {
                    chip.setTextColor(chip.context.getColor(R.color.color_red_1))
                } else {
                    chip.setTextColor(tagChipGroup.context.getColor(R.color.color_white_1_50))
                }
            }

            chip.setOnClickListener { view ->
                adultListener.onChipClick(PostType.VIDEO, (view as Chip).text.toString())
            }

            tagChipGroup.addView(chip)
        }

        var contentItem: MediaContentItem? = null
        try {
            contentItem = Gson().fromJson(item.content, MediaContentItem::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tvLength.text = contentItem?.shortVideo?.length
        contentItem?.images?.takeIf { it.isNotEmpty() }?.also { images ->
            images[0].also { image ->
                if (TextUtils.isEmpty(image.url)) {
                    image.id.takeIf { !TextUtils.isEmpty(it) && it != "0" }?.also { id ->
                        LruCacheUtils.getLruCache(id)?.also { bitmap ->
                            Glide.with(ivPhoto.context).load(bitmap).into(ivPhoto)
                        } ?: run { memberPostFuncItem.getBitmap(id) { id -> updatePicture(id) } }
                    } ?: run {
                        Glide.with(ivPhoto.context).load(R.drawable.img_nopic_03).into(ivPhoto)
                    }
                } else {
                    Glide.with(ivPhoto.context)
                        .load(image.url).placeholder(R.drawable.img_nopic_03).into(ivPhoto)
                }
            }
        }

        commentImage.setOnClickListener {
            when (isClipList) {
                true -> itemList?.also { adultListener.onClipCommentClick(it, position) }
                false -> adultListener.onCommentClick(item, AdultTabType.CLIP)
            }
        }

        moreImage.setOnClickListener {
            itemList?.also { adultListener.onMoreClick(item, it) }
        }

        ivPhoto.setOnClickListener {
            when (isClipList) {
                true -> itemList?.also { adultListener.onClipItemClick(it, position) }
                false -> adultListener.onItemClick(item, AdultTabType.CLIP)
            }
        }

        ivAvatar.setOnClickListener {
            adultListener.onAvatarClick(item.creatorId, item.postFriendlyName)
        }
    }

    private fun updatePicture(id: String) {
        val bitmap = LruCacheUtils.getLruCache(id)
        Glide.with(ivPhoto.context).load(bitmap).into(ivPhoto)
    }

    private fun updateAvatar(id: String) {
        val bitmap = LruCacheUtils.getLruCache(id)
        Glide.with(ivAvatar.context).load(bitmap).circleCrop().into(ivAvatar)
    }

    private fun updateLikeAndFollowItem(
        item: MemberPostItem,
        itemList: List<MemberPostItem>?,
        memberPostFuncItem: MemberPostFuncItem
    ) {
        likeCount.text = item.likeCount.toString()
        favoriteCount.text = item.favoriteCount.toString()
        commentCount.text = item.commentCount.toString()

        if (item.isFollow) {
            follow.text = follow.context.getString(R.string.followed)
            follow.background = follow.context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            follow.setTextColor(follow.context.getColor(R.color.color_white_1))
        } else {
            follow.text = follow.context.getString(R.string.follow)
            follow.background = follow.context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            follow.setTextColor(follow.context.getColor(R.color.color_red_1))
        }

        val likeType = item.likeType
        if (likeType == LikeType.LIKE) {
            likeImage.setImageResource(R.drawable.ico_nice_s)
        } else {
            likeImage.setImageResource(R.drawable.ico_nice)
        }

        if (item.isFavorite) {
            favoriteImage.setImageResource(R.drawable.btn_favorite_white_s)
        } else {
            favoriteImage.setImageResource(R.drawable.btn_favorite_white_n)
        }

        follow.setOnClickListener {
            itemList?.also {
                memberPostFuncItem.onFollowClick(item, itemList, !(item.isFollow)) { isFollow ->
                    updateFollow(
                        isFollow
                    )
                }
            }
        }

        likeImage.setOnClickListener {
            val isLike = item.likeType == LikeType.LIKE
            memberPostFuncItem.onLikeClick(item, !isLike) { like, count -> updateLike(like, count) }
        }

        favoriteImage.setOnClickListener {
            val isFavorite = item.isFavorite
            memberPostFuncItem.onFavoriteClick(item, !isFavorite) { favorite, count ->
                updateFavorite(favorite, count)
            }
        }
    }

    fun updateFollow(isFollow: Boolean) {
        if (isFollow) {
            follow.text = follow.context.getString(R.string.followed)
            follow.background = follow.context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            follow.setTextColor(follow.context.getColor(R.color.color_white_1))
        } else {
            follow.text = follow.context.getString(R.string.follow)
            follow.background = follow.context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            follow.setTextColor(follow.context.getColor(R.color.color_red_1))
        }
    }

    private fun updateLike(isLike: Boolean, count: Int) {
        if (isLike) {
            likeImage.setImageResource(R.drawable.ico_nice_s)
        } else {
            likeImage.setImageResource(R.drawable.ico_nice)
        }
        likeCount.text = count.toString()
    }

    private fun updateFavorite(isFavorite: Boolean, count: Int) {
        if (isFavorite) {
            favoriteImage.setImageResource(R.drawable.btn_favorite_white_s)
        } else {
            favoriteImage.setImageResource(R.drawable.btn_favorite_white_n)
        }
        favoriteCount.text = count.toString()
    }
}