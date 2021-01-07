package com.dabenxiang.mimi.view.club.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.club.base.PostDBDiffCallback.diffCallback
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class PostItemAdapter(
    val context: Context,
    private val myPostListener: MyPostListener,
    private val viewModelScope: CoroutineScope,
    private val adGap: Int? = null
) : PagingDataAdapter<MemberPostItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        const val UPDATE_LIKE = 0
        const val UPDATE_FAVORITE = 1
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item?.type) {
            PostType.VIDEO -> VIEW_TYPE_CLIP
            PostType.IMAGE -> VIEW_TYPE_PICTURE
            PostType.AD -> VIEW_TYPE_AD
            else -> VIEW_TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> {
                AdHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_ad, parent, false)
                )
            }
            VIEW_TYPE_CLIP -> {
                MyPostClipPostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_clip_post, parent, false)
                )
            }
            VIEW_TYPE_PICTURE -> {
                MyPostPicturePostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_picture_post, parent, false)
                )
            }
            VIEW_TYPE_TEXT -> {
                MyPostTextPostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_text_post, parent, false)
                )
            }
            else -> {
                DeletedItemViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_deleted, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = getItem(position)
        item?.also { memberPostItem ->
            Timber.i("PostItemAdapter position=$position  holder=$holder")
            when (holder) {
                is AdHolder -> {
                    val options = RequestOptions()
                        .priority(Priority.NORMAL)
                        .placeholder(R.drawable.img_ad)
                        .error(R.drawable.img_ad)
                    Glide.with(context)
                        .load(item.adItem?.href)
                        .apply(options)
                        .into(holder.adImg)
                    holder.adImg.setOnClickListener {
                        GeneralUtils.openWebView(context, item.adItem?.target ?: "")
                    }
                }

                is MyPostPicturePostHolder -> {
                    holder.pictureRecycler.tag = position
                    holder.onBind(
                        memberPostItem,
                        position,
                        myPostListener,
                        viewModelScope,
                        adGap = adGap
                    )
                }
                is MyPostTextPostHolder -> {
                    holder.onBind(
                        memberPostItem,
                        position,
                        myPostListener,
                        viewModelScope,
                        adGap = adGap
                    )
                }
                is MyPostClipPostHolder -> {
                    holder.onBind(
                        memberPostItem,
                        position,
                        myPostListener,
                        viewModelScope,
                        adGap = adGap
                    )
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

}