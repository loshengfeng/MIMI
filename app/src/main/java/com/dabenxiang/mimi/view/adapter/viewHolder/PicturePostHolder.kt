package com.dabenxiang.mimi.view.adapter.viewHolder

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.view.adapter.PictureAdapter
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_picture_post.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class PicturePostHolder(itemView: View) : BaseViewHolder(itemView), KoinComponent {

    private val accountManager: AccountManager by inject()

    val picturePostItemLayout: ConstraintLayout = itemView.layout_picture_post_item
    val avatarImg: ImageView = itemView.img_avatar
    val name: TextView = itemView.tv_name
    val time: TextView = itemView.tv_time
    val follow: TextView = itemView.tv_follow
    val title: TextView = itemView.tv_title
    val pictureRecycler: RecyclerView = itemView.recycler_picture
    val pictureCount: TextView = itemView.tv_picture_count
    val tagChipGroup: ChipGroup = itemView.chip_group_tag
    val likeImage: ImageView = itemView.iv_like
    val likeCount: TextView = itemView.tv_like_count
    val commentImage: ImageView = itemView.iv_comment
    val commentCount: TextView = itemView.tv_comment_count
    val moreImage: ImageView = itemView.iv_more

    fun onBind(
        item: MemberPostItem,
        itemList: List<MemberPostItem>?,
        position: Int,
        adultListener: AdultListener,
        tag: String,
        memberPostFuncItem: MemberPostFuncItem = MemberPostFuncItem()
    ) {
        name.text = item.postFriendlyName
        time.text = GeneralUtils.getTimeDiff(item.creationDate ?: Date(), Date())
        title.text = item.title
        follow.visibility =
            if (accountManager.getProfile().userId == item.creatorId) View.GONE else View.VISIBLE

        updateLikeAndFollowItem(item, itemList, memberPostFuncItem)

        memberPostFuncItem.getBitmap(item.avatarAttachmentId, avatarImg, LoadImageType.AVATAR)

        tagChipGroup.removeAllViews()
        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            if (TextUtils.isEmpty(tag)) {
                chip.setTextColor(tagChipGroup.context.getColor(R.color.color_black_1_50))
            } else {
                if (it == tag) {
                    chip.setTextColor(chip.context.getColor(R.color.color_red_1))
                } else {
                    chip.setTextColor(tagChipGroup.context.getColor(R.color.color_black_1_50))
                }
            }
            chip.setOnClickListener { view ->
                adultListener.onChipClick(PostType.IMAGE, (view as Chip).text.toString())
            }
            tagChipGroup.addView(chip)
        }

        val contentItem = Gson().fromJson(item.content, MediaContentItem::class.java)
        pictureCount.tag = position
        pictureRecycler.layoutManager = LinearLayoutManager(
            pictureRecycler.context, LinearLayoutManager.HORIZONTAL, false
        )

        pictureRecycler.adapter = PictureAdapter(
            pictureRecycler.context,
            contentItem.images ?: arrayListOf(),
            object : OnItemClickListener {
                override fun onItemClick() {
                    item.also { adultListener.onItemClick(item, AdultTabType.PICTURE) }
                }
            },
            memberPostFuncItem
        )
        pictureRecycler.onFlingListener = null
        PagerSnapHelper().attachToRecyclerView(pictureRecycler)

        pictureRecycler.setOnScrollChangeListener { _, _, _, _, _ ->
            val currentPosition =
                (pictureRecycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            pictureCount.text =
                "${currentPosition + 1}/${contentItem.images?.size}"
        }

        pictureCount.text = "1/${contentItem.images?.size}"

        commentImage.setOnClickListener {
            item.also { adultListener.onCommentClick(it, AdultTabType.PICTURE) }
        }

        moreImage.setOnClickListener {
            itemList?.also { adultListener.onMoreClick(item, it) }
        }

        picturePostItemLayout.setOnClickListener {
            item.also { adultListener.onItemClick(item, AdultTabType.PICTURE) }
        }

        avatarImg.setOnClickListener {
            adultListener.onAvatarClick(item.creatorId, item.postFriendlyName)
        }
    }

    fun updateLikeAndFollowItem(
        item: MemberPostItem,
        itemList: List<MemberPostItem>?,
        memberPostFuncItem: MemberPostFuncItem
    ) {
        likeCount.text = item.likeCount.toString()
        commentCount.text = item.commentCount.toString()

        val isFollow = item.isFollow
        if (isFollow) {
            follow.text = follow.context.getString(R.string.followed)
            follow.background =
                follow.context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            follow.setTextColor(follow.context.getColor(R.color.color_black_1_60))
        } else {
            follow.text = follow.context.getString(R.string.follow)
            follow.background =
                follow.context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            follow.setTextColor(follow.context.getColor(R.color.color_red_1))
        }

        val likeType = item.likeType
        if (likeType == LikeType.LIKE) {
            likeImage.setImageResource(R.drawable.ico_nice_s)
        } else {
            likeImage.setImageResource(R.drawable.ico_nice_gray)
        }

        follow.setOnClickListener {
            itemList?.also {
                memberPostFuncItem.onFollowClick(item, itemList, !item.isFollow) { isFollow ->
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
    }

    fun updateFollow(isFollow: Boolean) {
        if (isFollow) {
            follow.text = follow.context.getString(R.string.followed)
            follow.background = follow.context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            follow.setTextColor(follow.context.getColor(R.color.color_black_1_60))
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
            likeImage.setImageResource(R.drawable.ico_nice_gray)
        }
        likeCount.text = count.toString()
    }

}