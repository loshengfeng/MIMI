package com.dabenxiang.mimi.view.club.pages

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.zxing.client.android.Intents.Scan.RESULT
import kotlinx.android.synthetic.main.item_ad.view.*
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.inject
import timber.log.Timber

class ClubItemAdapter(
        val context: Context,
        private val myPostListener: MyPostListener,
        private val viewModelScope: CoroutineScope,
        val mimiDB: MiMiDB
) : PagingDataAdapter<MemberPostWithPostDBItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        const val PAYLOAD_UPDATE_LIKE = 0
        const val PAYLOAD_UPDATE_FAVORITE = 1
        const val PAYLOAD_UPDATE_FOLLOW = 2

        const val VIEW_TYPE_CLIP = 0
        const val VIEW_TYPE_PICTURE = 1
        const val VIEW_TYPE_TEXT = 2
        const val VIEW_TYPE_DELETED = 3
        const val VIEW_TYPE_AD = 4

        val diffCallback = object : DiffUtil.ItemCallback<MemberPostWithPostDBItem>() {
            override fun areItemsTheSame(
                    oldItem: MemberPostWithPostDBItem,
                    newItem: MemberPostWithPostDBItem
            ): Boolean {
                return oldItem.postDBItem.id == newItem.postDBItem.id
            }

            override fun areContentsTheSame(
                    oldItem: MemberPostWithPostDBItem,
                    newItem: MemberPostWithPostDBItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun getChangePayload(oldItem: MemberPostWithPostDBItem, newItem: MemberPostWithPostDBItem): Any? {
                return oldItem.copy(memberPostItem = newItem.memberPostItem) == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)?.postDBItem
        return when (item?.postType) {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

//        val item = getItem(position)?.postDBId?.let {
//            mimiDB.postDBItemDao().getMemberPostItemById(it)
//        }
        val item = getItem(position)?.memberPostItem
        item?.also {memberPostItem->
            Timber.i("memberPostItem $memberPostItem position=$position  holder=$holder")
            when (holder) {
                is AdHolder -> {
                    Timber.i("memberPostItem $memberPostItem AdHolder=$holder")
                    Glide.with(context).load(memberPostItem.adItem?.href)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(holder.adImg)
                    holder.adImg.setOnClickListener {
                        GeneralUtils.openWebView(context, memberPostItem.adItem?.target ?: "")
                    }
                }

                is MyPostPicturePostHolder -> {

                    holder.pictureRecycler.tag = position
                    holder.onBind(
                            memberPostItem,
                            position,
                            myPostListener,
                            viewModelScope
                    )


                }
                is MyPostTextPostHolder -> {
                    holder.onBind(
                            memberPostItem,
                            position,
                            myPostListener,
                            viewModelScope
                    )

                }
                is MyPostClipPostHolder -> {
                    holder.onBind(
                            memberPostItem,
                            position,
                            myPostListener,
                            viewModelScope
                    )

                }
            }
        }
    }

}