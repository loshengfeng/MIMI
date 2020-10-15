package com.dabenxiang.mimi.view.club

import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.PostItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_club_member_post.view.*

/**
 * VAI4.1.6_圈子頁 滑動內容
 */
class ClubMemberPostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val ivCover = view.iv_cover
    val tvPostType = view.tv_post_type
    val tvTile = view.tv_tile

    fun onBind(item: PostItem, clubItem: MemberClubItem, clubFuncItem: ClubFuncItem) {
        tvTile.text = item.title
        tvPostType.text = when (item.type) {
            PostType.IMAGE -> tvPostType.context.getString(R.string.picture)
            PostType.VIDEO -> tvPostType.context.getString(R.string.video)
            else -> tvPostType.context.getString(R.string.text)
        }

        val contentItem = Gson().fromJson(item.content, MediaContentItem::class.java)

        contentItem.images?.also { images ->
            if (!TextUtils.isEmpty(images[0].url)) {
                Glide.with(ivCover.context)
                    .load(images[0].url).placeholder(R.drawable.img_nopic_03).into(ivCover)
            } else {
                images[0].id.toLongOrNull()?.also { id ->
                    clubFuncItem.getBitmap(id, ivCover, LoadImageType.PICTURE_THUMBNAIL)
                }
            }
        }

        ivCover.setOnClickListener {
            clubFuncItem.onItemClick(clubItem)
        }
    }
}