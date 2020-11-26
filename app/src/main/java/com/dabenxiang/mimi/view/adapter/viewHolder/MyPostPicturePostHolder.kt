package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class MyPostPicturePostHolder(
    itemView: View,
    private val isAdultTheme: Boolean
) : BaseViewHolder(itemView),KoinComponent {

    private val accountManager: AccountManager by inject()

    private val picturePostItemLayout: ConstraintLayout = itemView.layout_picture_post_item
    private val imgAvatar: ImageView = itemView.img_avatar
    private val tvName: TextView = itemView.tv_name
    private val tvTime: TextView = itemView.tv_time
    private val tvTitle: TextView = itemView.tv_title
    val pictureRecycler: RecyclerView = itemView.recycler_picture
    private val tvPictureCount: TextView = itemView.tv_picture_count
    private val tagChipGroup: ChipGroup = itemView.chip_group_tag
    private val ivLike: ImageView = itemView.iv_like
    private val tvLikeCount: TextView = itemView.tv_like_count
    private val ivComment: ImageView = itemView.iv_comment
    private val tvCommentCount: TextView = itemView.tv_comment_count
    private val ivMore: ImageView = itemView.iv_more
    private val tvFollow: TextView = itemView.tv_follow
    private val vSeparator: View = itemView.v_separator

    fun onBind(
        item: MemberPostItem,
        itemList: List<MemberPostItem>?,
        position: Int,
        myPostListener: MyPostListener,
        attachmentListener: AttachmentListener,
        memberPostFuncItem: MemberPostFuncItem
    ) {
        val isMe = accountManager.getProfile().userId == item.creatorId

        picturePostItemLayout.setBackgroundColor(App.self.getColor(if (isAdultTheme) R.color.color_black_4 else R.color.color_white_1))
        tvName.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        tvTime.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1_50 else R.color.color_black_1_50))
        tvTitle.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        tvLikeCount.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        tvCommentCount.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        ivComment.setImageResource(if (isAdultTheme) R.drawable.ico_messege_adult else R.drawable.ico_messege_adult_gray)
        ivMore.setImageResource(if (isAdultTheme) R.drawable.btn_more_white_n else R.drawable.btn_more_gray_n)
        vSeparator.setBackgroundColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1_30 else R.color.color_black_1_05))

        tvName.text = item.postFriendlyName
        tvTime.text = GeneralUtils.getTimeDiff(item.creationDate, Date())
        tvTitle.text = item.title
        tvFollow.visibility = if(accountManager.getProfile().userId == item.creatorId) View.GONE else View.VISIBLE

        attachmentListener.onGetAttachment(item.avatarAttachmentId, imgAvatar, LoadImageType.AVATAR)
        imgAvatar.setOnClickListener {
            myPostListener.onAvatarClick(item.creatorId,item.postFriendlyName)
        }

        tagChipGroup.removeAllViews()
        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            chip.setTextColor(tagChipGroup.context.getColor(if (isAdultTheme) R.color.color_white_1_50 else R.color.color_black_1_50))
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    tagChipGroup.context,
                    if (isAdultTheme) R.color.color_black_6 else R.color.color_black_1_05
                )
            )
            chip.setOnClickListener { view ->
                myPostListener.onChipClick(PostType.IMAGE, (view as Chip).text.toString())
            }
            tagChipGroup.addView(chip)
        }

        val contentItem = Gson().fromJson(item.content, MediaContentItem::class.java)
        if (pictureRecycler.adapter == null || tvPictureCount.tag != position) {
            tvPictureCount.tag = position
            pictureRecycler.layoutManager = LinearLayoutManager(
                pictureRecycler.context, LinearLayoutManager.HORIZONTAL, false
            )

            pictureRecycler.adapter = PictureAdapter(
                pictureRecycler.context,
                contentItem.images ?: arrayListOf(),
                object : OnItemClickListener {
                    override fun onItemClick() {
                        item.also { myPostListener.onItemClick(item, AdultTabType.PICTURE) }
                    }
                },
                memberPostFuncItem
            )
            pictureRecycler.onFlingListener = null
            PagerSnapHelper().attachToRecyclerView(pictureRecycler)

            pictureRecycler.setOnScrollChangeListener { _, _, _, _, _ ->
                val currentPosition =
                    (pictureRecycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                tvPictureCount.text =
                    "${currentPosition + 1}/${contentItem.images?.size}"
            }

            tvPictureCount.text = "1/${contentItem.images?.size}"
        }

        if (isMe) {
            tvFollow.visibility = View.GONE
        } else {
            tvFollow.visibility = View.VISIBLE
            tvFollow.setOnClickListener {
                itemList?.also { myPostListener.onFollowClick(itemList, position, !item.isFollow) }
                item.isFollow = !item.isFollow
            }
            updateFollow(item)
        }

        ivMore.setOnClickListener {
            myPostListener.onMoreClick(item)
        }

        updateLike(item)
        val onLikeClickListener = View.OnClickListener {
            item.likeType = if (item.likeType == LikeType.LIKE) LikeType.DISLIKE else LikeType.LIKE
            item.likeCount =
                if (item.likeType == LikeType.LIKE) item.likeCount + 1 else item.likeCount - 1
            myPostListener.onLikeClick(item, position, item.likeType == LikeType.LIKE)
        }
        ivLike.setOnClickListener(onLikeClickListener)
        tvLikeCount.setOnClickListener(onLikeClickListener)

        tvCommentCount.text = item.commentCount.toString()
        val onCommentClickListener = View.OnClickListener {
            item.also { myPostListener.onCommentClick(it, AdultTabType.PICTURE) }
        }
        ivComment.setOnClickListener(onCommentClickListener)
        tvCommentCount.setOnClickListener(onCommentClickListener)

        picturePostItemLayout.setOnClickListener {
            item.also { myPostListener.onItemClick(item, AdultTabType.PICTURE) }
        }
    }


    fun updateLike(item: MemberPostItem) {
        tvLikeCount.text = item.likeCount.toString()

        if (item.likeType == LikeType.LIKE) {
            ivLike.setImageResource(R.drawable.ico_nice_s)
        } else {
            ivLike.setImageResource(if (isAdultTheme) R.drawable.ico_nice else R.drawable.ico_nice_gray)
        }
    }

    fun updateFollow(item: MemberPostItem) {
        tvFollow.setText(if (item.isFollow) R.string.followed else R.string.follow)
        tvFollow.setBackgroundResource(if (item.isFollow) R.drawable.bg_white_1_stroke_radius_16 else R.drawable.bg_red_1_stroke_radius_16)
        tvFollow.setTextColor(App.self.getColor(if (item.isFollow) R.color.color_black_1_60 else R.color.color_red_1))
    }

}