package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.callback.OnPictureItemClickListener
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.view.adapter.PictureAdapter
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.GeneralUtils.getSpanString
import com.dabenxiang.mimi.widget.utility.LoadImageUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_clip_post.view.*
import kotlinx.android.synthetic.main.item_picture_post.view.*
import kotlinx.android.synthetic.main.item_picture_post.view.chip_group_tag
import kotlinx.android.synthetic.main.item_picture_post.view.img_avatar
import kotlinx.android.synthetic.main.item_picture_post.view.iv_ad
import kotlinx.android.synthetic.main.item_picture_post.view.iv_comment
import kotlinx.android.synthetic.main.item_picture_post.view.iv_favorite
import kotlinx.android.synthetic.main.item_picture_post.view.iv_like
import kotlinx.android.synthetic.main.item_picture_post.view.iv_more
import kotlinx.android.synthetic.main.item_picture_post.view.tv_comment_count
import kotlinx.android.synthetic.main.item_picture_post.view.tv_favorite_count
import kotlinx.android.synthetic.main.item_picture_post.view.tv_follow
import kotlinx.android.synthetic.main.item_picture_post.view.tv_like_count
import kotlinx.android.synthetic.main.item_picture_post.view.tv_name
import kotlinx.android.synthetic.main.item_picture_post.view.tv_time
import kotlinx.android.synthetic.main.item_picture_post.view.tv_title
import kotlinx.android.synthetic.main.item_picture_post.view.tv_title_more
import kotlinx.android.synthetic.main.item_picture_post.view.v_separator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.*

class MyPostPicturePostHolder(
    itemView: View
) : BaseViewHolder(itemView),KoinComponent {

    private val accountManager: AccountManager by inject()

    val pictureRecycler: RecyclerView = itemView.recycler_picture
    private val picturePostItemLayout: ConstraintLayout = itemView.layout_picture_post_item
    private val imgAvatar: ImageView = itemView.img_avatar
    private val tvName: TextView = itemView.tv_name
    private val tvTime: TextView = itemView.tv_time
    private val tvTitle: TextView = itemView.tv_title
    private val tvTitleMore: TextView = itemView.tv_title_more
    private val tvPictureCount: TextView = itemView.tv_picture_count
    private val tagChipGroup: ChipGroup = itemView.chip_group_tag
    private val ivLike: ImageView = itemView.iv_like
    private val tvLikeCount: TextView = itemView.tv_like_count
    private val ivComment: ImageView = itemView.iv_comment
    private val tvCommentCount: TextView = itemView.tv_comment_count
    private val ivMore: ImageView = itemView.iv_more
    private val tvFollow: TextView = itemView.tv_follow
    private val vSeparator: View = itemView.v_separator
    private val ivFavorite: ImageView = itemView.iv_favorite
    private val tvFavoriteCount: TextView = itemView.tv_favorite_count
    private val ivAd:ImageView = itemView.iv_ad

    fun onBind(
            item: MemberPostItem,
            position: Int,
            myPostListener: MyPostListener,
            viewModelScope: CoroutineScope,
            searchStr: String = "",
            searchTag: String = "",
            adGap:Int? = null
    ) {
        picturePostItemLayout.setBackgroundColor(App.self.getColor(R.color.color_white_1))
        tvName.setTextColor(App.self.getColor(R.color.color_black_1))
        tvTime.setTextColor(App.self.getColor(R.color.color_black_1_50))
        tvTitle.setTextColor(App.self.getColor(R.color.color_black_1))
        tvLikeCount.setTextColor(App.self.getColor(R.color.color_black_1))
        tvCommentCount.setTextColor(App.self.getColor(R.color.color_black_1))
        ivComment.setImageResource(R.drawable.ico_messege_adult_gray)
        ivMore.setImageResource(R.drawable.btn_more_gray_n)
        vSeparator.setBackgroundColor(App.self.getColor(R.color.color_black_1_05))
        if (adGap != null && position % adGap == adGap - 1) {
            ivAd.visibility = View.VISIBLE
            val options = RequestOptions()
                .priority(Priority.NORMAL)
                .placeholder(R.drawable.img_ad)
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

        tvName.text = item.postFriendlyName
        tvTime.text = GeneralUtils.getTimeDiff(item.creationDate, Date())
        item.title.let {
            val title = if (searchStr.isNotBlank()) getSpanString(
                    tvTitle.context,
                    item.title,
                    searchStr).toString() else item.title
            tvTitle.text = title
            Timber.i("title size=${tvTitle.text.length}")
            tvTitleMore.visibility = if(tvTitle.text.length >=45){
                View.VISIBLE
            }else{
                View.GONE
            }
        }
        tvFollow.visibility = if(accountManager.getProfile().userId == item.creatorId) View.GONE else View.VISIBLE

        viewModelScope.launch {
            LoadImageUtils.loadImage(item.avatarAttachmentId, imgAvatar, LoadImageType.AVATAR)
        }

        imgAvatar.setOnClickListener {
            myPostListener.onAvatarClick(item.creatorId,item.postFriendlyName)
        }

        tagChipGroup.removeAllViews()
        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            if (it == searchTag || it == searchStr) chip.setTextColor(tagChipGroup.context.getColor(R.color.color_red_1))
            else chip.setTextColor(tagChipGroup.context.getColor(R.color.color_black_1_50))
            chip.setOnClickListener { view ->
                myPostListener.onChipClick(PostType.IMAGE, (view as Chip).text.toString())
            }
            tagChipGroup.addView(chip)
        }

        val contentItem = Gson().fromJson(item.postContent, MediaContentItem::class.java)
        if (pictureRecycler.adapter == null || tvPictureCount.tag != position) {
            tvPictureCount.tag = position
            pictureRecycler.layoutManager = LinearLayoutManager(
                pictureRecycler.context, LinearLayoutManager.HORIZONTAL, false
            )

            pictureRecycler.adapter = PictureAdapter(
                pictureRecycler.context,
                contentItem.images ?: arrayListOf(),
                object : OnPictureItemClickListener {
                    override fun onItemClick() {
                        item.also { myPostListener.onItemClick(item, AdultTabType.PICTURE) }
                    }

                    override fun onLoginClick() {
                        myPostListener.onLoginClick()
                    }

                    override fun onRegisterClick() {
                        myPostListener.onRegisterClick()
                    }
                }, viewModelScope
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

        tvFollow.visibility = View.GONE

        updateFavorite(item)
        val onFavoriteClickListener = View.OnClickListener {
            item.isFavorite = !item.isFavorite
            item.favoriteCount =
                    if (item.isFavorite) item.favoriteCount + 1 else item.favoriteCount - 1

            updateFavorite(item)
            myPostListener.onFavoriteClick(
                    item,
                    position,
                    item.isFavorite,
                    AttachmentType.ADULT_HOME_CLIP
            )
        }
        ivFavorite.setOnClickListener(onFavoriteClickListener)
        tvFavoriteCount.setOnClickListener(onFavoriteClickListener)

        ivMore.setOnClickListener {
            myPostListener.onMoreClick(item, position)
        }

        updateLike(item)
        val onLikeClickListener = View.OnClickListener {
            item.likeType = if (item.likeType == LikeType.LIKE) null else LikeType.LIKE
            item.likeCount =
                if (item.likeType == LikeType.LIKE) item.likeCount + 1 else item.likeCount - 1
            updateLike(item)
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
        tvTitleMore.setOnClickListener {
            item.also { myPostListener.onItemClick(item, AdultTabType.PICTURE) }
        }
    }


    fun updateLike(item: MemberPostItem) {
        tvLikeCount.text = item.likeCount.toString()

        if (item.likeType == LikeType.LIKE) {
            ivLike.setImageResource(R.drawable.ico_nice_s)
        } else {
            ivLike.setImageResource(R.drawable.ico_nice_gray)
        }
    }

    fun updateFollow(item: MemberPostItem) {
        tvFollow.setText(if (item.isFollow) R.string.followed else R.string.follow)
        tvFollow.setBackgroundResource(if (item.isFollow) R.drawable.bg_white_1_stroke_radius_16 else R.drawable.bg_red_1_stroke_radius_16)
        tvFollow.setTextColor(App.self.getColor(if (item.isFollow) R.color.color_black_1_60 else R.color.color_red_1))
    }

    fun updateFavorite(item: MemberPostItem) {
        tvFavoriteCount.text = item.favoriteCount.toString()

        if (item.isFavorite) {
            ivFavorite.setImageResource(R.drawable.btn_favorite_white_s)
        } else {
            ivFavorite.setImageResource(R.drawable.btn_favorite_n)
        }
    }

}