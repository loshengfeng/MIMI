package com.dabenxiang.mimi.view.club

import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.PostItem
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_club_member_post.view.*
import timber.log.Timber

/**
 * VAI4.1.6_圈子頁 滑動內容
 */
class ClubMemberPostViewHolder(view: View) : RecyclerView.ViewHolder(view)  {
    val ivCover = view.iv_cover
    val tvPostType = view.tv_post_type
    val tvTile = view.tv_tile

    fun onBind(item: PostItem, clubFuncItem: ClubFuncItem) {
        tvTile.text = item.title
        tvPostType.text = when(item.type) {
            PostType.IMAGE -> tvPostType.context.getString(R.string.picture)
            PostType.VIDEO -> tvPostType.context.getString(R.string.video)
            else -> tvPostType.context.getString(R.string.text)
        }

        val contentItem = Gson().fromJson(item.content, MediaContentItem::class.java)

        contentItem.images?.takeIf { it.isNotEmpty() }?.also { images ->
            images[0].also { image ->
                if (TextUtils.isEmpty(image.url)) {
                    image.id.takeIf { !TextUtils.isEmpty(it) && it != LruCacheUtils.ZERO_ID }?.also { id ->
                        LruCacheUtils.getLruCache(id)?.also { bitmap ->
                            Glide.with(ivCover.context).load(bitmap).into(ivCover)
                        } ?: run {
                            clubFuncItem.getBitmap(id) { id -> updateCover(id) }
                        }
                    } ?: run { Glide.with(ivCover.context).load(R.drawable.img_nopic_03).into(ivCover) }
                } else {
                    Glide.with(ivCover.context)
                        .load(image.url).placeholder(R.drawable.img_nopic_03).into(ivCover)
                }
            }
        }
    }

    private fun updateCover(id: String) {
        val bitmap = LruCacheUtils.getLruCache(id)
        Glide.with(ivCover.context).load(bitmap).into(ivCover)
    }
}